package com.koolearn.android.kooreader;

import android.view.ContextMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.koolearn.klibrary.core.tree.ZLTree;

import java.util.HashSet;


public abstract class ZLTreeAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, View.OnCreateContextMenuListener {
	private final ListView myParent;
	private final ZLTree<?> Root;
	private ZLTree<?>[] myItems;
	private final HashSet<ZLTree<?>> myOpenItems = new HashSet<ZLTree<?>>();

	protected ZLTreeAdapter(ListView parent, ZLTree<?> root) {
		myParent = parent;
		Root = root;
		myItems = new ZLTree[root.getSize() - 1];
		myOpenItems.add(root);

		parent.setAdapter(this);
		parent.setOnItemClickListener(this);
		parent.setOnCreateContextMenuListener(this);
	}

	protected final void openTree(ZLTree<?> tree) {
		if (tree == null) {
			return;
		}
		while (!myOpenItems.contains(tree)) {
			myOpenItems.add(tree);
			tree = tree.Parent;
		}
	}

	public final void expandOrCollapseTree(ZLTree<?> tree) {
		if (!tree.hasChildren()) {
			return;
		}
		if (isOpen(tree)) {
			myOpenItems.remove(tree);
		} else {
			myOpenItems.add(tree);
		}
		notifyDataSetChanged();
	}

	public final boolean isOpen(ZLTree<?> tree) {
		return myOpenItems.contains(tree);
	}

	public final void selectItem(ZLTree<?> tree) {
		if (tree == null) {
			return;
		}
		openTree(tree.Parent);
		int index = 0;
		while (true) {
			ZLTree<?> parent = tree.Parent;
			if (parent == null) {
				break;
			}
			for (ZLTree<?> sibling : parent.subtrees()) {
				if (sibling == tree) {
					break;
				}
				index += getCount(sibling);
			}
			tree = parent;
			++index;
		}
		if (index > 0) {
			myParent.setSelection(index - 1);
		}
		myParent.invalidateViews();
	}

	private int getCount(ZLTree<?> tree) {
		int count = 1;
		if (isOpen(tree)) {
			for (ZLTree<?> subtree : tree.subtrees()) {
				count += getCount(subtree);
			}
		}
		return count;
	}

	public final int getCount() {
		return getCount(Root) - 1;
	}

	private final int indexByPosition(int position, ZLTree<?> tree) {
		if (position == 0) {
			return 0;
		}
		--position;
		int index = 1;
		for (ZLTree<?> subtree : tree.subtrees()) {
			int count = getCount(subtree);
			if (count <= position) {
				position -= count;
				index += subtree.getSize();
			} else {
				return index + indexByPosition(position, subtree);
			}
		}
		throw new RuntimeException("That's impossible!!!");
	}

	public final ZLTree<?> getItem(int position) {
		final int index = indexByPosition(position + 1, Root) - 1;
		ZLTree<?> item = myItems[index];
		if (item == null) {
			item = Root.getTreeByParagraphNumber(index + 1);
			myItems[index] = item;
		}
		return item;
	}

	public final boolean areAllItemsEnabled() {
		return true;
	}

	public final boolean isEnabled(int position) {
		return true;
	}

	public final long getItemId(int position) {
		return indexByPosition(position + 1, Root);
	}

	protected boolean runTreeItem(ZLTree<?> tree) {
		if (!tree.hasChildren()) {
			return false;
		}
		expandOrCollapseTree(tree);
		return true;
	}

	public final void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		runTreeItem(getItem(position));
	}

	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
	}

	public abstract View getView(int position, View convertView, ViewGroup parent);

}
