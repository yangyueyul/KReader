package com.koolearn.klibrary.text.view;

public final class ZLTextHyperlinkControlElement extends ZLTextControlElement {
	public final ZLTextHyperlink Hyperlink;

	ZLTextHyperlinkControlElement(byte kind, byte type, String id) {
		super(kind, true);
		Hyperlink = new ZLTextHyperlink(type, id);
	}
}
