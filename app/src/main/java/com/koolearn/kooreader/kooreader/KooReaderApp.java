package com.koolearn.kooreader.kooreader;

import com.koolearn.klibrary.core.application.ZLApplication;
import com.koolearn.klibrary.core.application.ZLKeyBindings;
import com.koolearn.klibrary.core.drm.EncryptionMethod;
import com.koolearn.klibrary.core.drm.FileEncryptionInfo;
import com.koolearn.klibrary.core.util.RationalNumber;
import com.koolearn.klibrary.text.model.ZLTextModel;
import com.koolearn.klibrary.text.view.ZLTextFixedPosition;
import com.koolearn.klibrary.text.view.ZLTextParagraphCursor;
import com.koolearn.klibrary.text.view.ZLTextPosition;
import com.koolearn.klibrary.text.view.ZLTextView;
import com.koolearn.klibrary.text.view.ZLTextWordCursor;
import com.koolearn.kooreader.book.Book;
import com.koolearn.kooreader.book.BookEvent;
import com.koolearn.kooreader.book.BookUtil;
import com.koolearn.kooreader.book.Bookmark;
import com.koolearn.kooreader.book.BookmarkQuery;
import com.koolearn.kooreader.book.BookmarkUtil;
import com.koolearn.kooreader.book.IBookCollection;
import com.koolearn.kooreader.bookmodel.BookModel;
import com.koolearn.kooreader.bookmodel.TOCTree;
import com.koolearn.kooreader.formats.BookReadingException;
import com.koolearn.kooreader.formats.FormatPlugin;
import com.koolearn.kooreader.formats.PluginCollection;
import com.koolearn.kooreader.kooreader.options.ImageOptions;
import com.koolearn.kooreader.kooreader.options.MiscOptions;
import com.koolearn.kooreader.kooreader.options.PageTurningOptions;
import com.koolearn.kooreader.kooreader.options.ViewOptions;
import com.koolearn.kooreader.network.sync.SyncData;
import com.koolearn.kooreader.util.AutoTextSnippet;
import com.koolearn.kooreader.util.TextSnippet;
import com.kooreader.util.ComparisonUtil;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public final class KooReaderApp extends ZLApplication {
    public final MiscOptions MiscOptions = new MiscOptions();
    public final ImageOptions ImageOptions = new ImageOptions();
    public final ViewOptions ViewOptions = new ViewOptions();
    public final PageTurningOptions PageTurningOptions = new PageTurningOptions();
    private final ZLKeyBindings myBindings = new ZLKeyBindings();
    public final KooView BookTextView;
    public final KooView FootnoteView;
    private String myFootnoteModelId;
    public volatile BookModel Model;
    public volatile Book ExternalBook;
    private ZLTextPosition myJumpEndPosition;
    private Date myJumpTimeStamp;
    public final IBookCollection<Book> Collection;
    private final SyncData mySyncData = new SyncData();

    public KooReaderApp(com.koolearn.klibrary.core.util.SystemInfo systemInfo, final IBookCollection<Book> collection) {
        super(systemInfo);

        Collection = collection;

        collection.addListener(new IBookCollection.Listener<Book>() {
            public void onBookEvent(BookEvent event, Book book) {
                switch (event) {
                    case BookmarkStyleChanged:
                    case BookmarksUpdated:
                        if (Model != null && (book == null || collection.sameBook(book, Model.Book))) {
                            if (BookTextView.getModel() != null) {
                                setBookmarkHighlightings(BookTextView, null);
                            }
                            if (FootnoteView.getModel() != null && myFootnoteModelId != null) {
                                setBookmarkHighlightings(FootnoteView, myFootnoteModelId);
                            }
                        }
                        break;
                    case Updated:
                        onBookUpdated(book);
                        break;
                    case ProgressUpdated:
//                        clearTextCaches(); // FixBug
//						getViewWidget().repaint();
                        break;
                }
            }

            public void onBuildEvent(IBookCollection.Status status) {
            }
        });

        addAction(ActionCode.SELECTION_CLEAR, new SelectionClearAction(this));

        addAction(ActionCode.TURN_PAGE_FORWARD, new TurnPageAction(this, true)); //y 点击翻页
        addAction(ActionCode.TURN_PAGE_BACK, new TurnPageAction(this, false));

        addAction(ActionCode.EXIT, new ExitAction(this)); //y 关闭应用

        BookTextView = new KooView(this);
        FootnoteView = new KooView(this);

        setView(BookTextView);
    }

    public Book getCurrentBook() {
        final BookModel m = Model;
        return m != null ? m.Book : ExternalBook;
    }

    public void openBook(Book book, final Bookmark bookmark, Runnable postAction) {
        if (Model != null) {
            if (book == null || bookmark == null && Collection.sameBook(book, Model.Book)) {
                return;
            }
        }

        final Book bookToOpen = book;
        bookToOpen.addNewLabel(Book.READ_LABEL);
        Collection.saveBook(bookToOpen);

        final SynchronousExecutor executor = createExecutor("loadingBook");
        executor.execute(new Runnable() {
            public void run() {
                openBookInternal(bookToOpen, bookmark, false);
            }
        }, postAction);
    }

    private void reloadBook() {
        final Book book = getCurrentBook();
        if (book != null) {
            final SynchronousExecutor executor = createExecutor("loadingBook");
            executor.execute(new Runnable() {
                public void run() {
                    openBookInternal(book, null, true);
                }
            }, null);
        }
    }

    public ZLKeyBindings keyBindings() {
        return myBindings;
    }

    public KooView getTextView() {
        return (KooView) getCurrentView();
    }

    public AutoTextSnippet getFootnoteData(String id) {
        if (Model == null) {
            return null;
        }
        final BookModel.Label label = Model.getLabel(id);
        if (label == null) {
            return null;
        }
        final ZLTextModel model;
        if (label.ModelId != null) {
            model = Model.getFootnoteModel(label.ModelId);
        } else {
            model = Model.getTextModel();
        }
        if (model == null) {
            return null;
        }
        final ZLTextWordCursor cursor =
                new ZLTextWordCursor(new ZLTextParagraphCursor(model, label.ParagraphIndex));
        final AutoTextSnippet longSnippet = new AutoTextSnippet(cursor, 140);
        if (longSnippet.IsEndOfText) {
            return longSnippet;
        } else {
            return new AutoTextSnippet(cursor, 100);
        }
    }

    public void tryOpenFootnote(String id) {
        if (Model != null) {
            myJumpEndPosition = null;
            myJumpTimeStamp = null;
            final BookModel.Label label = Model.getLabel(id);
            if (label != null) {
                if (label.ModelId == null) {
                    if (getTextView() == BookTextView) {
                        addInvisibleBookmark();
                        myJumpEndPosition = new ZLTextFixedPosition(label.ParagraphIndex, 0, 0);
                        myJumpTimeStamp = new Date();
                    }
                    BookTextView.gotoPosition(label.ParagraphIndex, 0, 0);
                    setView(BookTextView);
                } else {
                    setFootnoteModel(label.ModelId);
                    setView(FootnoteView);
                    FootnoteView.gotoPosition(label.ParagraphIndex, 0, 0);
                }
                getViewWidget().repaint();
                storePosition();
            }
        }
    }

    public void clearTextCaches() {
        BookTextView.clearCaches();
        FootnoteView.clearCaches();
    }

    public Bookmark addSelectionBookmark() {
        final KooView kooView = getTextView();
        final TextSnippet snippet = kooView.getSelectedSnippet();
        if (snippet == null) {
            return null;
        }

        final Bookmark bookmark = new Bookmark(
                Collection,
                Model.Book,
                kooView.getModel().getId(),
                snippet,
                true
        );
        Collection.saveBookmark(bookmark);
        kooView.clearSelection();

        return bookmark;
    }

    private void setBookmarkHighlightings(ZLTextView view, String modelId) {
        view.removeHighlightings(BookmarkHighlighting.class);
        for (BookmarkQuery query = new BookmarkQuery(Model.Book, 20); ; query = query.next()) {
            final List<Bookmark> bookmarks = Collection.bookmarks(query);
            if (bookmarks.isEmpty()) {
                break;
            }
            for (Bookmark b : bookmarks) {
                if (b.getEnd() == null) {
                    BookmarkUtil.findEnd(b, view);
                }
                if (ComparisonUtil.equal(modelId, b.ModelId)) {
                    view.addHighlighting(new BookmarkHighlighting(view, Collection, b));
                }
            }
        }
    }

    private void setFootnoteModel(String modelId) {
        final ZLTextModel model = Model.getFootnoteModel(modelId);
        FootnoteView.setModel(model);
        if (model != null) {
            myFootnoteModelId = modelId;
            setBookmarkHighlightings(FootnoteView, modelId);
        }
    }

    private synchronized void openBookInternal(final Book book, Bookmark bookmark, boolean force) {
        if (!force && Model != null && Collection.sameBook(book, Model.Book)) {
            if (bookmark != null) {
                gotoBookmark(bookmark, false);
            }
            return;
        }

        hideActivePopup();
        storePosition();

        BookTextView.setModel(null);
        FootnoteView.setModel(null);
        clearTextCaches();
        Model = null;
        ExternalBook = null;
        System.gc();
        System.gc();

        // 使用插件的方式,让不同的格式走不同的代码去生成Model(根据后缀名判断属于哪种插件)
        final PluginCollection pluginCollection = PluginCollection.Instance(SystemInfo);
        final FormatPlugin plugin;
        try {
            plugin = BookUtil.getPlugin(pluginCollection, book);
        } catch (BookReadingException e) {
            processException(e);
            return;
        }

        try {
            Model = BookModel.createModel(book, plugin); // NativeFormatPlugin [ePub] 慢慢加载
            Collection.saveBook(book); // 保存书籍
//            ZLTextHyphenator.Instance().load(book.getLanguage());
            BookTextView.setModel(Model.getTextModel()); // 给KooView传入TextModel 操作-UI在这里分界
            setBookmarkHighlightings(BookTextView, null);
            gotoStoredPosition();
            if (bookmark == null) {
                setView(BookTextView);
            } else {
                gotoBookmark(bookmark, false);
            }

            Collection.addToRecentlyOpened(book); // 保存书籍至最近阅读的数据库
        } catch (BookReadingException e) {
            processException(e);
        }

        /**
         *  得到ZLViewWidget
         *  ZLAndroidWidget实现了该接口
         *  repaint()中执行postInvalidate();
         *  onDraw()被调用
         */
        getViewWidget().reset();
        getViewWidget().repaint();

        for (FileEncryptionInfo info : plugin.readEncryptionInfos(book)) {
            if (info != null && !EncryptionMethod.isSupported(info.Method)) {
                showErrorMessage("unsupportedEncryptionMethod", book.getPath()); // 不支持加密方法
                break;
            }
        }
    }

    private List<Bookmark> invisibleBookmarks() {
        final List<Bookmark> bookmarks = Collection.bookmarks(
                new BookmarkQuery(Model.Book, false, 10)
        );
        Collections.sort(bookmarks, new Bookmark.ByTimeComparator());
        return bookmarks;
    }

    /**
     * 返回到最近阅读
     * @return
     */
    public boolean jumpBack() {
        try {
            if (getTextView() != BookTextView) {
                showBookTextView();
                return true;
            }

            if (myJumpEndPosition == null || myJumpTimeStamp == null) {
                return false;
            }
            // more than 2 minutes ago
            if (myJumpTimeStamp.getTime() + 2 * 60 * 1000 < new Date().getTime()) {
                return false;
            }
            if (!myJumpEndPosition.equals(BookTextView.getStartCursor())) {
                return false;
            }

            final List<Bookmark> bookmarks = invisibleBookmarks();
            if (bookmarks.isEmpty()) {
                return false;
            }
            final Bookmark b = bookmarks.get(0);
            Collection.deleteBookmark(b);
            gotoBookmark(b, true);
            return true;
        } finally {
            myJumpEndPosition = null;
            myJumpTimeStamp = null;
        }
    }

    private void gotoBookmark(Bookmark bookmark, boolean exactly) {
        final String modelId = bookmark.ModelId;
        if (modelId == null) {
            addInvisibleBookmark();
            if (exactly) {
                BookTextView.gotoPosition(bookmark);
            } else {
                BookTextView.gotoHighlighting(
                        new BookmarkHighlighting(BookTextView, Collection, bookmark)
                );
            }
            setView(BookTextView);
        } else {
            setFootnoteModel(modelId);
            if (exactly) {
                FootnoteView.gotoPosition(bookmark);
            } else {
                FootnoteView.gotoHighlighting(
                        new BookmarkHighlighting(FootnoteView, Collection, bookmark)
                );
            }
            setView(FootnoteView);
        }
        getViewWidget().repaint();
        storePosition();
    }

    public void showBookTextView() {
        setView(BookTextView);
    }

    public void onWindowClosing() {
        storePosition();
    }

    private class PositionSaver implements Runnable { // 进度保存
        private final Book myBook;
        private final ZLTextPosition myPosition;
        private final RationalNumber myProgress;

        PositionSaver(Book book, ZLTextPosition position, RationalNumber progress) {
            myBook = book;
            myPosition = position;
            myProgress = progress;
        }

        public void run() {
            Collection.storePosition(myBook.getId(), myPosition);
            myBook.setProgress(myProgress);
            Collection.saveBook(myBook);
        }
    }

    private class SaverThread extends Thread {
        private final List<Runnable> myTasks =
                Collections.synchronizedList(new LinkedList<Runnable>());

        SaverThread() {
            setPriority(MIN_PRIORITY);
        }

        void add(Runnable task) {
            myTasks.add(task);
        }

        public void run() {
            while (true) {
                synchronized (myTasks) {
                    while (!myTasks.isEmpty()) {
                        myTasks.remove(0).run();
                    }
                }
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    private final SaverThread mySaverThread = new SaverThread();
    private volatile ZLTextPosition myStoredPosition;
    private volatile Book myStoredPositionBook;

    private ZLTextFixedPosition getStoredPosition(Book book) {
        final ZLTextFixedPosition.WithTimestamp fromServer =
                mySyncData.getAndCleanPosition(Collection.getHash(book, true));
        final ZLTextFixedPosition.WithTimestamp local =
                Collection.getStoredPosition(book.getId());
        if (local == null) {
            return fromServer != null ? fromServer : new ZLTextFixedPosition(0, 0, 0);
        } else if (fromServer == null) {
            return local;
        } else {
            return fromServer.Timestamp >= local.Timestamp ? fromServer : local;
        }
    }

    private void gotoStoredPosition() {
        myStoredPositionBook = Model != null ? Model.Book : null;
        if (myStoredPositionBook == null) {
            return;
        }
        myStoredPosition = getStoredPosition(myStoredPositionBook);
        BookTextView.gotoPosition(myStoredPosition);
        savePosition();
    }

    public void storePosition() { // 进度保存
        final Book bk = Model != null ? Model.Book : null;

        if (bk != null && bk == myStoredPositionBook && myStoredPosition != null && BookTextView != null) {
            final ZLTextPosition position = new ZLTextFixedPosition(BookTextView.getStartCursor());
            if (!myStoredPosition.equals(position)) {
                myStoredPosition = position;
                savePosition();
            }
        }
    }

    private void savePosition() { // 保存进度
        final RationalNumber progress = BookTextView.getProgress();
        synchronized (mySaverThread) {
            if (!mySaverThread.isAlive()) {
                mySaverThread.start();
            }
            mySaverThread.add(new PositionSaver(myStoredPositionBook, myStoredPosition, progress));
        }
    }

    private synchronized void updateInvisibleBookmarksList(Bookmark b) {
        if (Model != null && Model.Book != null && b != null) {
            for (Bookmark bm : invisibleBookmarks()) {
                if (b.equals(bm)) {
                    Collection.deleteBookmark(bm);
                }
            }
            Collection.saveBookmark(b);
            final List<Bookmark> bookmarks = invisibleBookmarks();
            for (int i = 3; i < bookmarks.size(); ++i) {
                Collection.deleteBookmark(bookmarks.get(i));
            }
        }
    }

    public void addInvisibleBookmark(ZLTextWordCursor cursor) {
        if (cursor == null) {
            return;
        }

        cursor = new ZLTextWordCursor(cursor);
        if (cursor.isNull()) {
            return;
        }

        final ZLTextView textView = getTextView();
        final ZLTextModel textModel;
        final Book book;
        final AutoTextSnippet snippet;
        // textView.model will not be changed inside synchronised block
        synchronized (textView) {
            textModel = textView.getModel();
            final BookModel model = Model;
            book = model != null ? model.Book : null;
            if (book == null || textView != BookTextView || textModel == null) {
                return;
            }
            snippet = new AutoTextSnippet(cursor, 30);
        }

        updateInvisibleBookmarksList(new Bookmark(
                Collection, book, textModel.getId(), snippet, false
        ));
    }

    public void addInvisibleBookmark() {
        if (Model.Book != null && getTextView() == BookTextView) {
            updateInvisibleBookmarksList(createBookmark(30, false));
        }
    }

    public Bookmark createBookmark(int maxChars, boolean visible) {
        final KooView view = getTextView();
        final ZLTextWordCursor cursor = view.getStartCursor();

        if (cursor.isNull()) {
            return null;
        }
        return new Bookmark(
                Collection,
                Model.Book,
                view.getModel().getId(),
                new AutoTextSnippet(cursor, maxChars),
                visible
        );
    }

    public TOCTree getCurrentTOCElement() {
        final ZLTextWordCursor cursor = BookTextView.getStartCursor();
        if (Model == null || cursor == null) {
            return null;
        }

        int index = cursor.getParagraphIndex();
        if (cursor.isEndOfParagraph()) {
            ++index;
        }
        TOCTree treeToSelect = null;
        for (TOCTree tree : Model.TOCTree) {
            final TOCTree.Reference reference = tree.getReference();
            if (reference == null) {
                continue;
            }
            if (reference.ParagraphIndex > index) {
                break;
            }
            treeToSelect = tree;
        }
        return treeToSelect;
    }

    public void onBookUpdated(Book book) {
        if (Model == null || Model.Book == null || !Collection.sameBook(Model.Book, book)) {
            return;
        }

        final String newEncoding = book.getEncodingNoDetection();
        final String oldEncoding = Model.Book.getEncodingNoDetection();

        Model.Book.updateFrom(book);

        if (newEncoding != null && !newEncoding.equals(oldEncoding)) {
            reloadBook();
        } else {
            clearTextCaches();
            getViewWidget().repaint();
        }
    }
}