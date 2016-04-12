package com.koolearn.klibrary.core.view;

import com.koolearn.klibrary.core.library.ZLibrary;
import com.koolearn.klibrary.core.util.ZLColor;

public abstract class SelectionCursor {
	public enum Which {
		Left,
		Right
	}

	public static void draw(ZLPaintContext context, Which which, int x, int y, ZLColor color) {
		context.setFillColor(color);
		final int dpi = ZLibrary.Instance().getDisplayDPI();
		final int unit = dpi / 160;
		final int xCenter = which == Which.Left ? x - unit - 1 : x + unit + 1;
		context.fillRectangle(xCenter - unit, y + dpi / 10, xCenter + unit, y - dpi / 10);
		if (which == Which.Left) {
			context.fillCircle(xCenter, y - dpi / 10, unit * 4);
		} else {
			context.fillCircle(xCenter, y + dpi / 10, unit * 4);
		}
	}
}
