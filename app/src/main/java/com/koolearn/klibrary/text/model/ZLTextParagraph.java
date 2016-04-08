package com.koolearn.klibrary.text.model;

/**
 * 抽象出的段落
 * Model的工作是将不同格式的数据,规整出自定义的结构想要的数据,并且保存
 */
public interface ZLTextParagraph {
	interface Entry {
		byte TEXT = 1; // 该段是文字 txt,epub中的<p></p>
		byte IMAGE = 2; // 解析到image,在保存缓存文件的时候前面就加 IMAGE
		byte CONTROL = 3; // 链接
		byte HYPERLINK_CONTROL = 4; // 超链接
		byte STYLE_CSS = 5;
		byte STYLE_OTHER = 6;
		byte STYLE_CLOSE = 7;
		byte FIXED_HSPACE = 8;
		byte RESET_BIDI = 9;
		byte AUDIO = 10;
		byte VIDEO = 11;
		byte EXTENSION = 12;
	}

	interface EntryIterator {
		byte getType();

		char[] getTextData();
		int getTextOffset();
		int getTextLength();

		byte getControlKind();
		boolean getControlIsStart();

		byte getHyperlinkType();
		String getHyperlinkId();

		ZLImageEntry getImageEntry();
		ZLVideoEntry getVideoEntry();
		ExtensionEntry getExtensionEntry();
		ZLTextStyleEntry getStyleEntry();

		short getFixedHSpaceLength();

		boolean next();
	}

	public EntryIterator iterator();

	interface Kind {
		byte TEXT_PARAGRAPH = 0;
		//byte TREE_PARAGRAPH = 1;
		byte EMPTY_LINE_PARAGRAPH = 2;
		byte BEFORE_SKIP_PARAGRAPH = 3;
		byte AFTER_SKIP_PARAGRAPH = 4;
		byte END_OF_SECTION_PARAGRAPH = 5;
		byte PSEUDO_END_OF_SECTION_PARAGRAPH = 6;
		byte END_OF_TEXT_PARAGRAPH = 7;
		byte ENCRYPTED_SECTION_PARAGRAPH = 8;
	};

	byte getKind();
}