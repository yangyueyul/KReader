package com.koolearn.android.kooreader.tree;

import android.widget.BaseAdapter;

import com.koolearn.kooreader.tree.KooTree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class TreeAdapter extends BaseAdapter {
    private final TreeActivity myActivity;
    private final List<KooTree> myItems;

    protected TreeAdapter(TreeActivity activity) {
        myActivity = activity;
        myItems = Collections.synchronizedList(new ArrayList<KooTree>());
        activity.setListAdapter(this);
    }

    protected TreeActivity getActivity() {
        return myActivity;
    }

    public void remove(final KooTree item) {
        myActivity.runOnUiThread(new Runnable() {
            public void run() {
                myItems.remove(item);
                notifyDataSetChanged();
            }
        });
    }

    public void add(final KooTree item) {
        myActivity.runOnUiThread(new Runnable() {
            public void run() {
                myItems.add(item);
                notifyDataSetChanged();
            }
        });
    }

    public void add(final int index, final KooTree item) {
        myActivity.runOnUiThread(new Runnable() {
            public void run() {
                myItems.add(index, item);
                notifyDataSetChanged();
            }
        });
    }

    public void replaceAll(final Collection<KooTree> items, final boolean invalidateViews) {
        myActivity.runOnUiThread(new Runnable() {
            public void run() {
                synchronized (myItems) {
                    myItems.clear();
                    myItems.addAll(items);
                }
                notifyDataSetChanged();
                if (invalidateViews) {
                    myActivity.getListView().invalidateViews();
                }
            }
        });
    }

    public int getCount() {
        return myItems.size();
    }

    public KooTree getItem(int position) {
        return myItems.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public int getIndex(KooTree item) {
        return myItems.indexOf(item);
    }

    public KooTree getFirstSelectedItem() {
        synchronized (myItems) {
            for (KooTree t : myItems) {
                if (myActivity.isTreeSelected(t)) {
                    return t;
                }
            }
        }
        return null;
    }
}