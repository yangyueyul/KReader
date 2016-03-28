package com.koolearn.klibrary.core.image;

import com.koolearn.android.util.LogInfo;

public abstract class ZLImageProxy implements ZLImage {
	public interface Synchronizer {
		void startImageLoading(ZLImageProxy image, Runnable postAction);
		void synchronize(ZLImageProxy image, Runnable postAction);
	}

	private volatile boolean myIsSynchronized;

	public final boolean isSynchronized() {
		LogInfo.i("image");

		if (myIsSynchronized && isOutdated()) {
			myIsSynchronized = false;
		}
		return myIsSynchronized;
	}

	protected final void setSynchronized() {
		myIsSynchronized = true;
	}

	protected boolean isOutdated() {
		return false;
	}

	public void startSynchronization(Synchronizer synchronizer, Runnable postAction) {
		LogInfo.i("image");

		synchronizer.startImageLoading(this, postAction);
	}

	public static enum SourceType {
		FILE,
		NETWORK,
		SERVICE;
	};
	public abstract SourceType sourceType();

	public abstract ZLImage getRealImage();
	public abstract String getId();

	@Override
	public String toString() {
		return getClass().getName() + "[" + getId() + "; synchronized=" + isSynchronized() + "]";
	}
}
