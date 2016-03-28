/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package com.koolearn.kooreader.library;

import com.koolearn.kooreader.book.IBookCollection;
import com.koolearn.kooreader.formats.PluginCollection;

public class RootTree extends LibraryTree {
	public RootTree(IBookCollection collection, PluginCollection pluginCollection) {
		super(collection, pluginCollection);

		new FileFirstLevelTree(this);
	}

	public LibraryTree getLibraryTree(LibraryTree.Key key) {
		if (key == null) {
			return null;
		}
		if (key.Parent == null) {
			return key.Id.equals(getUniqueKey().Id) ? this : null;
		}
		final LibraryTree parentTree = getLibraryTree(key.Parent);
		return parentTree != null ? (LibraryTree)parentTree.getSubtree(key.Id) : null;
	}

	@Override
	public String getName() {
		return resource().getValue();
	}

	@Override
	public String getSummary() {
		return resource().getValue();
	}

	@Override
	protected String getStringId() {
		return "@KooReaderLibraryRoot";
	}
}