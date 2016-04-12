/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package com.koolearn.android.kooreader.bookmark;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.koolearn.android.kooreader.api.KooReaderIntents;
import com.koolearn.android.kooreader.libraryService.BookCollectionShadow;
import com.koolearn.klibrary.ui.android.R;
import com.koolearn.kooreader.book.Bookmark;

public class EditBookmarkActivity extends Activity {
    private final BookCollectionShadow myCollection = new BookCollectionShadow();
    private Bookmark myBookmark;
    private LinearLayout li;
    private ImageButton mIBBookmark1;
    private ImageButton mIBBookmark2;
    private ImageButton mIBBookmark3;
    private EditText editor;
    private Button saveTextButton;
    private Button deleteButton;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.edit_bookmark);

        myBookmark = KooReaderIntents.getBookmarkExtra(getIntent());
        if (myBookmark == null) {
            finish();
            return;
        }

        li = (LinearLayout) findViewById(R.id.edit_bookmark_tabhost);
        initBgColor();
        mIBBookmark1 = (ImageButton) findViewById(R.id.ib_bookmark1);
        mIBBookmark2 = (ImageButton) findViewById(R.id.ib_bookmark2);
        mIBBookmark3 = (ImageButton) findViewById(R.id.ib_bookmark3);
        editor = (EditText) findViewById(R.id.edit_bookmark_text);
        editor.setText(myBookmark.getText());
        final int len = editor.getText().length();
        editor.setSelection(len, len);
        saveTextButton = (Button) findViewById(R.id.edit_bookmark_save_text_button);
        saveTextButton.setEnabled(false);
        deleteButton = (Button) findViewById(R.id.edit_bookmark_delete_button);
        initListener();
    }

    private void initBgColor() {
        String bgValue = getIntent().getStringExtra("bgColor");
        if (bgValue != null) {
            switch (bgValue) {
                case "wallpapers/bg_green.png":
                    li.setBackgroundResource(R.drawable.bg_green);
                    break;
                case "wallpapers/bg_grey.png":
                    li.setBackgroundResource(R.drawable.bg_grey);
                    break;
                case "wallpapers/bg_night.png":
                    li.setBackgroundResource(R.drawable.bg_white);
                    break;
                case "wallpapers/bg_vine_grey.png":
                    li.setBackgroundResource(R.drawable.bg_vine_grey);
                    break;
                case "wallpapers/bg_vine_white.png":
                    li.setBackgroundResource(R.drawable.bg_vine_white);
                    break;
                case "wallpapers/bg_white.png":
                    li.setBackgroundResource(R.drawable.bg_white);
                    break;
            }
        }
    }

    private void initListener() {
        mIBBookmark1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeColor(9846973, 1);
            }
        });
        mIBBookmark2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeColor(15559168, 2);

            }
        });
        mIBBookmark3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeColor(7648264, 3);
            }
        });
        saveTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myCollection.bindToService(EditBookmarkActivity.this, new Runnable() {
                    public void run() {
                        myBookmark.setText(editor.getText().toString());
                        myCollection.saveBookmark(myBookmark);
                        saveTextButton.setEnabled(false);
                    }
                });
            }
        });
        editor.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence sequence, int start, int before, int count) {
                final String originalText = myBookmark.getText();
                saveTextButton.setEnabled(!originalText.equals(editor.getText().toString()));
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myCollection.bindToService(EditBookmarkActivity.this, new Runnable() {
                    public void run() {
                        myCollection.deleteBookmark(myBookmark);
                        finish();
                    }
                });
            }
        });
    }

    private void changeColor(int mSelectColor, final int styleId) {
        Intent data = new Intent();
        data.putExtra("selectColor", mSelectColor);
        setResult(7, data);
        myCollection.bindToService(EditBookmarkActivity.this, new Runnable() {
            public void run() {
                myBookmark.setStyleId(styleId);
                myCollection.setDefaultHighlightingStyleId(styleId);
                myCollection.saveBookmark(myBookmark);
            }
        });
    }


    @Override
    protected void onDestroy() {
        myCollection.unbind();
        super.onDestroy();
    }
}
