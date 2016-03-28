package com.koolearn.klibrary.core.fonts;

import com.koolearn.android.util.LogInfo;
import com.kooreader.util.ComparisonUtil;

import com.koolearn.klibrary.core.drm.FileEncryptionInfo;

public final class FileInfo {
	public final String Path;
	public final FileEncryptionInfo EncryptionInfo;

	public FileInfo(String path, FileEncryptionInfo encryptionInfo) {
		LogInfo.i("fonts");

		Path = path;
		EncryptionInfo = encryptionInfo;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof FileInfo)) {
			return false;
		}
		final FileInfo oInfo = (FileInfo)other;
		LogInfo.i("fonts"+EncryptionInfo+oInfo.EncryptionInfo);
		return Path.equals(oInfo.Path) && ComparisonUtil.equal(EncryptionInfo, oInfo.EncryptionInfo);
	}

	@Override
	public int hashCode() {
		return Path.hashCode() + 23 * ComparisonUtil.hashCode(EncryptionInfo);
	}
}
