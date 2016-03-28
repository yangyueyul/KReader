package com.koolearn.klibrary.core.filesystem.tar;

import com.koolearn.klibrary.core.filesystem.ZLArchiveEntryFile;
import com.koolearn.klibrary.core.filesystem.ZLFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class ZLTarEntryFile extends ZLArchiveEntryFile {
	public static List<ZLFile> archiveEntries(ZLFile archive) {
		try {
			InputStream stream = archive.getInputStream();
			if (stream != null) {
				LinkedList<ZLFile> entries = new LinkedList<ZLFile>();
				ZLTarHeader header = new ZLTarHeader();
				while (header.read(stream)) {
					if (header.IsRegularFile) {
						entries.add(new ZLTarEntryFile(archive, header.Name));
					}
					final int lenToSkip = (header.Size + 0x1ff) & -0x200;
					if (lenToSkip < 0) {
						break;
					}
					if (stream.skip(lenToSkip) != lenToSkip) {
						break;
					}
					header.erase();
				}
				stream.close();
				return entries;
			}
		} catch (IOException e) {
		}
		return Collections.emptyList();
	}

	public ZLTarEntryFile(ZLFile parent, String name) {
		super(parent, name);
	}

	@Override
	public boolean exists() {
		// TODO: optimize
		return myParent.exists() && archiveEntries(myParent).contains(this);
	}

	@Override
	public long size() {
		throw new RuntimeException("Not implemented yet.");
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new ZLTarInputStream(myParent.getInputStream(), myName);
	}
}
