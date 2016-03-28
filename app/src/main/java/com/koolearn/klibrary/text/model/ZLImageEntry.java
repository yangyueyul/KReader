package com.koolearn.klibrary.text.model;

import com.koolearn.klibrary.core.image.ZLImage;

import java.util.Map;

public final class ZLImageEntry {
	private final Map<String,ZLImage> myImageMap;
	public final String Id;
	public final short VOffset;
	public final boolean IsCover;

	ZLImageEntry(Map<String,ZLImage> imageMap, String id, short vOffset, boolean isCover) {
		myImageMap = imageMap;
		Id = id;
		VOffset = vOffset;
		IsCover = isCover;
	}

	public ZLImage getImage() {
		return myImageMap.get(Id);
	}
}
