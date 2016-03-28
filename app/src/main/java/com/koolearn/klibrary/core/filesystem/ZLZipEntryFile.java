package com.koolearn.klibrary.core.filesystem;

import com.amse.ys.zip.LocalFileHeader;
import com.amse.ys.zip.ZipFile;
import com.koolearn.android.util.LogUtil;

import java.io.*;
import java.util.*;

// 压缩File
final class ZLZipEntryFile extends ZLArchiveEntryFile {
    static List<ZLFile> archiveEntries(ZLFile archive) {
        try {
            final ZipFile zf = ZLZipEntryFile.getZipFile(archive);
            final Collection<LocalFileHeader> headers = zf.headers();
            if (!headers.isEmpty()) {
                ArrayList<ZLFile> entries = new ArrayList<ZLFile>(headers.size());
                for (LocalFileHeader h : headers) {
                    entries.add(new ZLZipEntryFile(archive, h.FileName));
                }
                return entries;
            }
        } catch (IOException e) {
        }
        return Collections.emptyList();
    }

    private static HashMap<ZLFile, ZipFile> ourZipFileMap = new HashMap<ZLFile, ZipFile>();

    private static ZipFile getZipFile(final ZLFile file) throws IOException {
        synchronized (ourZipFileMap) {
            LogUtil.i21(""); // 图片打开时用到
            ZipFile zf = file.isCached() ? ourZipFileMap.get(file) : null;
            if (zf == null) {
                zf = new ZipFile(file);
                if (file.isCached()) {
                    // 不走
                    ourZipFileMap.put(file, zf);
                }
            }
            return zf;
        }
    }

    static void removeFromCache(ZLFile file) {
        LogUtil.i21("" + file.getPath());
        ourZipFileMap.remove(file);
    }

    ZLZipEntryFile(ZLFile parent, String name) {
        super(parent, name);
    }

    @Override
    public boolean exists() {
        try {
            return myParent.exists() && getZipFile(myParent).entryExists(myName);
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public long size() {
        try {
            return getZipFile(myParent).getEntrySize(myName);
        } catch (IOException e) {
            return 0;
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        // epub路径，OEBPS/images/cover.jpg
        return getZipFile(myParent).getInputStream(myName);
    }
}