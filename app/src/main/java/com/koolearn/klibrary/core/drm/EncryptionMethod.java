package com.koolearn.klibrary.core.drm;

import com.koolearn.android.util.LogInfo;

public abstract class EncryptionMethod {
	public static final String EMBEDDING = "embedding";

	public static boolean isSupported(String method) {
		LogInfo.i("drm");
		return EMBEDDING.equals(method);
	}
}
