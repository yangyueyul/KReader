package com.koolearn.klibrary.core.filesystem;

import com.koolearn.android.util.LogUtil;
import com.koolearn.klibrary.core.filesystem.tar.ZLTarEntryFile;

import java.util.*;

public abstract class ZLArchiveEntryFile extends ZLFile {
    public static String normalizeEntryName(String entryName) {
        while (entryName.startsWith("./")) {
            entryName = entryName.substring(2);
        }
        while (true) {
            final int index = entryName.lastIndexOf("/./");
            if (index == -1) {
                break;
            }
            entryName = entryName.substring(0, index) + entryName.substring(index + 2);
        }
        while (true) {
            final int index = entryName.indexOf("/../");
            if (index <= 0) {
                break;
            }
            final int prevIndex = entryName.lastIndexOf('/', index - 1);
            if (prevIndex == -1) {
                entryName = entryName.substring(index + 4);
                break;
            }
            entryName = entryName.substring(0, prevIndex) + entryName.substring(index + 3);
        }
        return entryName;
    }

    public static ZLArchiveEntryFile createArchiveEntryFile(ZLFile archive, String entryName) {
        LogUtil.i21("" + archive.getPath() + ":" + entryName); // images里的所有图片 每次打开图片都走这
        if (archive == null) {
            return null;
        }
        entryName = normalizeEntryName(entryName);
        switch (archive.myArchiveType & ArchiveType.ARCHIVE) {
            case ArchiveType.ZIP:
                return new ZLZipEntryFile(archive, entryName);
            case ArchiveType.TAR:
                LogUtil.i21("" + archive.getPath() + ":" + entryName);
                return new ZLTarEntryFile(archive, entryName);
            default:
                return null;
        }
    }

    static List<ZLFile> archiveEntries(ZLFile archive) {
        switch (archive.myArchiveType & ArchiveType.ARCHIVE) {
            case ArchiveType.ZIP:
                return ZLZipEntryFile.archiveEntries(archive);
            case ArchiveType.TAR:
                return ZLTarEntryFile.archiveEntries(archive);
            default:
                return Collections.emptyList();
        }
    }

    protected final ZLFile myParent;
    protected final String myName;

    protected ZLArchiveEntryFile(ZLFile parent, String name) {
        myParent = parent;
        myName = name;
        init();
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public String getPath() {
        return myParent.getPath() + ":" + myName;
    }

    @Override
    public String getLongName() {
        return myName;
    }

    @Override
    public ZLFile getParent() {
        return myParent;
    }

    @Override
    public ZLPhysicalFile getPhysicalFile() {
        ZLFile ancestor = myParent;
        while ((ancestor != null) && !(ancestor instanceof ZLPhysicalFile)) {
            ancestor = ancestor.getParent();
        }
        return (ZLPhysicalFile) ancestor;
    }
}