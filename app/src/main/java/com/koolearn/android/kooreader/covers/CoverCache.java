package com.koolearn.android.kooreader.covers;

import android.graphics.Bitmap;

import com.koolearn.android.util.LogInfo;
import com.koolearn.kooreader.tree.KooTree;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

class CoverCache {
	static class NullObjectException extends Exception {
	}

	private static final Object NULL_BITMAP = new Object();

	volatile int HoldersCounter = 0;

	private final Map<KooTree.Key,Object> myBitmaps =
		Collections.synchronizedMap(new LinkedHashMap<KooTree.Key,Object>(10, 0.75f, true) {
			@Override
			protected boolean removeEldestEntry(Map.Entry<KooTree.Key,Object> eldest) {
				LogInfo.I("");
				return size() > 3 * HoldersCounter;
			}
		});

	Bitmap getBitmap(KooTree.Key key) throws NullObjectException {
		final Object bitmap = myBitmaps.get(key);
		if (bitmap == NULL_BITMAP) {
			throw new NullObjectException();
		}
		return (Bitmap)bitmap;
	}

	void putBitmap(KooTree.Key key, Bitmap bitmap) {
		myBitmaps.put(key, bitmap != null ? bitmap : NULL_BITMAP);
	}
}
