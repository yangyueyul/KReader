package com.koolearn.klibrary.core.encodings;

import com.koolearn.android.util.LogInfo;

import java.nio.charset.Charset;

public final class JavaEncodingCollection extends FilteredEncodingCollection {
	private volatile static JavaEncodingCollection ourInstance;

	public static JavaEncodingCollection Instance() {
		LogInfo.i("encodings");

		if (ourInstance == null) {
			ourInstance = new JavaEncodingCollection();
		}
		return ourInstance;
	}

	private JavaEncodingCollection() {
		super();
	}

	@Override
	public boolean isEncodingSupported(String name) {
		LogInfo.i("encodings");

		try {
			return Charset.forName(name) != null;
		} catch (Exception e) {
			return false;
		}
	}
}
