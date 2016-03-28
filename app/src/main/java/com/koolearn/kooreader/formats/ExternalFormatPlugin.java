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

package com.koolearn.kooreader.formats;

import com.koolearn.klibrary.core.encodings.AutoEncodingCollection;
import com.koolearn.kooreader.book.AbstractBook;
import com.koolearn.klibrary.core.filesystem.ZLFile;

public abstract class ExternalFormatPlugin extends FormatPlugin {
	protected ExternalFormatPlugin(com.koolearn.klibrary.core.util.SystemInfo systemInfo, String fileType) {
		super(systemInfo, fileType);
	}

	@Override
	public int priority() {
		return 10;
	}

	public abstract String packageName();

	@Override
	public PluginImage readCover(ZLFile file) {
		return new PluginImage(file, this);
	}

	@Override
	public AutoEncodingCollection supportedEncodings() {
		return new AutoEncodingCollection();
	}

	@Override
	public void detectLanguageAndEncoding(AbstractBook book) {
	}

	@Override
	public String toString() {
		return "ExternalFormatPlugin [" + supportedFileType() + "]";
	}
}
