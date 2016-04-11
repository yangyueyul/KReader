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

import android.content.Intent;
import android.os.Parcelable;
import android.view.View;

import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.OnClickWrapper;
import com.koolearn.android.kooreader.api.KooReaderIntents;
import com.koolearn.android.kooreader.bookmark.EditBookmarkActivity;
import com.koolearn.android.util.OrientationUtil;
import com.koolearn.klibrary.core.resources.ZLResource;
import com.koolearn.kooreader.book.Bookmark;
import com.koolearn.kooreader.kooreader.KooReaderApp;

public class SelectionBookmarkAction extends KooAndroidAction {
    SelectionBookmarkAction(KooReader baseApplication, KooReaderApp kooreader) {
        super(baseApplication, kooreader);
    }

    @Override
    protected void run(Object... params) {
        final Bookmark bookmark;
        if (params.length != 0) {
            bookmark = (Bookmark) params[0];
        } else {
            bookmark = Reader.addSelectionBookmark();
        }
        if (bookmark == null) {
            return;
        }

        final SuperActivityToast toast =
                new SuperActivityToast(BaseActivity, SuperToast.Type.BUTTON);
        toast.setText(bookmark.getText());
        toast.setDuration(SuperToast.Duration.EXTRA_LONG);
        toast.setButtonIcon(
                android.R.drawable.ic_menu_edit,
                ZLResource.resource("dialog").getResource("button").getResource("edit").getValue()
        );
        toast.setOnClickWrapper(new OnClickWrapper("bkmk", new SuperToast.OnClickListener() {
            @Override
            public void onClick(View view, Parcelable token) {
                final Intent intent =
                        new Intent(BaseActivity.getApplicationContext(), EditBookmarkActivity.class);
                intent.putExtra("bgColor", Reader.ViewOptions.getColorProfile().WallpaperOption.getValue());
                KooReaderIntents.putBookmarkExtra(intent, bookmark);
                OrientationUtil.startActivityForResult(BaseActivity, intent, 6);
            }
        }));
        BaseActivity.showToast(toast);
    }
}
