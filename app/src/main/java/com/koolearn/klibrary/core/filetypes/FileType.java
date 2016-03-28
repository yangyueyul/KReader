package com.koolearn.klibrary.core.filetypes;

import com.koolearn.android.util.LogInfo;
import com.koolearn.klibrary.core.filesystem.ZLFile;
import com.koolearn.klibrary.core.util.MimeType;

import java.util.List;

public abstract class FileType {
	public final String Id;

	protected FileType(String id) {
		LogInfo.i("filetypes"+id);

		Id = id;
	}

	public abstract boolean acceptsFile(ZLFile file);

	public abstract List<MimeType> mimeTypes();
	public abstract MimeType mimeType(ZLFile file);
	public MimeType rawMimeType(ZLFile file) {
		return mimeType(file);
	}

	public abstract String defaultExtension(MimeType mime);
}
