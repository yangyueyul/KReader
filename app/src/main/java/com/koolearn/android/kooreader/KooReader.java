package com.koolearn.android.kooreader;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.koolearn.android.kooreader.api.KooReaderIntents;
import com.koolearn.android.kooreader.libraryService.BookCollectionShadow;
import com.koolearn.android.util.UIMessageUtil;
import com.koolearn.android.util.UIUtil;
import com.koolearn.klibrary.core.application.ZLApplicationWindow;
import com.koolearn.klibrary.core.filesystem.ZLFile;
import com.koolearn.klibrary.core.options.Config;
import com.koolearn.klibrary.core.view.ZLViewWidget;
import com.koolearn.klibrary.ui.android.R;
import com.koolearn.klibrary.ui.android.error.ErrorKeys;
import com.koolearn.klibrary.ui.android.view.AndroidFontUtil;
import com.koolearn.klibrary.ui.android.view.ZLAndroidPaintContext;
import com.koolearn.klibrary.ui.android.view.ZLAndroidWidget;
import com.koolearn.kooreader.Paths;
import com.koolearn.kooreader.book.Book;
import com.koolearn.kooreader.book.BookUtil;
import com.koolearn.kooreader.book.Bookmark;
import com.koolearn.kooreader.bookmodel.BookModel;
import com.koolearn.kooreader.kooreader.ActionCode;
import com.koolearn.kooreader.kooreader.KooReaderApp;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

public final class KooReader extends KooReaderMainActivity implements ZLApplicationWindow {
    public static final int RESULT_DO_NOTHING = RESULT_FIRST_USER;

