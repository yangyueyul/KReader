package com.koolearn.klibrary.text.hyphenation;

// 根据断字判断是哪种语言
public final class ZLTextHyphenationInfo {
	final boolean[] Mask;

	public ZLTextHyphenationInfo(int length) {
		Mask = new boolean[length - 1];
	}

	public boolean isHyphenationPossible(int position) {
		return (position < Mask.length && Mask[position]);
	}
}
