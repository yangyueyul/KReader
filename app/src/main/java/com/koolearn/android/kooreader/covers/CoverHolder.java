package com.koolearn.android.kooreader.covers;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.koolearn.android.util.LogInfo;
import com.koolearn.klibrary.core.image.ZLImageProxy;
import com.koolearn.kooreader.tree.KooTree;

import java.util.concurrent.Future;

class CoverHolder {
	private final CoverManager myManager;
	final ImageView CoverView;
	volatile KooTree.Key Key;

	private CoverSyncRunnable coverSyncRunnable;
	Future<?> coverBitmapTask;
	private Runnable coverBitmapRunnable;

	CoverHolder(CoverManager manager, ImageView coverView, KooTree.Key key) {
		LogInfo.I("");
		myManager = manager;
		manager.setupCoverView(coverView);
		CoverView = coverView;
		Key = key;

		myManager.Cache.HoldersCounter++;
	}

	synchronized void setKey(KooTree.Key key) {
		LogInfo.I("");
		if (!Key.equals(key)) {
			if (coverBitmapTask != null) {
				coverBitmapTask.cancel(true);
				coverBitmapTask = null;
			}
			coverBitmapRunnable = null;
		}
		Key = key;
	}

	class CoverSyncRunnable implements Runnable {
		private final ZLImageProxy myImage;
		private final KooTree.Key myKey;

		CoverSyncRunnable(ZLImageProxy image) {
			myImage = image;
			synchronized (CoverHolder.this) {
				myKey = Key;
				coverSyncRunnable = this;
			}
		}

		public void run() {
			synchronized (CoverHolder.this) {
				try {
					if (coverSyncRunnable != this) {
						return;
					}
					if (!Key.equals(myKey)) {
						return;
					}
					if (!myImage.isSynchronized()) {
						return;
					}
					myManager.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							synchronized (CoverHolder.this) {
								if (Key.equals(myKey)) {
									myManager.setCoverForView(CoverHolder.this, myImage);
								}
							}
						}
					});
				} finally {
					if (coverSyncRunnable == this) {
						coverSyncRunnable = null;
					}
				}
			}
		}
	}

	class CoverBitmapRunnable implements Runnable {
		private final ZLImageProxy myImage;
		private final KooTree.Key myKey;

		CoverBitmapRunnable(ZLImageProxy image) {
			myImage = image;
			synchronized (CoverHolder.this) {
				myKey = Key;
				coverBitmapRunnable = this;
			}
		}

		public void run() {
			synchronized (CoverHolder.this) {
				if (coverBitmapRunnable != this) {
					return;
				}
			}
			try {
				if (!myImage.isSynchronized()) {
					return;
				}
				final Bitmap coverBitmap = myManager.getBitmap(myImage);
				if (coverBitmap == null) {
					// If bitmap is null, then there's no image
					// and CoverView already has a stock image
					myManager.Cache.putBitmap(myKey, null);
					return;
				}
				if (Thread.currentThread().isInterrupted()) {
					// We have been cancelled
					return;
				}
				myManager.Cache.putBitmap(myKey, coverBitmap);
				myManager.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						synchronized (CoverHolder.this) {
							if (Key.equals(myKey)) {
								CoverView.setImageBitmap(coverBitmap);
							}
						}
					}
				});
			} finally {
				synchronized (CoverHolder.this) {
					if (coverBitmapRunnable == this) {
						coverBitmapRunnable = null;
						coverBitmapTask = null;
					}
				}
			}
		}
	}
}