    public static void openBookActivity(Context context, Book book, Bookmark bookmark) {
        final Intent intent = new Intent(context, KooReader.class);
        intent.setAction(KooReaderIntents.Action.VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        KooReaderIntents.putBookExtra(intent, book);
        context.startActivity(intent);
    }

    private KooReaderApp myKooReaderApp;
    private volatile Book myBook;
    private RelativeLayout myRootView;
    private ZLAndroidWidget myMainView;
//    private ZLAndroidCurlWidget myCurlView;
    volatile boolean IsPaused = false;
    private volatile long myResumeTimestamp;
    private Intent myOpenBookIntent = null;

    private synchronized void openBook(Intent intent, final Runnable action, boolean force) {
        if (!force && myBook != null) {
            return;
        }
        myBook = KooReaderIntents.getBookExtra(intent, myKooReaderApp.Collection);
        if (myBook == null) {
            final Uri data = intent.getData();
            if (data != null) {
                myBook = createBookForFile(ZLFile.createFileByPath(data.getPath()));
            }
        }
        if (myBook != null) {
            ZLFile file = BookUtil.fileByBook(myBook);
            if (!file.exists()) {
                if (file.getPhysicalFile() != null) {
                    file = file.getPhysicalFile();
                }
                UIMessageUtil.showErrorMessage(this, "fileNotFound", file.getPath());
                myBook = null;
            } else {
            }
        }
        Config.Instance().runOnConnect(new Runnable() {
            public void run() {
                myKooReaderApp.openBook(myBook, null, action); // UIUtil
                AndroidFontUtil.clearFontCache();
            }
        });
    }

    private Book createBookForFile(ZLFile file) {
        if (file == null) {
            return null;
        }
        Book book = myKooReaderApp.Collection.getBookByFile(file.getPath());
        if (book != null) {
            return book;
        }
        if (file.isArchive()) {
            for (ZLFile child : file.children()) {
                book = myKooReaderApp.Collection.getBookByFile(child.getPath());
                if (book != null) {
                    return book;
                }
            }
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);

        myRootView = (RelativeLayout) findViewById(R.id.root_view);
        myMainView = (ZLAndroidWidget) findViewById(R.id.main_view);
//        myCurlView = (ZLAndroidCurlWidget) findViewById(R.id.curl_view);

        myKooReaderApp = (KooReaderApp) KooReaderApp.Instance();
        if (myKooReaderApp == null) {
            myKooReaderApp = new KooReaderApp(Paths.systemInfo(this), new BookCollectionShadow());
        }
        getCollection().bindToService(this, null); //y 绑定libraryService

        myBook = null;

        myKooReaderApp.setWindow(this);
        myKooReaderApp.initWindow();

        getWindow().setFlags( //y 全屏
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN // 设置窗体全屏
        );

        if (myKooReaderApp.getPopupById(NavigationPopup.ID) == null) {
            new NavigationPopup(myKooReaderApp);
        }
        if (myKooReaderApp.getPopupById(SettingPopup.ID) == null) {
            new SettingPopup(myKooReaderApp);
        }

        myKooReaderApp.addAction(ActionCode.SHOW_NAVIGATION, new ShowNavigationAction(this, myKooReaderApp)); //y 页面跳转
        myKooReaderApp.addAction(ActionCode.PROCESS_HYPERLINK, new ProcessHyperlinkAction(this, myKooReaderApp)); //y 打开超链接、图片等
        new OpenPhotoAction(this, myKooReaderApp, myRootView);

        final Intent intent = getIntent();

        myOpenBookIntent = intent;
        ZLAndroidPaintContext.myReader = myKooReaderApp;
    }


    @Override
    protected void onStart() {
        super.onStart();
        ((NavigationPopup) myKooReaderApp.getPopupById(NavigationPopup.ID)).setPanelInfo(this, myRootView);
        ((SettingPopup) myKooReaderApp.getPopupById(SettingPopup.ID)).setPanelInfo(this, myRootView);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        switchWakeLock(hasFocus && getZLibrary().BatteryLevelToTurnScreenOffOption.getValue() < myKooReaderApp.getBatteryLevel());
    }

    @Override
    protected void onResume() {
        super.onResume();
        myStartTimer = true;
        Config.Instance().runOnConnect(new Runnable() {
            public void run() {
                final int brightnessLevel = getZLibrary().ScreenBrightnessLevelOption.getValue(); // 亮度设置
                if (brightnessLevel != 0) {
                    getViewWidget().setScreenBrightness(brightnessLevel);
                } else {
                    setScreenBrightnessAuto();
                }
                getCollection().bindToService(KooReader.this, new Runnable() { // 字体类型等设置改变时更新界面 clearTextCaches(); getViewWidget().repaint();
                    public void run() {
                        final BookModel model = myKooReaderApp.Model;
                        if (model == null || model.Book == null) {
                            return; // 首次调用会进入一次
                        }
                        onPreferencesUpdate(myKooReaderApp.Collection.getBookById(model.Book.getId()));
                    }
                });
            }
        });

        registerReceiver(myBatteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        IsPaused = false;
        myResumeTimestamp = System.currentTimeMillis();

        if (myOpenBookIntent != null) {
            final Intent intent = myOpenBookIntent;
            myOpenBookIntent = null;
            getCollection().bindToService(this, new Runnable() {
                public void run() {
                    openBook(intent, null, true);
                }
            });
        }
    }

    @Override
    protected void onPause() {
        IsPaused = true;
        try {
            unregisterReceiver(myBatteryInfoReceiver);
        } catch (IllegalArgumentException e) {
        }

        myKooReaderApp.stopTimer();
        myKooReaderApp.onWindowClosing();
        super.onPause();
    }

    @Override
    protected void onStop() {
        OpenPhotoAction.isOpen = false; // 防止未关闭图片直接按按返回键
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        getCollection().unbind();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        myKooReaderApp.onWindowClosing();
        super.onLowMemory();
    }

    private void onPreferencesUpdate(Book book) {
        AndroidFontUtil.clearFontCache();
        myKooReaderApp.onBookUpdated(book);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
            case REQUEST_PREFERENCES:
                if (resultCode != RESULT_DO_NOTHING && data != null) {
                    final Book book = KooReaderIntents.getBookExtra(data, myKooReaderApp.Collection);
                    if (book != null) {
                        getCollection().bindToService(this, new Runnable() {
                            public void run() {
                                onPreferencesUpdate(book);
                            }
                        });
                    }
                }
                break;
        }
    }

    public void navigate() {
        ((NavigationPopup) myKooReaderApp.getPopupById(NavigationPopup.ID)).runNavigation();
    }

    public void setting() {
        ((SettingPopup) myKooReaderApp.getPopupById(SettingPopup.ID)).runNavigation();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return (myMainView != null && myMainView.onKeyDown(keyCode, event)) || super.onKeyDown(keyCode, event);
//        return (myCurlView != null && myCurlView.onKeyDown(keyCode, event)) || super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        return (myCurlView != null && myCurlView.onKeyUp(keyCode, event)) || super.onKeyUp(keyCode, event);
        return (myMainView != null && myMainView.onKeyUp(keyCode, event)) || super.onKeyUp(keyCode, event);
    }

    private PowerManager.WakeLock myWakeLock;
    private boolean myWakeLockToCreate;
    private boolean myStartTimer;

    public final void createWakeLock() {
        // 滑动过程不停调用
        if (myWakeLockToCreate) { // 首次调用1次
            synchronized (this) {
                if (myWakeLockToCreate) {
                    myWakeLockToCreate = false;
                    myWakeLock =
                            ((PowerManager) getSystemService(POWER_SERVICE))
                                    .newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "KooReader");
                    myWakeLock.acquire();
                }
            }
        }
        if (myStartTimer) { // 首次调用1次
            myKooReaderApp.startTimer();
            myStartTimer = false;
        }
    }

