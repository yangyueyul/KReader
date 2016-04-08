/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;

import com.koolearn.android.kooreader.httpd.DataUtil;
import com.koolearn.android.util.UIMessageUtil;
import com.koolearn.klibrary.core.util.MimeType;
import com.koolearn.klibrary.text.view.ZLTextVideoElement;
import com.koolearn.klibrary.text.view.ZLTextVideoRegionSoul;
import com.koolearn.kooreader.kooreader.KooReaderApp;

class OpenVideoAction extends KooAndroidAction {
	OpenVideoAction(KooReader baseActivity, KooReaderApp kooreader) {
		super(baseActivity, kooreader);
	}

	@Override
	protected void run(Object... params) {
		if (params.length != 1 || !(params[0] instanceof ZLTextVideoRegionSoul)) {
			return;
		}

		final ZLTextVideoElement element = ((ZLTextVideoRegionSoul)params[0]).VideoElement;
		boolean playerNotFound = false;
		for (MimeType mimeType : MimeType.TYPES_VIDEO) {
			final String mime = mimeType.toString();
			final String path = element.Sources.get(mime);
			if (path == null) {
				continue;
			}
			final Intent intent = new Intent(Intent.ACTION_VIEW);
			final String url = DataUtil.buildUrl(BaseActivity.DataConnection, mime, path);
			if (url == null) {
				UIMessageUtil.showErrorMessage(BaseActivity, "videoServiceNotWorking");
				return;
			}
			intent.setDataAndType(Uri.parse(url), mime);
			try {
				BaseActivity.startActivity(intent);
				return;
			} catch (ActivityNotFoundException e) {
				playerNotFound = true;
				continue;
			}
		}
		if (playerNotFound) {
			UIMessageUtil.showErrorMessage(BaseActivity, "videoPlayerNotFound");
		}
	}
}
