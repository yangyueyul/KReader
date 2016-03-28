package com.koolearn.klibrary.core.filesystem;

import com.koolearn.android.util.LogInfo;
import com.koolearn.klibrary.core.drm.EncryptionMethod;
import com.koolearn.klibrary.core.drm.FileEncryptionInfo;
import com.koolearn.klibrary.core.drm.embedding.EmbeddingInputStream;
import com.koolearn.klibrary.core.util.InputStreamHolder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public abstract class ZLFile implements InputStreamHolder {
    private final static HashMap<String, ZLFile> ourCachedFiles = new HashMap<String, ZLFile>();

    protected interface ArchiveType {
        int NONE = 0;
        int COMPRESSED = 0x00ff;
        int ZIP = 0x0100;
        int TAR = 0x0200;
        int ARCHIVE = 0xff00;
    }

    ;

    private String myExtension;
    private String myShortName;
    protected int myArchiveType;
    private boolean myIsCached;

    /**
     * 设定myExtension
     */
    protected void init() {
        final String name = getLongName();
        final int index = name.lastIndexOf('.');
        myExtension = (index > 0) ? name.substring(index + 1).toLowerCase().intern() : "";
        myShortName = name.substring(name.lastIndexOf('/') + 1);
        int archiveType = ArchiveType.NONE;
        if (myExtension == "zip") {
            archiveType |= ArchiveType.ZIP;
        } else if (myExtension == "oebzip") {
            archiveType |= ArchiveType.ZIP;
        } else if (myExtension == "epub") {
            archiveType |= ArchiveType.ZIP;
        } else if (myExtension == "tar") {
            archiveType |= ArchiveType.TAR;
        }
        myArchiveType = archiveType;
    }

    /**
     * 递归调用
     * 第一次createFile,参数为null,           建立一个ZLPhysicalFile类,     /storage/emulated/0  文件夹
     * 第二次createFile,参数为ZLPhysicalFile，建立一个新的ZLPhysicalFile类, 新的ZLPhysicalFile类 epub文件
     */
    public static ZLFile createFile(ZLFile parent, String name) {
        ZLFile file = null;
        if (parent == null) { // parent==null,代表当前路径是最高路径
            ZLFile cached = ourCachedFiles.get(name);
            if (cached != null) {
                return cached;
            }
            if (name.length() == 0 || name.charAt(0) != '/') {
                return ZLResourceFile.createResourceFile(name);
            } else {
                return new ZLPhysicalFile(name);
            }
        } else if (parent instanceof ZLPhysicalFile && (parent.getParent() == null)) {
            // 路径 /storage/emulated/0 + / + 哈利波特.epub
            /**
             * 传入epub全路径
             * 在这里设置好file后 后面才能getInputStream
             */
            file = new ZLPhysicalFile(parent.getPath() + '/' + name); // 新建的ZLPhysicalFile类构造函数的参数会是epub文件的完整路径
        } else if (parent instanceof ZLResourceFile) {
            file = ZLResourceFile.createResourceFile((ZLResourceFile) parent, name);
        } else {
            file = ZLArchiveEntryFile.createArchiveEntryFile(parent, name);
        }

        if (!ourCachedFiles.isEmpty() && file != null) {
            ZLFile cached = ourCachedFiles.get(file.getPath());
            if (cached != null) {
                return cached;
            }
        }
        return file;
    }

    public static ZLFile createFileByUrl(String url) {
        if (url == null || !url.startsWith("file://")) {
            return null;
        }
        return createFileByPath(url.substring("file://".length()));
    }

    public static ZLFile createFileByPath(String path) {
        if (path == null) {
            return null;
        }
        ZLFile cached = ourCachedFiles.get(path);
        if (cached != null) {
            return cached;
        }

        int len = path.length();
        char first = len == 0 ? '*' : path.charAt(0);
        if (first != '/') {
            while (len > 1 && first == '.' && path.charAt(1) == '/') {
                path = path.substring(2);
                len -= 2;
                first = len == 0 ? '*' : path.charAt(0);
            }
            return ZLResourceFile.createResourceFile(path);
        }
        int index = path.lastIndexOf(':');
        if (index > 1) {
            final ZLFile archive = createFileByPath(path.substring(0, index));
            if (archive != null && archive.myArchiveType != 0) {
                return ZLArchiveEntryFile.createArchiveEntryFile(
                        archive, path.substring(index + 1)
                );
            }
        }
        return new ZLPhysicalFile(path);
    }

    public abstract long size();

    public abstract boolean exists();

    public abstract boolean isDirectory();

    public abstract String getPath();

    public abstract ZLFile getParent();

    public abstract ZLPhysicalFile getPhysicalFile();

    public abstract InputStream getInputStream() throws IOException;

    public long lastModified() {
        final ZLFile physicalFile = getPhysicalFile();
        return physicalFile != null ? physicalFile.lastModified() : 0;
    }

    public final InputStream getInputStream(FileEncryptionInfo encryptionInfo) throws IOException {
        if (encryptionInfo == null) {
            return getInputStream();
        }
        LogInfo.i("filesystem" + encryptionInfo);

        if (EncryptionMethod.EMBEDDING.equals(encryptionInfo.Method)) {
            return new EmbeddingInputStream(getInputStream(), encryptionInfo.ContentId);
        }

        throw new IOException("Encryption method " + encryptionInfo.Method + " is not supported");
    }

    public String getUrl() {
        return "file://" + getPath();
    }

    public boolean isReadable() {
        return true;
    }

    public final boolean isCompressed() {
        return (0 != (myArchiveType & ArchiveType.COMPRESSED));
    }

    public final boolean isArchive() {
        return (0 != (myArchiveType & ArchiveType.ARCHIVE));
    }

    public abstract String getLongName();

    public final String getShortName() {
        return myShortName;
    }

    public final String getExtension() {
        return myExtension;
    }

    protected List<ZLFile> directoryEntries() {
        return Collections.emptyList();
    }

    public final List<ZLFile> children() {
        if (exists()) {
            if (isDirectory()) {
                return directoryEntries();
            } else if (isArchive()) {
                return ZLArchiveEntryFile.archiveEntries(this);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public int hashCode() {
        return getPath().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ZLFile)) {
            return false;
        }
        return getPath().equals(((ZLFile) o).getPath());
    }

    @Override
    public String toString() {
        return "ZLFile [" + getPath() + "]";
    }

    protected boolean isCached() {
        return myIsCached;
    }

    public void setCached(boolean cached) {
        myIsCached = cached;
        if (cached) {
            ourCachedFiles.put(getPath(), this);
        } else {
            ourCachedFiles.remove(getPath());
            if (0 != (myArchiveType & ArchiveType.ZIP)) {
                ZLZipEntryFile.removeFromCache(this);
            }
        }
    }
}
