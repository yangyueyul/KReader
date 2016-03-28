package com.koolearn.klibrary.ui.android.image;

import com.koolearn.klibrary.core.image.ZLImage;
import com.koolearn.klibrary.core.image.ZLImageManager;
import com.koolearn.klibrary.core.image.ZLImageProxy;
import com.koolearn.klibrary.core.image.ZLStreamImage;

public final class ZLAndroidImageManager extends ZLImageManager {
	@Override
	public ZLAndroidImageData getImageData(ZLImage image) {
		if (image instanceof ZLImageProxy) {
			return getImageData(((ZLImageProxy)image).getRealImage());
		} else if (image instanceof ZLStreamImage) {
			return new InputStreamImageData((ZLStreamImage)image);
		} else if (image instanceof ZLBitmapImage) {
			return BitmapImageData.get((ZLBitmapImage)image);
		} else {
			// unknown image type or null
			return null;
		}
	}

	private ZLAndroidImageLoader myLoader;

	public void startImageLoading(ZLImageProxy.Synchronizer syncronizer, ZLImageProxy image, Runnable postLoadingRunnable) {
		if (myLoader == null) {
			myLoader = new ZLAndroidImageLoader();
		}
		myLoader.startImageLoading(syncronizer, image, postLoadingRunnable);
	}
}
