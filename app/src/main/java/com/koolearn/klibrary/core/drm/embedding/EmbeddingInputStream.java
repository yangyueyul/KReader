/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package com.koolearn.klibrary.core.drm.embedding;

import com.koolearn.android.util.LogInfo;
import com.koolearn.klibrary.core.util.InputStreamWithOffset;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

public class EmbeddingInputStream extends InputStreamWithOffset {
	private final byte[] myKey;

	public EmbeddingInputStream(InputStream base, String uid) throws IOException {
		super(base);
		try {
			LogInfo.i("drm");

			myKey = MessageDigest.getInstance("SHA").digest(uid.getBytes("utf-8"));
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	@Override
	public int read() throws IOException {
		LogInfo.i("drm");

		final int o = offset();
		final int bt = super.read();
		if (bt == -1) {
			return -1;
		}
		return o > 1040 ? bt : ((bt ^ myKey[o % myKey.length]) & 0xFF);
	}

	@Override
	public int read(byte[] buffer, int bOffset, int bCount) throws IOException {
		LogInfo.i("drm");

		final int o = offset();
		final int len = super.read(buffer, bOffset, bCount);
		if (o < 1040) {
			final int e = Math.min(1040 - o, len);
			for (int c = 0; c < e; ++c) {
				buffer[bOffset + c] ^= myKey[(o + c) % myKey.length];
			}
		}
		return len;
	}

	@Override
	public int read(byte[] buffer) throws IOException {
		LogInfo.i("drm");

		return read(buffer, 0, buffer.length);
	}
}
