package com.koolearn.klibrary.core.fonts;

import com.koolearn.android.util.LogInfo;
import com.kooreader.util.ComparisonUtil;

import java.util.HashMap;
import java.util.Map;

public final class FontEntry {
	private static Map<String,FontEntry> ourSystemEntries = new HashMap<String,FontEntry>();
	//y 根据"词"来 对每个词的字体进行绘制
	public static FontEntry systemEntry(String family) {
		LogInfo.i("fonts"+family);

		synchronized(ourSystemEntries) {
			FontEntry entry = ourSystemEntries.get(family);
			if (entry == null) {
				entry = new FontEntry(family);
				ourSystemEntries.put(family, entry);
			}
			return entry;
		}
	}

	public final String Family;
	private final FileInfo[] myFileInfos;

	public FontEntry(String family, FileInfo normal, FileInfo bold, FileInfo italic, FileInfo boldItalic) {
//		LogInfo.i("fonts"+family+":"+normal.toString()+":"+bold.toString()+":"+italic.toString()+":"+boldItalic.toString());

		Family = family;
		myFileInfos = new FileInfo[4];
		myFileInfos[0] = normal;
		myFileInfos[1] = bold;
		myFileInfos[2] = italic;
		myFileInfos[3] = boldItalic;
	}

	FontEntry(String family) {
		LogInfo.i("fonts"+family);

		Family = family;
		myFileInfos = null;
	}

	public boolean isSystem() {
		LogInfo.i("fonts");

		return myFileInfos == null;
	}

	public FileInfo fileInfo(boolean bold, boolean italic) {
		LogInfo.i("fonts"+bold+italic);

		return myFileInfos != null ? myFileInfos[(bold ? 1 : 0) + (italic ? 2 : 0)] : null;
	}

	@Override
	public String toString() {
		LogInfo.i("fonts");

		final StringBuilder builder = new StringBuilder("FontEntry[");
		builder.append(Family);
		if (myFileInfos != null) {
			for (int i = 0; i < 4; ++i) {
				final FileInfo info = myFileInfos[i];
				builder.append(";").append(info != null ? info.Path : null);
			}
		}
		return builder.append("]").toString();
	}

	@Override
	public boolean equals(Object other) {
		LogInfo.i("fonts"+other.toString());

		if (other == this) {
			return true;
		}
		if (!(other instanceof FontEntry)) {
			return false;
		}
		final FontEntry entry = (FontEntry)other;
		if (!ComparisonUtil.equal(Family, entry.Family)) {
			return false;
		}
		if (myFileInfos == null) {
			return entry.myFileInfos == null;
		}
		if (entry.myFileInfos == null) {
			return false;
		}
		for (int i = 0; i < myFileInfos.length; ++i) {
			if (!ComparisonUtil.equal(myFileInfos[i], entry.myFileInfos[i])) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		return ComparisonUtil.hashCode(Family);
	}
}
