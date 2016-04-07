package com.koolearn.android.kooreader.library;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.koolearn.android.kooreader.KooReader;
import com.koolearn.android.kooreader.api.KooReaderIntents;
import com.koolearn.android.kooreader.libraryService.BookCollectionShadow;
import com.koolearn.android.kooreader.tree.TreeActivity;
import com.koolearn.klibrary.ui.android.R;
import com.koolearn.kooreader.Paths;
import com.koolearn.kooreader.book.Book;
import com.koolearn.kooreader.book.BookEvent;
import com.koolearn.kooreader.book.IBookCollection;
import com.koolearn.kooreader.formats.PluginCollection;
import com.koolearn.kooreader.library.LibraryTree;
import com.koolearn.kooreader.library.RootTree;
import com.koolearn.kooreader.tree.KooTree;

//y 长按图书打开书籍/查看书籍信息
public class LibraryActivity extends TreeActivity<LibraryTree> implements IBookCollection.Listener<Book> {
    private final BookCollectionShadow myCollection = new BookCollectionShadow();
    private volatile RootTree myRootTree;
    private Book mySelectedBook;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mySelectedBook = KooReaderIntents.getBookExtra(getIntent(), myCollection);

        new LibraryTreeAdapter(this);
        setContentView(R.layout.listview_localbook);
//        View view = View.inflate(this, R.layout.local_book_head, null);
//        view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
//        getListView().addHeaderView(view);

//        getListView().setTextFilterEnabled(true);
//        getListView().setOnCreateContextMenuListener(this);

        deleteRootTree();

        myCollection.bindToService(this, new Runnable() {
            public void run() {
                setProgressBarIndeterminateVisibility(!myCollection.status().IsComplete);
                myRootTree = new RootTree(myCollection, PluginCollection.Instance(Paths.systemInfo(LibraryActivity.this)));
                myCollection.addListener(LibraryActivity.this);
                init(getIntent());
            }
        });

    }

    @Override
    protected LibraryTree getTreeByKey(KooTree.Key key) {
        return key != null ? myRootTree.getLibraryTree(key) : myRootTree;
    }

    private synchronized void deleteRootTree() {
        if (myRootTree != null) {
            myCollection.removeListener(this);
            myCollection.unbind();
            myRootTree = null;
        }
    }

    @Override
    protected void onDestroy() {
        deleteRootTree();
        super.onDestroy();
    }

    @Override
    public boolean isTreeSelected(KooTree tree) {
        final LibraryTree lTree = (LibraryTree) tree;
        return lTree.isSelectable() && lTree.containsBook(mySelectedBook);
    }

    @Override
    protected void onListItemClick(ListView listView, View view, int position, long rowId) {
        final LibraryTree tree = (LibraryTree) getTreeAdapter().getItem(position);
        final Book book = tree.getBook();
        if (book != null) {
            showBookInfo(book);
        } else {
            openTree(tree);
        }
    }

    private void showBookInfo(Book book) { //y 直接打开书籍，不再打开书籍详情 ---> BookInfoActivity
        KooReader.openBookActivity(LibraryActivity.this, book, null);
        overridePendingTransition(R.anim.tran_fade_in, R.anim.tran_fade_out);
//        final Intent intent = new Intent(getApplicationContext(), BookInfoActivity.class);
//        KooReaderIntents.putBookExtra(intent, book);
//        OrientationUtil.startActivity(this, intent);
    }

    public void onBookEvent(BookEvent event, Book book) {
    }

    public void onBuildEvent(IBookCollection.Status status) {
        setProgressBarIndeterminateVisibility(!status.IsComplete);
    }
}