/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package com.koolearn.kooreader.kooreader;

import com.koolearn.klibrary.text.view.ZLTextRegion;
import com.koolearn.klibrary.text.view.ZLTextWordRegionSoul;

class MoveCursorAction extends KooAction {
	private final KooView.Direction myDirection;

	MoveCursorAction(KooReaderApp fbreader, KooView.Direction direction) {
		super(fbreader);
		myDirection = direction;
	}

	@Override
	protected void run(Object... params) {
		final KooView kooView = Reader.getTextView();
		ZLTextRegion region = kooView.getOutlinedRegion();
		final ZLTextRegion.Filter filter =
			(region != null && region.getSoul() instanceof ZLTextWordRegionSoul)
				|| Reader.MiscOptions.NavigateAllWords.getValue()
					? ZLTextRegion.AnyRegionFilter : ZLTextRegion.ImageOrHyperlinkFilter;
		region = kooView.nextRegion(myDirection, filter);
		if (region != null) {
			kooView.outlineRegion(region);
		} else {
			switch (myDirection) {
				case down:
					kooView.turnPage(true, KooView.ScrollingMode.SCROLL_LINES, 1);
					break;
				case up:
					kooView.turnPage(false, KooView.ScrollingMode.SCROLL_LINES, 1);
					break;
			}
		}

		Reader.getViewWidget().reset();
		Reader.getViewWidget().repaint();
	}
}
