package com.amse.ys.zip;

import com.koolearn.android.util.LogUtil;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

// 解压缩
public abstract class Decompressor {
	public Decompressor(MyBufferedInputStream is, LocalFileHeader header) {
	}

	/**
	 * byte b[] -- target buffer for bytes; might be null
	 */
	public abstract int read(byte b[], int off, int len) throws IOException;

	public abstract int read() throws IOException;

	protected Decompressor() {
		LogUtil.i1("amseys");

	}

	private static Queue<DeflatingDecompressor> ourDeflators = new LinkedList<DeflatingDecompressor>();

	static void storeDecompressor(Decompressor decompressor) {
		LogUtil.i1("amseys");

		if (decompressor instanceof DeflatingDecompressor) {
			synchronized (ourDeflators) {
				ourDeflators.add((DeflatingDecompressor)decompressor);
			}
		}
	}

	static Decompressor init(MyBufferedInputStream is, LocalFileHeader header) throws IOException {
		switch (header.CompressionMethod) {
			case 0:
				return new NoCompressionDecompressor(is, header);
			case 8:
				synchronized (ourDeflators) {
					if (!ourDeflators.isEmpty()) {
						DeflatingDecompressor decompressor = ourDeflators.poll();
						decompressor.reset(is, header); // 将压缩状态多大 解压多大等信息传入
						return decompressor;
					}
				}
				return new DeflatingDecompressor(is, header);
			default:
				throw new ZipException("Unsupported method of compression");
		}
	}

	public int available() throws IOException {
		return -1;
	}
}
