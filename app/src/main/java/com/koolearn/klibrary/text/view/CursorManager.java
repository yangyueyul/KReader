package com.koolearn.klibrary.text.view;

import android.support.v4.util.LruCache;

import com.koolearn.klibrary.text.model.ZLTextModel;

final class CursorManager extends LruCache<Integer, ZLTextParagraphCursor> {
    private final ZLTextModel myModel;
    final ExtensionElementManager ExtensionManager; // 扩展元素管理器

    CursorManager(ZLTextModel model, ExtensionElementManager extManager) {
        super(200); // max 200 cursors in the cache
        myModel = model;
        ExtensionManager = extManager;
    }

    @Override
    protected ZLTextParagraphCursor create(Integer index) {
        return new ZLTextParagraphCursor(this, myModel, index);
    }
}