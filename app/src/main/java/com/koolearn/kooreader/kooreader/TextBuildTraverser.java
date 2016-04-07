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

import com.koolearn.klibrary.text.view.ZLTextControlElement;
import com.koolearn.klibrary.text.view.ZLTextTraverser;
import com.koolearn.klibrary.text.view.ZLTextView;
import com.koolearn.klibrary.text.view.ZLTextWord;

class TextBuildTraverser extends ZLTextTraverser {
	protected final StringBuilder myBuffer = new StringBuilder();

	TextBuildTraverser(ZLTextView view) {
		super(view);
	}

	@Override
	protected void processWord(ZLTextWord word) {
		myBuffer.append(word.Data, word.Offset, word.Length);
	}

	@Override
	protected void processControlElement(ZLTextControlElement control) {
		// does nothing
	}

	@Override
	protected void processSpace() {
		myBuffer.append(" ");
	}

	@Override
	protected void processNbSpace() {
		myBuffer.append("\240");
	}

	@Override
	protected void processEndOfParagraph() {
		myBuffer.append("\n");
	}

	public String getText() {
		return myBuffer.toString();
	}
}