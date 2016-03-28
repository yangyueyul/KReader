/*
 * Copyright (C) 2004-2015 FBReader.ORG Limited <contact@fbreader.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

#include <algorithm>

#include <ZLDir.h>
#include <ZLInputStream.h>
#include <ZLLogger.h>
#include <ZLStringUtil.h>
#include <ZLUnicodeUtil.h>
#include <FileEncryptionInfo.h>
#include <ZLFile.h>
#include <ZLFileImage.h>
#include <ZLXMLNamespace.h>

#include "OEBBookReader.h"
#include "OEBEncryptionReader.h"
#include "XHTMLImageFinder.h"
#include "NCXReader.h"
#include "../xhtml/XHTMLReader.h"
#include "../util/MiscUtil.h"
#include "../../bookmodel/BookModel.h"

OEBBookReader::OEBBookReader(BookModel &model) : myModelReader(model) {
}

static const std::string MANIFEST = "manifest";
static const std::string SPINE = "spine";
static const std::string GUIDE = "guide";
static const std::string TOUR = "tour";
static const std::string SITE = "site";

static const std::string ITEM = "item";
static const std::string ITEMREF = "itemref";
static const std::string REFERENCE = "reference";

static const std::string COVER = "cover";
static const std::string COVER_IMAGE = "other.ms-coverimage-standard";

/**
 * 通过ZLZipEntryFile类的getInputStream方法和ZLZipEntryFile类对应的LocalFileHeader类获取一个针对epub内部xml文件的字节流类（ZipInputStream类）
 * 并以这个字节流类为参数初始化了一个针对资源文件的字符流类。接着，就调用了ZLXMLParser类的doIt方法。
 * ZLXMLParser类的doIt方法利用字符流类将文件转换成一个char数组（第五章用一整个章节介绍了这个转换的流程）
 * 在利用for循环迭代char数组的过程中，
 * doIt方法又反过来调用ZLXMLReader接口实现类（ResourceTreeReader类）的startElementHandler与endElementHandler方法
 * 对byte数组中元素所代表的不同节点进行操作
 *
 * .opf文件中的“manifest节点的作用是描述epub文件内部对应不同章节对应的文件的位置”
 * 把这些位置信息存储到myHtmlFileNames属性指向的ArrayList中
 *
 * 而这个工作是OEBBookReader类的startElementHandler方法中完成的。
 */


void OEBBookReader::startElementHandler(const char *tag, const char **xmlattributes) {
	std::string tagString = ZLUnicodeUtil::toLower(tag);

	switch (myState) {
		case READ_NONE:
			if (testOPFTag(MANIFEST, tagString)) {
				myState = READ_MANIFEST;
			} else if (testOPFTag(SPINE, tagString)) {
				const char *toc = attributeValue(xmlattributes, "toc");
				if (toc != 0) {
					myNCXTOCFileName = myIdToHref[toc];
				}
				myState = READ_SPINE;
			} else if (testOPFTag(GUIDE, tagString)) {
				myState = READ_GUIDE;
			} else if (testOPFTag(TOUR, tagString)) {
				myState = READ_TOUR;
			}
			break;
		case READ_MANIFEST:
			if (testOPFTag(ITEM, tagString)) {
				/**
				 * 获取xhtml中href属性
				 */
				const char *href = attributeValue(xmlattributes, "href");
				if (href != 0) {
					// 对href属性进行处理
					const std::string sHref = MiscUtil::decodeHtmlURL(href);
					const char *id = attributeValue(xmlattributes, "id");
					const char *mediaType = attributeValue(xmlattributes, "media-type");
					if (id != 0) {
						myIdToHref[id] = sHref; // 将href属性加入
					}
					if (mediaType != 0) {
						myHrefToMediatype[sHref] = mediaType;
					}
				}
			}
			break;
		case READ_SPINE:
			if (testOPFTag(ITEMREF, tagString)) {
				const char *id = attributeValue(xmlattributes, "idref");
				if (id != 0) {
					const std::string &fileName = myIdToHref[id];
					if (!fileName.empty()) {
						myHtmlFileNames.push_back(fileName);
					}
				}
			}
			break;
		case READ_GUIDE:
			if (testOPFTag(REFERENCE, tagString)) {
				const char *type = attributeValue(xmlattributes, "type");
				const char *title = attributeValue(xmlattributes, "title");
				const char *href = attributeValue(xmlattributes, "href");
				if (href != 0) {
					const std::string reference = MiscUtil::decodeHtmlURL(href);
					if (title != 0) {
						myGuideTOC.push_back(std::make_pair(std::string(title), reference));
					}
					if (type != 0 && (COVER == type || COVER_IMAGE == type)) {
						ZLFile imageFile(myFilePrefix + reference);
						myCoverFileName = imageFile.path();
						myCoverFileType = type;
						const std::map<std::string,std::string>::const_iterator it =
							myHrefToMediatype.find(reference);
						myCoverMimeType =
							it != myHrefToMediatype.end() ? it->second : std::string();
					}
				}
			}
			break;
		case READ_TOUR:
			if (testOPFTag(SITE, tagString)) {
				const char *title = attributeValue(xmlattributes, "title");
				const char *href = attributeValue(xmlattributes, "href");
				if ((title != 0) && (href != 0)) {
					myTourTOC.push_back(std::make_pair(title, MiscUtil::decodeHtmlURL(href)));
				}
			}
			break;
	}
}

bool OEBBookReader::coverIsSingleImage() const {
	return
		COVER_IMAGE == myCoverFileType ||
		(COVER == myCoverFileType &&
			ZLStringUtil::stringStartsWith(myCoverMimeType, "image/"));
}

