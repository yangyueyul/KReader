package com.amse.ys.zip;

import com.koolearn.android.util.LogUtil;

import java.io.*;

class ZipInputStream extends InputStream {
    private final ZipFile myParent;
    private final MyBufferedInputStream myBaseStream;
    private final Decompressor myDecompressor;
    private boolean myIsClosed;

    public ZipInputStream(ZipFile parent, LocalFileHeader header) throws IOException {
        myParent = parent; //
        myBaseStream = parent.getBaseStream();
        myBaseStream.setPosition(header.DataOffset); // 将epub文件的字节流定位到图片文件开始的位置
        myDecompressor = Decompressor.init(myBaseStream, header); // 根据不同的CompressionMethod
    }

    @Override
    public int available() throws IOException {
        return myDecompressor.available();
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        LogUtil.i21("read");
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || off + len > b.length) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }
        return myDecompressor.read(b, off, len);
    }

    @Override
    public int read() throws IOException {
        return myDecompressor.read();
    }

    @Override
    public void close() throws IOException {
        LogUtil.i1("amseys");

        if (!myIsClosed) {
            myIsClosed = true;
            myParent.storeBaseStream(myBaseStream);
            Decompressor.storeDecompressor(myDecompressor);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        LogUtil.i1("amseys");

        try {
            close();
        } finally {
            super.finalize();
        }
    }
}