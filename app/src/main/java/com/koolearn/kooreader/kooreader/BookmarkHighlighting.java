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

import com.koolearn.klibrary.core.util.ZLColor;
import com.koolearn.klibrary.text.view.ZLTextFixedPosition;
import com.koolearn.klibrary.text.view.ZLTextPosition;
import com.koolearn.klibrary.text.view.ZLTextSimpleHighlighting;
import com.koolearn.klibrary.text.view.ZLTextView;
import com.koolearn.kooreader.book.HighlightingStyle;
import com.koolearn.kooreader.book.IBookCollection;

public final class BookmarkHighlighting extends ZLTextSimpleHighlighting {
	final IBookCollection Collection;
	final com.koolearn.kooreader.book.Bookmark Bookmark;

	private static ZLTextPosition startPosition(com.koolearn.kooreader.book.Bookmark bookmark) {
		return new ZLTextFixedPosition(bookmark.getParagraphIndex(), bookmark.getElementIndex(), 0);
	}

	private static ZLTextPosition endPosition(com.koolearn.kooreader.book.Bookmark bookmark) {
		final ZLTextPosition end = bookmark.getEnd();
		if (end != null) {
			return end;
		}
		// TODO: compute end and save bookmark
		return bookmark;
	}

	BookmarkHighlighting(ZLTextView view, IBookCollection collection, com.koolearn.kooreader.book.Bookmark bookmark) {
		super(view, startPosition(bookmark), endPosition(bookmark));
		Collection = collection;
		Bookmark = bookmark;
	}

	@Override
	public ZLColor getBackgroundColor() {
		final HighlightingStyle bmStyle = Collection.getHighlightingStyle(Bookmark.getStyleId());
		return bmStyle != null ? bmStyle.getBackgroundColor() : null;
	}

	@Override
	public ZLColor getForegroundColor() {
		final HighlightingStyle bmStyle = Collection.getHighlightingStyle(Bookmark.getStyleId());
		return bmStyle != null ? bmStyle.getForegroundColor() : null;
	}

	@Override
	public ZLColor getOutlineColor() {
		return null;
	}
}
