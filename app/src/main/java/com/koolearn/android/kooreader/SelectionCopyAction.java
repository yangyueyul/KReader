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

package com.koolearn.android.kooreader;

import android.app.Application;
import android.text.ClipboardManager;

import com.koolearn.android.util.UIMessageUtil;
import com.koolearn.klibrary.core.resources.ZLResource;
import com.koolearn.kooreader.kooreader.KooReaderApp;
import com.koolearn.kooreader.kooreader.KooView;
import com.koolearn.kooreader.util.TextSnippet;

public class SelectionCopyAction extends KooAndroidAction {
	SelectionCopyAction(KooReader baseActivity, KooReaderApp kooreader) {
		super(baseActivity, kooreader);
	}

	@Override
	protected void run(Object... params) {
		final KooView kooView = Reader.getTextView();
		final TextSnippet snippet = kooView.getSelectedSnippet();
		if (snippet == null) {
			return;
		}

		final String text = snippet.getText();
		kooView.clearSelection();

		final ClipboardManager clipboard =
			(ClipboardManager)BaseActivity.getApplication().getSystemService(Application.CLIPBOARD_SERVICE);
		clipboard.setText(text);
		UIMessageUtil.showMessageText(
				BaseActivity,
				ZLResource.resource("selection").getResource("textInBuffer").getValue().replace("%s", clipboard.getText())
		);
	}
}
