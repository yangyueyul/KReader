/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package com.koolearn.kooreader.tree;

import com.koolearn.android.util.LogInfo;
import com.koolearn.android.util.LogUtil;
import com.kooreader.util.ComparisonUtil;
import com.kooreader.util.Pair;
import com.koolearn.klibrary.core.image.ZLImage;
import com.koolearn.klibrary.core.tree.ZLTree;

import java.io.Serializable;

public abstract class KooTree extends ZLTree<KooTree> implements Comparable<KooTree> {
	public static class Key implements Serializable {
		private static final long serialVersionUID = -6500763093522202052L;

		public final Key Parent;
		public final String Id;

		private Key(Key parent, String id) {
			//y 目录相关
			LogUtil.i3("parent"+parent+":"+id);
			if (id == null) {
				throw new IllegalArgumentException("KooTree.Key string id must be non-null");
			}
			Parent = parent;
			Id = id;
		}

		@Override
		public boolean equals(Object other) {
			if (other == this) {
				return true;
			}
			if (!(other instanceof Key)) {
				return false;
			}
			final Key key = (Key)other;
			return Id.equals(key.Id) && ComparisonUtil.equal(Parent, key.Parent);
		}

		@Override
		public int hashCode() {
			return Id.hashCode();
		}

		@Override
		public String toString() {
			return Parent == null ? Id : Parent.toString() + " :: " + Id;
		}
	}

	public static enum Status {
		READY_TO_OPEN,
		WAIT_FOR_OPEN,
		ALWAYS_RELOAD_BEFORE_OPENING,
		CANNOT_OPEN
	};

	private ZLImage myCover;
	private boolean myCoverRequested;
	private Key myKey;

	protected KooTree() {
		super();
	}

	protected KooTree(KooTree parent) {
		super(parent);
	}

	protected KooTree(KooTree parent, int position) {
		super(parent, position);
	}

	public final Key getUniqueKey() {
		LogInfo.i("Kootree");

		if (myKey == null) {
			myKey = new Key(Parent != null ? Parent.getUniqueKey() : null, getStringId());
		}
		return myKey;
	}

	/**
	 * Returns id used as a part of unique key above. This string must be not null
	 * and be different for all children of same tree
	 */
	protected abstract String getStringId();

	public KooTree getSubtree(String id) {
		for (KooTree tree : subtrees()) {
			if (id.equals(tree.getStringId())) {
				return tree;
			}
		}
		return null;
	}

	public int indexOf(KooTree tree) {
		return subtrees().indexOf(tree);
	}

	public abstract String getName();

	public Pair<String,String> getTreeTitle() {
		return new Pair(getName(), null);
	}

	protected String getSortKey() {
		LogInfo.i("Kootree");

		final String sortKey = getName();
		if (sortKey == null ||
			sortKey.length() <= 1 ||
			Character.isLetterOrDigit(sortKey.charAt(0))) {
			return sortKey;
		}

		for (int i = 1; i < sortKey.length(); ++i) {
			if (Character.isLetterOrDigit(sortKey.charAt(i))) {
				return sortKey.substring(i);
			}
		}
		return sortKey;
	}

	private static int compareStringsIgnoreCase(String s0, String s1) {
		LogInfo.i("Kootree");

		final int len = Math.min(s0.length(), s1.length());
		for (int i = 0; i < len; ++i) {
		  	char c0 = s0.charAt(i);
		  	char c1 = s1.charAt(i);
			if (c0 == c1) {
			  	continue;
			}
			c0 = Character.toLowerCase(c0);
			c1 = Character.toLowerCase(c1);
			if (c0 == c1) {
			  	continue;
			}
			return c0 - c1;
		}
		if (s0.length() > len) {
		  	return 1;
		}
		if (s0.length() > len) {
		  	return -1;
		}
		return 0;
	}

	public int compareTo(KooTree tree) {
		LogInfo.i("Kootree");

		final String key0 = getSortKey();
		final String key1 = tree.getSortKey();
		if (key0 == null) {
			return (key1 == null) ? 0 : -1;
		}
		if (key1 == null) {
			return 1;
		}
		final int diff = compareStringsIgnoreCase(key0, key1);
		return diff != 0 ? diff : getName().compareTo(tree.getName());
	}

	public abstract String getSummary();

	protected ZLImage createCover() {
		return null;
	}

	protected boolean canUseParentCover() {
		return true;
	}

	public final ZLImage getCover() {
		if (!myCoverRequested) {
			myCover = createCover();
			if (myCover == null && Parent != null && canUseParentCover()) {
				myCover = Parent.getCover();
			}
			myCoverRequested = true;
		}
		return myCover;
	}

	public Status getOpeningStatus() {
		return Status.READY_TO_OPEN;
	}

	public String getOpeningStatusMessage() {
		return null;
	}

	public void waitForOpening() {
	}
}
