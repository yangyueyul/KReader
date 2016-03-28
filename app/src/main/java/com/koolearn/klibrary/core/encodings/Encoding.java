package com.koolearn.klibrary.core.encodings;

import com.koolearn.android.util.LogInfo;

// 用于读取enconding/Encodings.xml内的编码
public final class Encoding {
	public final String Family;
	public final String Name;
	public final String DisplayName;

	Encoding(String family, String name, String displayName) {
		LogInfo.i("encodings");

		Family = family;
		Name = name;
		DisplayName = displayName;
	}

	@Override
	public boolean equals(Object other) {
		LogInfo.i("encodings");

		return other instanceof Encoding && Name.equals(((Encoding)other).Name);
	}

	@Override
	public int hashCode() {
		LogInfo.i("encodings");

		return Name.hashCode();
	}

	public EncodingConverter createConverter() {
		LogInfo.i("encodings");

		return new EncodingConverter(Name);
	}
}