    private final void switchWakeLock(boolean on) {
        if (on) {
            if (myWakeLock == null) {
                myWakeLockToCreate = true;
            }
        } else {
            if (myWakeLock != null) {
                synchronized (this) {
                    if (myWakeLock != null) {
                        myWakeLock.release();
                        myWakeLock = null;
                    }
                }
            }
        }
    }

    private BroadcastReceiver myBatteryInfoReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            final int level = intent.getIntExtra("level", 100);
            setBatteryLevel(level);
            switchWakeLock(
                    hasWindowFocus() &&
                            getZLibrary().BatteryLevelToTurnScreenOffOption.getValue() < level
            );
        }
    };

    private BookCollectionShadow getCollection() {
        return (BookCollectionShadow) myKooReaderApp.Collection;
    }

    @Override
    public void showErrorMessage(String key) {
        UIMessageUtil.showErrorMessage(this, key);
    }

    @Override
    public void showErrorMessage(String key, String parameter) {
        UIMessageUtil.showErrorMessage(this, key, parameter);
    }

    @Override
    public KooReaderApp.SynchronousExecutor createExecutor(String key) {
        return UIUtil.createExecutor(this, key);
    }

    private int myBatteryLevel;

    @Override
    public int getBatteryLevel() {
        return myBatteryLevel;
    }

    private void setBatteryLevel(int percent) {
        myBatteryLevel = percent;
    }

    @Override
    public void close() {
        finish();
    }

    @Override
    public ZLViewWidget getViewWidget() {
        return myMainView;
//        return myCurlView;
    }

    private final HashMap<MenuItem, String> myMenuItemMap = new HashMap<MenuItem, String>();

    private final MenuItem.OnMenuItemClickListener myMenuListener =
            new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    myKooReaderApp.runAction(myMenuItemMap.get(item));
                    return true;
                }
            };

    @Override
    public void refresh() {
    }

    @Override
    public void processException(Exception exception) {
        exception.printStackTrace();
        final Intent intent = new Intent(
                KooReaderIntents.Action.ERROR,
                new Uri.Builder().scheme(exception.getClass().getSimpleName()).build()
        );
        intent.setPackage(KooReaderIntents.DEFAULT_PACKAGE);
        intent.putExtra(ErrorKeys.MESSAGE, exception.getMessage());
        final StringWriter stackTrace = new StringWriter();
        exception.printStackTrace(new PrintWriter(stackTrace));
        intent.putExtra(ErrorKeys.STACKTRACE, stackTrace.toString());
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }
}