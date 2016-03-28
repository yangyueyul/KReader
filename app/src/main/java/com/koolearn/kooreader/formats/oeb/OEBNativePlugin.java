/*
 * Copyright (C) 2011-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package com.koolearn.kooreader.formats.oeb;

import com.koolearn.klibrary.core.encodings.AutoEncodingCollection;
import com.koolearn.klibrary.core.encodings.EncodingCollection;
import com.koolearn.klibrary.core.filesystem.ZLFile;
import com.koolearn.kooreader.book.AbstractBook;
import com.koolearn.kooreader.book.BookUtil;
import com.koolearn.kooreader.bookmodel.BookModel;
import com.koolearn.kooreader.formats.BookReadingException;
import com.koolearn.kooreader.formats.NativeFormatPlugin;

import java.util.Collections;
import java.util.List;

public class OEBNativePlugin extends NativeFormatPlugin {
    public OEBNativePlugin(com.koolearn.klibrary.core.util.SystemInfo systemInfo) {
        super(systemInfo, "ePub");
    }

    @Override
    public void readModel(BookModel model) throws BookReadingException {
        final ZLFile file = BookUtil.fileByBook(model.Book);
        file.setCached(true);
        try {
            super.readModel(model); // 调用父类中的本地方法
            model.setLabelResolver(new BookModel.LabelResolver() {
                public List<String> getCandidates(String id) {
                    final int index = id.indexOf("#");
                    return index > 0 ? Collections.<String>singletonList(id.substring(0, index))
                            : Collections.<String>emptyList();
                }
            });
        } finally {
            file.setCached(false);
        }
    }

    @Override
    public EncodingCollection supportedEncodings() {
        return new AutoEncodingCollection();
    }

    @Override
    public void detectLanguageAndEncoding(AbstractBook book) {
        book.setEncoding("auto");
    }

//    @Override
//    public String readAnnotation(ZLFile file) {
//        file.setCached(true);
//        try {
//            return new OEBAnnotationReader().readAnnotation(getOpfFile(file));
//        } catch (BookReadingException e) {
//            return null;
//        } finally {
//            file.setCached(false);
//        }
//    }

//    private ZLFile getOpfFile(ZLFile oebFile) throws BookReadingException {
//        if ("opf".equals(oebFile.getExtension())) {
//            return oebFile;
//        }
//
//        final ZLFile containerInfoFile = ZLFile.createFile(oebFile, "META-INF/container.xml");
//        LogUtil.i18("containerInfoFile:" + containerInfoFile.getPath());
//        LogUtil.i18("containerInfoFile:" + containerInfoFile.getUrl());
//        LogUtil.i18("containerInfoFile:" + containerInfoFile.getLongName());
//        if (containerInfoFile.exists()) {
//            final ContainerFileReader reader = new ContainerFileReader();
//            reader.readQuietly(containerInfoFile);
//            final String opfPath = reader.getRootPath();
//            if (opfPath != null) {
//                return ZLFile.createFile(oebFile, opfPath);
//            }
//        }
//
//        for (ZLFile child : oebFile.children()) {
//            if (child.getExtension().equals("opf")) {
//                return child;
//            }
//        }
//        throw new BookReadingException("opfFileNotFound", oebFile);
//    }

    @Override
    public int priority() {
        return 0;
    }
}
