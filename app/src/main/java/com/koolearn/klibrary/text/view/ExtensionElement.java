package com.koolearn.klibrary.text.view;

import com.koolearn.klibrary.core.view.ZLPaintContext;

public abstract class ExtensionElement extends ZLTextElement {
	protected abstract int getWidth();
	protected abstract int getHeight();

	protected abstract void draw(ZLPaintContext context, ZLTextElementArea area);
}
