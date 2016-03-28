package com.koolearn.klibrary.core.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// 本地File
public final class ZLPhysicalFile extends ZLFile {
	private final File myFile;

	ZLPhysicalFile(String path) {
		this(new File(path)); // 把书的路径传进来 new 一个File
		// 本地书籍查找时各个文件也是用的这个
	}

	/**
	 * 构造函数会利用这个完整路径创建一个ZLPhysicalFile类
	 * 正因这里设置了File类，所以才能在getInputStream方法中返回FileInputStream类。
	 */
	public ZLPhysicalFile(File file) {
		myFile = file;
		init(); // 初始化
	}

	@Override
	public boolean exists() {
		return myFile.exists();
	}

	@Override
	public long size() {
		return myFile.length();
	}

	@Override
	public long lastModified() {
		return myFile.lastModified();
	}

	private Boolean myIsDirectory;
	@Override
	public boolean isDirectory() {
		if (myIsDirectory == null) {
			myIsDirectory = myFile.isDirectory();
		}
		return myIsDirectory;
	}

	@Override
	public boolean isReadable() {
		return myFile.canRead();
	}

	public boolean delete() {
		return myFile.delete();
	}

	public File javaFile() {
		return myFile;
	}

	private String myPath;
	@Override
	public String getPath() {
		if (myPath == null) {
			try {
				myPath = myFile.getCanonicalPath();
			} catch (Exception e) {
				// should be never thrown
				myPath = myFile.getPath();
			}
		}
		return myPath;
	}

	@Override
	public String getLongName() {
		return isDirectory() ? getPath() : myFile.getName();
	}

	@Override
	public ZLFile getParent() {
		return isDirectory() ? null : new ZLPhysicalFile(myFile.getParent());
	}

	@Override
	public ZLPhysicalFile getPhysicalFile() {
		return this;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new FileInputStream(myFile);
	}

	protected List<ZLFile> directoryEntries() {
		File[] subFiles = myFile.listFiles();
		if (subFiles == null || subFiles.length == 0) {
			return Collections.emptyList();
		}

		ArrayList<ZLFile> entries = new ArrayList<ZLFile>(subFiles.length);
		for (File f : subFiles) {
			if (!f.getName().startsWith(".")) {
				entries.add(new ZLPhysicalFile(f));
			}
		}
		return entries;
	}
}
