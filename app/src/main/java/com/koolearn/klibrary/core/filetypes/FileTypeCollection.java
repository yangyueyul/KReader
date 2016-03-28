package com.koolearn.klibrary.core.filetypes;

import com.koolearn.android.util.LogInfo;
import com.koolearn.klibrary.core.filesystem.ZLFile;
import com.koolearn.klibrary.core.util.MimeType;

import java.util.*;

public class FileTypeCollection {
	public static final FileTypeCollection Instance = new FileTypeCollection();

	private final TreeMap<String,FileType> myTypes = new TreeMap<String,FileType>();

	private FileTypeCollection() {
		//y 能看到哪些类型的文件
		LogInfo.i("filetypes");
//		addType(new FileTypeFB2());
		addType(new FileTypeEpub());
//		addType(new FileTypeMobipocket());
//		addType(new FileTypeHtml());
		addType(new SimpleFileType("txt", "txt", MimeType.TYPES_TXT));
//		addType(new SimpleFileType("RTF", "rtf", MimeType.TYPES_RTF));
//		addType(new SimpleFileType("PDF", "pdf", MimeType.TYPES_PDF));
//		addType(new FileTypeDjVu());
//		addType(new FileTypeCBZ());
//		addType(new SimpleFileType("ZIP archive", "zip", Collections.singletonList(MimeType.APP_ZIP)));
		addType(new SimpleFileType("msdoc", "doc", MimeType.TYPES_DOC));
	}

	private void addType(FileType type) {
		LogInfo.i("filetypes"+type.toString());

		myTypes.put(type.Id.toLowerCase(), type);
	}

	public Collection<FileType> types() {
		LogInfo.i("filetypes"+myTypes.values());

		return myTypes.values();
	}

	public FileType typeById(String id) {
		LogInfo.i("filetypes");

		return myTypes.get(id.toLowerCase());
	}

	public FileType typeForFile(ZLFile file) {
		LogInfo.i("filetypes"+file.toString());

		for (FileType type : types()) {
			if (type.acceptsFile(file)) {
				return type;
			}
		}
		return null;
	}

	public FileType typeForMime(MimeType mime) {
		if (mime == null) {
			return null;
		}
		mime = mime.clean();
		for (FileType type : types()) {
			if (type.mimeTypes().contains(mime)) {
				return type;
			}
		}
		return null;
	}

	public MimeType mimeType(ZLFile file) {
		for (FileType type : types()) {
			final MimeType mime = type.mimeType(file);
			if (mime != MimeType.NULL) {
				return mime;
			}
		}
		return MimeType.UNKNOWN;
	}

	public MimeType rawMimeType(ZLFile file) {
		for (FileType type : types()) {
			final MimeType mime = type.rawMimeType(file);
			if (mime != MimeType.NULL) {
				return mime;
			}
		}
		return MimeType.UNKNOWN;
	}
}
