package com.koolearn.android.kooreader.tree;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import com.koolearn.android.kooreader.util.AndroidImageSynchronizer;
import com.koolearn.android.util.LogInfo;
import com.koolearn.android.util.LogUtil;
import com.koolearn.android.util.OrientationUtil;
import com.koolearn.android.util.UIMessageUtil;
import com.koolearn.android.util.UIUtil;
import com.koolearn.klibrary.ui.android.library.UncaughtExceptionHandler;
import com.koolearn.kooreader.tree.KooTree;
import com.kooreader.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//y 选择本地文件的时候调用
public abstract class TreeActivity<T extends KooTree> extends ListActivity {
    private static final String OPEN_TREE_ACTION = "android.kooreader.action.OPEN_TREE";

    public static final String TREE_KEY_KEY = "TreeKey";
    public static final String SELECTED_TREE_KEY_KEY = "SelectedTreeKey";
    public static final String HISTORY_KEY = "HistoryKey";

    public final AndroidImageSynchronizer ImageSynchronizer = new AndroidImageSynchronizer(this);

    private T myCurrentTree;
    private KooTree.Key myCurrentKey;
    private final List<KooTree.Key> myHistory =
            Collections.synchronizedList(new ArrayList<KooTree.Key>());

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        OrientationUtil.setOrientation(this, getIntent());
    }

    @Override
    protected void onDestroy() {
        ImageSynchronizer.clear();

        super.onDestroy();
    }

    public TreeAdapter getTreeAdapter() {
        return (TreeAdapter) super.getListAdapter();
    }

    protected T getCurrentTree() {
        return myCurrentTree;
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        LogInfo.I1("");
        OrientationUtil.setOrientation(this, intent);
        if (OPEN_TREE_ACTION.equals(intent.getAction())) {
            runOnUiThread(new Runnable() {
                public void run() {
                    init(intent);
                }
            });
        } else {
            super.onNewIntent(intent);
        }
    }

    protected abstract T getTreeByKey(KooTree.Key key);

    public abstract boolean isTreeSelected(KooTree tree);

    protected boolean isTreeInvisible(KooTree tree) {
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        LogUtil.i3("onKeyDown");

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            KooTree parent = null;
            synchronized (myHistory) {
                while (parent == null && !myHistory.isEmpty()) {
                    parent = getTreeByKey(myHistory.remove(myHistory.size() - 1));
                }
            }
            if (parent == null && myCurrentTree != null) {
                parent = myCurrentTree.Parent;
            }
            if (parent != null && !isTreeInvisible(parent)) {
                openTree(parent, myCurrentTree, false);
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    // TODO: change to protected
    public void openTree(final KooTree tree) {
        openTree(tree, null, true);
    }

    public void clearHistory() {
        runOnUiThread(new Runnable() {
            public void run() {
                myHistory.clear();
            }
        });
    }

    protected void onCurrentTreeChanged() {
    }

    private void openTree(final KooTree tree, final KooTree treeToSelect, final boolean storeInHistory) {
        LogUtil.i3("openTree");
        switch (tree.getOpeningStatus()) {
            case WAIT_FOR_OPEN:
            case ALWAYS_RELOAD_BEFORE_OPENING:
                LogUtil.i3("openTree");
                final String messageKey = tree.getOpeningStatusMessage();
                if (messageKey != null) {
                    LogUtil.i3("openTree");
                    UIUtil.createExecutor(TreeActivity.this, messageKey).execute(
                            new Runnable() {
                                public void run() {
                                    LogUtil.i3("openTree");

                                    tree.waitForOpening();
                                }
                            },
                            new Runnable() {
                                public void run() {
                                    LogUtil.i3("openTree");

                                    openTreeInternal(tree, treeToSelect, storeInHistory);
                                }
                            }
                    );
                } else {
                    LogUtil.i3("openTree");
                    tree.waitForOpening();
                    openTreeInternal(tree, treeToSelect, storeInHistory);
                }
                break;
            default:
                openTreeInternal(tree, treeToSelect, storeInHistory);
                break;
        }
    }

    private void setTitleAndSubtitle(Pair<String, String> pair) {
        if (pair.Second != null) {
            setTitle(pair.First + " - " + pair.Second);
        } else {
            setTitle(pair.First);
        }
    }

    protected void init(Intent intent) {
        final KooTree.Key key = (KooTree.Key) intent.getSerializableExtra(TREE_KEY_KEY);
        final KooTree.Key selectedKey = (KooTree.Key) intent.getSerializableExtra(SELECTED_TREE_KEY_KEY);
        myCurrentTree = getTreeByKey(key);
        // not myCurrentKey = key
        // because key might be null
        myCurrentKey = myCurrentTree.getUniqueKey();
        final TreeAdapter adapter = getTreeAdapter();
        adapter.replaceAll(myCurrentTree.subtrees(), false);
        setTitleAndSubtitle(myCurrentTree.getTreeTitle());
        final KooTree selectedTree =
                selectedKey != null ? getTreeByKey(selectedKey) : adapter.getFirstSelectedItem();
        final int index = adapter.getIndex(selectedTree);
        if (index != -1) {
            setSelection(index);
            getListView().post(new Runnable() {
                public void run() {
                    setSelection(index);
                }
            });
        }
        myHistory.clear();
        final ArrayList<KooTree.Key> history =
                (ArrayList<KooTree.Key>) intent.getSerializableExtra(HISTORY_KEY);
        if (history != null) {
            myHistory.addAll(history);
        }
        onCurrentTreeChanged();
    }

    private void openTreeInternal(KooTree tree, KooTree treeToSelect, boolean storeInHistory) {
        switch (tree.getOpeningStatus()) {
            case READY_TO_OPEN:
            case ALWAYS_RELOAD_BEFORE_OPENING:
                if (storeInHistory && !myCurrentKey.equals(tree.getUniqueKey())) {
                    myHistory.add(myCurrentKey);
                }
                onNewIntent(new Intent(this, getClass())
                                .setAction(OPEN_TREE_ACTION)
                                .putExtra(TREE_KEY_KEY, tree.getUniqueKey())
                                .putExtra(
                                        SELECTED_TREE_KEY_KEY,
                                        treeToSelect != null ? treeToSelect.getUniqueKey() : null
                                )
                                .putExtra(HISTORY_KEY, new ArrayList<KooTree.Key>(myHistory))
                );
                break;
            case CANNOT_OPEN:
                UIMessageUtil.showErrorMessage(TreeActivity.this, tree.getOpeningStatusMessage());
                break;
        }
    }
}
