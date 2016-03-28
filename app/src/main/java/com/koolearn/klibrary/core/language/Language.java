package com.koolearn.klibrary.core.language;

import android.annotation.TargetApi;
import android.os.Build;

import com.koolearn.android.util.LogInfo;
import com.koolearn.klibrary.core.resources.ZLResource;

import java.text.Normalizer;

// 编码检测包
public class Language implements Comparable<Language> {
	public static final String ANY_CODE = "any";
	public static final String OTHER_CODE = "other";
	public static final String MULTI_CODE = "multi";
	public static final String SYSTEM_CODE = "system";

	private static enum Order {

		Before,
		Normal,
		After
	}

	public final String Code;
	public final String Name;
	private final String mySortKey;
	private final Order myOrder;

	public Language(String code) {

		this(code, ZLResource.resource("language"));
	}

	public Language(String code, ZLResource root) {
		//y 首次运行时加载所有，每次打开新书时都要调用，编辑语言时也调用

		LogInfo.i("language"+code+root.toString());
		Code = code;
		final ZLResource resource = root.getResource(code);
		Name = resource.hasValue() ? resource.getValue() : code;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			mySortKey = normalize(Name);
		} else {
			mySortKey = Name.toLowerCase();
		}
		if (SYSTEM_CODE.equals(code) || ANY_CODE.equals(code)) {
			myOrder = Order.Before;
		} else if (MULTI_CODE.equals(code) || OTHER_CODE.equals(code)) {
			myOrder = Order.After;
		} else {
			myOrder = Order.Normal;
		}
	}

	public int compareTo(Language other) {
		LogInfo.i("language"+other.toString());

		final int diff = myOrder.compareTo(other.myOrder);
		return diff != 0 ? diff : mySortKey.compareTo(other.mySortKey);
	}

	@Override
	public boolean equals(Object lang) {
		if (this == lang) {
			return true;
		}
		if (!(lang instanceof Language)) {
			return false;
		}
		return Code.equals(((Language)lang).Code);
	}

	@Override
	public int hashCode() {
		return Code.hashCode();
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private static String normalize(String s) {
		return Normalizer.normalize(s, Normalizer.Form.NFKD);
	}
}
