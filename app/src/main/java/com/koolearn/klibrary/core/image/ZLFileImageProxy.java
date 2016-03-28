package com.koolearn.klibrary.core.image;

import com.koolearn.android.util.LogInfo;
import com.koolearn.klibrary.core.filesystem.ZLFile;

public abstract class ZLFileImageProxy extends ZLImageSimpleProxy {
	protected final ZLFile File;
	private volatile ZLFileImage myImage;

	protected ZLFileImageProxy(ZLFile file) {
		LogInfo.i("image");

		File = file;
	}

	@Override
	public final ZLFileImage getRealImage() {
		return myImage;
	}

	@Override
	public String getURI() {
		return "cover:" + File.getPath();
	}

	@Override
	public final synchronized void synchronize() {
		LogInfo.i("image");

		if (myImage == null) {
			myImage = retrieveRealImage();
			setSynchronized();
		}
	}

	@Override
	public SourceType sourceType() {
		return SourceType.FILE;
	}

	@Override
	public String getId() {
		return File.getPath();
	}

	protected abstract ZLFileImage retrieveRealImage();
}
