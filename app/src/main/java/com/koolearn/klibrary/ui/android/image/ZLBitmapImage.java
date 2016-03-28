package com.koolearn.klibrary.ui.android.image;

import android.graphics.Bitmap;

import com.koolearn.klibrary.core.image.ZLImage;

public class ZLBitmapImage implements ZLImage {
	private final Bitmap myBitmap;

	public ZLBitmapImage(Bitmap bitmap) {
		myBitmap = bitmap;
	}

	public Bitmap getBitmap() {
		return myBitmap;
	}

	@Override
	public String getURI() {
		return "bitmap image";
	}
}
