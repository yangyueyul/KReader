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

import com.koolearn.klibrary.core.resources.ZLResource;
import com.koolearn.kooreader.book.Book;
import com.koolearn.kooreader.book.IBookCollection;
import com.koolearn.kooreader.formats.PluginCollection;
import com.koolearn.kooreader.tree.KooTree;

public abstract class LibraryTree extends KooTree {
	public static ZLResource resource() {
		return ZLResource.resource("library");
	}

	public final IBookCollection<Book> Collection;
	public final com.koolearn.kooreader.formats.PluginCollection PluginCollection;

	static final String ROOT_FILE = "fileTree";

	protected LibraryTree(IBookCollection collection, PluginCollection pluginCollection) {
		super();
		Collection = collection;
		PluginCollection = pluginCollection;
	}

	protected LibraryTree(LibraryTree parent) {
		super(parent);
		Collection = parent.Collection;
		PluginCollection = parent.PluginCollection;
	}

	protected LibraryTree(LibraryTree parent, int position) {
		super(parent, position);
		Collection = parent.Collection;
		PluginCollection = parent.PluginCollection;
	}

	public Book getBook() {
		return null;
	}

	public boolean containsBook(Book book) {
		return false;
	}

	public boolean isSelectable() {
		return true;
	}

	@Override
	public int compareTo(KooTree tree) {
		final int cmp = super.compareTo(tree);
		if (cmp == 0) {
			return getClass().getSimpleName().compareTo(tree.getClass().getSimpleName());
		}
		return cmp;
	}
}