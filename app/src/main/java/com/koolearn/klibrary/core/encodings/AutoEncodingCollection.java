package com.koolearn.klibrary.core.encodings;

import com.koolearn.android.util.LogInfo;

import java.util.List;
import java.util.Collections;

public final class AutoEncodingCollection extends EncodingCollection {

	private final Encoding myEncoding = new Encoding(null, "auto", "auto");

	public List<Encoding> encodings() {
		LogInfo.i("encodings");

		return Collections.singletonList(myEncoding);
	}

	public Encoding getEncoding(String alias) {
		LogInfo.i("encodings");

		return myEncoding;
	}

	public Encoding getEncoding(int code) {
		LogInfo.i("encodings");

		return myEncoding;
	}
}