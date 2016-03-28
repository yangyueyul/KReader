package com.koolearn.klibrary.core.drm;

import com.koolearn.android.util.LogInfo;
import com.kooreader.util.ComparisonUtil;

public class FileEncryptionInfo {
	public final String Uri;
	public final String Method;
	public final String Algorithm;
	public final String ContentId;

	public FileEncryptionInfo(String uri, String method, String algorithm, String contentId) {
		LogInfo.i("drm");

		Uri = uri;
		Method = method;
		Algorithm = algorithm;
		ContentId = contentId;
	}

	@Override
	public boolean equals(Object other) {
		LogInfo.i("drm");

		if (this == other) {
			return true;
		}
		if (!(other instanceof FileEncryptionInfo)) {
			return false;
		}
		final FileEncryptionInfo oInfo = (FileEncryptionInfo)other;
		return
			ComparisonUtil.equal(Uri, oInfo.Uri) &&
			ComparisonUtil.equal(Method, oInfo.Method) &&
			ComparisonUtil.equal(Algorithm, oInfo.Algorithm) &&
			ComparisonUtil.equal(ContentId, oInfo.ContentId);
	}

	@Override
	public int hashCode() {
		LogInfo.i("drm");
		return
			ComparisonUtil.hashCode(Uri) +
			23 * (ComparisonUtil.hashCode(Method) +
				  23 * (ComparisonUtil.hashCode(Algorithm) +
						23 * ComparisonUtil.hashCode(ContentId)));
	}
}