void OEBBookReader::addCoverImage() {
	ZLFile imageFile(myCoverFileName);
	shared_ptr<const ZLImage> image = coverIsSingleImage()
		? new ZLFileImage(imageFile, "", 0) : XHTMLImageFinder().readImage(imageFile);

	if (!image.isNull()) {
		const std::string imageName = imageFile.name(false);
		myModelReader.setMainTextModel();
		myModelReader.addImageReference(imageName, (short)0, true);
		myModelReader.addImage(imageName, image);
		myModelReader.insertEndOfSectionParagraph();
	}
}

void OEBBookReader::endElementHandler(const char *tag) {
	std::string tagString = ZLUnicodeUtil::toLower(tag);

	switch (myState) {
		case READ_MANIFEST:
			if (testOPFTag(MANIFEST, tagString)) {
				myState = READ_NONE;
			}
			break;
		case READ_SPINE:
			if (testOPFTag(SPINE, tagString)) {
				myState = READ_NONE;
			}
			break;
		case READ_GUIDE:
			if (testOPFTag(GUIDE, tagString)) {
				myState = READ_NONE;
			}
			break;
		case READ_TOUR:
			if (testOPFTag(TOUR, tagString)) {
				myState = READ_NONE;
			}
			break;
		case READ_NONE:
			break;
	}
}

bool OEBBookReader::readBook(const ZLFile &opfFile) {
	const ZLFile epubFile = opfFile.getContainerArchive();
	epubFile.forceArchiveType(ZLFile::ZIP);
	shared_ptr<ZLDir> epubDir = epubFile.directory();
	if (!epubDir.isNull()) {
		myEncryptionMap = new EncryptionMap();
		const std::vector<shared_ptr<FileEncryptionInfo> > encodingInfos =
			OEBEncryptionReader().readEncryptionInfos(epubFile, opfFile);

		for (std::vector<shared_ptr<FileEncryptionInfo> >::const_iterator it = encodingInfos.begin(); it != encodingInfos.end(); ++it) {
			myEncryptionMap->addInfo(*epubDir, *it);
		}
	}

	myFilePrefix = MiscUtil::htmlDirectoryPrefix(opfFile.path());

	myIdToHref.clear();
	myHtmlFileNames.clear();
	myNCXTOCFileName.erase();
	myCoverFileName.erase();
	myCoverFileType.erase();
	myCoverMimeType.erase();
	myTourTOC.clear();
	myGuideTOC.clear();
	myState = READ_NONE;

	if (!readDocument(opfFile)) {
		return false;
	}

	myModelReader.setMainTextModel(); // myCurrentTextModel = myModel.myBookTextModel;
	myModelReader.pushKind(REGULAR);

	//ZLLogger::Instance().registerClass("oeb");
	XHTMLReader xhtmlReader(myModelReader, myEncryptionMap);
	for (std::vector<std::string>::const_iterator it = myHtmlFileNames.begin(); it != myHtmlFileNames.end(); ++it) {
		const ZLFile xhtmlFile(myFilePrefix + *it);
		if (it == myHtmlFileNames.begin()) {
			if (myCoverFileName == xhtmlFile.path()) {
				if (coverIsSingleImage()) {
					addCoverImage();
					continue;
				}
				xhtmlReader.setMarkFirstImageAsCover();
			} else {
				addCoverImage();
			}
		} else {
			myModelReader.insertEndOfSectionParagraph();
		}
		//ZLLogger::Instance().println("oeb", "start " + xhtmlFile.path());
		if (!xhtmlReader.readFile(xhtmlFile, *it)) {
			if (opfFile.exists() && !myEncryptionMap.isNull()) {
				myModelReader.insertEncryptedSectionParagraph();
			}
		}
		//ZLLogger::Instance().println("oeb", "end " + xhtmlFile.path());
		//std::string debug = "para count = ";
		//ZLStringUtil::appendNumber(debug, myModelReader.model().bookTextModel()->paragraphsNumber());
		//ZLLogger::Instance().println("oeb", debug);
	}

	generateTOC(xhtmlReader);

	return true;
}

void OEBBookReader::generateTOC(const XHTMLReader &xhtmlReader) {
	if (!myNCXTOCFileName.empty()) {
		NCXReader ncxReader(myModelReader);
		const ZLFile ncxFile(myFilePrefix + myNCXTOCFileName);
		if (ncxReader.readDocument(ncxFile.inputStream(myEncryptionMap))) {
			const std::map<int,NCXReader::NavPoint> navigationMap = ncxReader.navigationMap();
			if (!navigationMap.empty()) {
				std::size_t level = 0;
				for (std::map<int,NCXReader::NavPoint>::const_iterator it = navigationMap.begin(); it != navigationMap.end(); ++it) {
					const NCXReader::NavPoint &point = it->second;
					int index = myModelReader.model().label(xhtmlReader.normalizedReference(point.ContentHRef)).ParagraphNumber;
					while (level > point.Level) {
						myModelReader.endContentsParagraph();
						--level;
					}
					while (++level <= point.Level) {
						myModelReader.beginContentsParagraph(-2);
						myModelReader.addContentsData("...");
					}
					myModelReader.beginContentsParagraph(index);
					myModelReader.addContentsData(point.Text);
				}
				while (level > 0) {
					myModelReader.endContentsParagraph();
					--level;
				}
				return;
			}
		}
	}

	std::vector<std::pair<std::string,std::string> > &toc = myTourTOC.empty() ? myGuideTOC : myTourTOC;
	for (std::vector<std::pair<std::string,std::string> >::const_iterator it = toc.begin(); it != toc.end(); ++it) {
		int index = myModelReader.model().label(it->second).ParagraphNumber;
		if (index != -1) {
			myModelReader.beginContentsParagraph(index);
			myModelReader.addContentsData(it->first);
			myModelReader.endContentsParagraph();
		}
	}
}
