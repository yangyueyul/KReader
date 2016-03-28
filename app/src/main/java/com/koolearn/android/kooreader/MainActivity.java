//package com.koolearn.android.kooreader;
//
//import android.app.Activity;
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.os.Bundle;
//import android.os.Environment;
//import android.os.Handler;
//import android.os.Message;
//import android.support.v4.widget.SwipeRefreshLayout;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.support.v7.widget.StaggeredGridLayoutManager;
//import android.util.DisplayMetrics;
//import android.view.View;
//import android.widget.ImageView;
//import android.widget.Toast;
//
//import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
//import com.koolearn.android.kooreader.library.LibraryActivity;
//import com.koolearn.android.kooreader.libraryService.BookCollectionShadow;
//import com.koolearn.android.kooreader.util.AndroidImageSynchronizer;
//import com.koolearn.android.util.LogUtil;
//import com.koolearn.android.util.OrientationUtil;
//import com.koolearn.klibrary.core.image.ZLImage;
//import com.koolearn.klibrary.core.image.ZLImageProxy;
//import com.koolearn.klibrary.ui.android.R;
//import com.koolearn.klibrary.ui.android.image.ZLAndroidImageData;
//import com.koolearn.klibrary.ui.android.image.ZLAndroidImageManager;
//import com.koolearn.klibrary.ui.android.library.ZLAndroidLibrary;
//import com.koolearn.kooreader.Paths;
//import com.koolearn.kooreader.book.Book;
//import com.koolearn.kooreader.book.CoverUtil;
//import com.koolearn.kooreader.formats.PluginCollection;
//import com.koolearn.kooreader.library.RootTree;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Timer;
//import java.util.TimerTask;
//
///**
// * ******************************************
// * 作    者 ：  杨越
// * 版    本 ：  1.0
// * 创建日期 ：  2016/2/23
// * 描    述 ：
// * 修订历史 ：
// * ******************************************
// */
//public class MainActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener {
//    private ImageView shelf_menu, shelf_search;
//    private Timer timer = null;
//    private TimerTask timeTask = null;
//    private boolean isExit = false;
//    private SlidingMenu slidingMenu;
//
//    private List<Book> bookshelf = new ArrayList<Book>();
//    private RecyclerView recyclerView;
//    private SwipeRefreshLayout refreshLayout;
//    private MyBookAdapter mBookAdapter;
//
//    private final BookCollectionShadow myCollection = new BookCollectionShadow();
//    private volatile RootTree myRootTree;
//
//    Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            LogUtil.i18("handleMessage");
//            refreshLayout.setRefreshing(false);
//        }
//    };
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_bookshelf);
//        init();
////        myCollection.bindToService(this, new Runnable() {
////            public void run() {
//////                setProgressBarIndeterminateVisibility(!myCollection.status().IsComplete);
//////                myRootTree = new RootTree(myCollection, PluginCollection.Instance(Paths.systemInfo(MainActivity.this)));
////                bookshelf = myCollection.recentlyOpenedBooks(9);
////                displayBook();
////            }
////        });
//
//        /**
//         * 侧滑菜单
//         */
//        slidingMenu = new SlidingMenu(this);
//        slidingMenu.setMode(SlidingMenu.LEFT); // 只要左边
//        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN); // 设置边界触摸
//        slidingMenu.setShadowWidthRes(R.dimen.shadow_width); // 两个界面间的阴影宽度
//        slidingMenu.setShadowDrawable(R.drawable.shadow);//设置阴影图片
//        int i = ZLAndroidLibrary.Instance().getScreenWidth() * 3 / 4;
//        slidingMenu.setBehindWidth(i); // 设置滑出的宽度为 屏幕宽度的 5/6
//        slidingMenu.setFadeDegree(0.50F);
//        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
//        slidingMenu.setMenu(R.layout.bookshelf_sliding_menu);
//    }
//
//    private void init() {
//        timer = new Timer();
//        shelf_search = (ImageView) findViewById(R.id.shelf_search);
//        shelf_menu = (ImageView) findViewById(R.id.shelf_menu);
//        recyclerView = (RecyclerView) findViewById(R.id.recycler);
//        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
//
//        shelf_menu.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                slidingMenu.showMenu();
//            }
//        });
//
//        shelf_search.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                OrientationUtil.startActivity(MainActivity.this, new Intent(MainActivity.this, LibraryActivity.class));
//                overridePendingTransition(R.anim.tran_fade_in, R.anim.tran_fade_out);
//            }
//        });
//
//        refreshLayout.setOnRefreshListener(this);
//        refreshLayout.setColorSchemeResources(R.color.progressBarBlue,
//                R.color.progressBarBgOrange,
//                R.color.progressBarBgWhiteOrange);
//        refreshLayout.setProgressBackgroundColor(R.color.progressBarBgGreen);
//
//        new Thread() {
//            @Override
//            public void run() {
//                copyFonts("hksv.ttf");
//                copyFonts("wryh.ttf");
//                copyEpub("Harry.epub");
//                copyEpub("ABeaver.epub");
//            }
//        }.start();
//    }
//
//    private void displayBook() {
//        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, LinearLayoutManager.VERTICAL);
//        recyclerView.setLayoutManager(layoutManager);
//        mBookAdapter = new MyBookAdapter(this, bookshelf);
//        recyclerView.setAdapter(mBookAdapter);
//        mBookAdapter.setOnItemClickListener(new MyBookAdapter.OnRecyclerViewItemClickListener() {
//            @Override
//            public void onItemClick(View view, Book data) {
//                KooReader.openBookActivity(MainActivity.this, data, null);
//                overridePendingTransition(R.anim.tran_fade_in, R.anim.tran_fade_out);
//            }
//        });
//    }
//
//    @Override
//    public void onBackPressed() {
//        if (isExit) {
//            finish();
//        } else {
//            isExit = true;
//            Toast.makeText(this, "再按一次退出掌读", Toast.LENGTH_SHORT).show();
//            timeTask = new TimerTask() {
//
//                @Override
//                public void run() {
//                    isExit = false;
//                }
//            };
//            timer.schedule(timeTask, 2000);
//        }
//    }
//
//    @Override
//    public void onRefresh() {
//        handler.sendEmptyMessageDelayed(1, 2000);
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        getBooks();
//    }
//
//    /**
//     * 字体拷贝
//     */
//    private void copyFonts(String fontName) {
//        File destFile = new File(getFilesDir(), fontName);
//        LogUtil.i6("destFile.getPath()" + destFile.getPath());
//        // 判断数据库是否拷贝过
//        if (destFile.exists()) {
//            System.out.println("字体" + fontName + "已存在!");
//            return; // 不再执行
//        }
//
//        FileOutputStream out = null;
//        InputStream in = null;
//
//        try {
//            in = getAssets().open(fontName);
//            out = new FileOutputStream(destFile);
//
//            int len = 0;
//            byte[] buffer = new byte[1024];
//            while ((len = in.read(buffer)) != -1) {
//                out.write(buffer, 0, len);
//            }
//            in.close();
//            out.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    /**
//     * epub拷贝
//     */
//    private void copyEpub(String epubName) {
//        File destFile = new File(Environment.getExternalStorageDirectory(), epubName); // 与Path路径中的设置一致,可以读到数据库中
////        File destFile = new File(Environment.getExternalStorageDirectory() + "/Books", epubName); // 与Path路径中的设置一致,可以读到数据库中
//        System.out.println("aa" + destFile.getPath());
//        // 判断数据库是否拷贝过
//
//        if (destFile.exists()) {
//            System.out.println("epubName:" + epubName + "已存在!");
//            return; // 不再执行
//        }
//
//        FileOutputStream out = null;
//        InputStream in = null;
//
//        try {
//            in = getAssets().open(epubName);
//            out = new FileOutputStream(destFile);
//
//            int len = 0;
//            byte[] buffer = new byte[1024];
//            while ((len = in.read(buffer)) != -1) {
//                out.write(buffer, 0, len);
//            }
//            in.close();
//            out.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        myCollection.unbind();
//    }
//
//
//    private void getBooks() {
//        myCollection.bindToService(this, new Runnable() {
//            public void run() {
//                bookshelf.clear();
//                bookshelf = myCollection.recentlyOpenedBooks(9);
//                initCover();
//                displayBook();
//            }
//        });
//    }
//
//    private void initCover() {
//        for (Book book : bookshelf) {
//            setCover(book);
//        }
//    }
//
//    private void setCover(final Book book) {
//        LogUtil.i18("1setCover");
//        final String fileName = Paths.internalTempDirectoryValue(this) + "/" + book.getSortKey() + ".png";
//        File file = new File(fileName);
//        if (file.exists()) {
//            book.setMyCoverPath(fileName);
//            LogUtil.i18("2setCover" + file + "已存在!");
//            return; // 不再执行
//        }
//        AndroidImageSynchronizer myImageSynchronizer = new AndroidImageSynchronizer(this);
//        PluginCollection pluginCollection = PluginCollection.Instance(Paths.systemInfo(this));
//        final ZLImage image = CoverUtil.getCover(book, pluginCollection);
//        if (image instanceof ZLImageProxy) {
//            ((ZLImageProxy) image).startSynchronization(myImageSynchronizer, new Runnable() {
//                public void run() {
//                    runOnUiThread(new Runnable() {
//                        public void run() {
//                            final ZLAndroidImageData data = ((ZLAndroidImageManager) ZLAndroidImageManager.Instance()).getImageData(image);
//                            if (data != null) {
//                                final DisplayMetrics metrics = new DisplayMetrics();
//                                getWindowManager().getDefaultDisplay().getMetrics(metrics);
//                                final int maxHeight = metrics.heightPixels * 2 / 3;
//                                final int maxWidth = maxHeight * 2 / 3;
//                                final Bitmap coverBitmap = data.getBitmap(2 * maxWidth, 2 * maxHeight);
//                                try {
//                                    book.setMyCoverPath(fileName);
//                                    saveMyBitmap(fileName, coverBitmap);
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }
//                    });
//                }
//            });
//        }
//    }
//
//    // 1.书籍加载缓存位置更改
//    // 2.首次展示预置书籍,图书封面缓存
//    public void saveMyBitmap(String fileName, Bitmap mBitmap) throws IOException {
//        File file = new File(fileName);
//        file.createNewFile();
//        LogUtil.i18("3setCover:" + fileName);
//        FileOutputStream fOut = null;
//        try {
//            fOut = new FileOutputStream(file);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
//        LogUtil.i18("6setCover Complete");
//        mBookAdapter.notifyDataSetChanged();
//        try {
//            fOut.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//            fOut.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    public void saveBitmap(File f, Bitmap mBitmap) {
//        LogUtil.i18("setCover:" + f.getPath());
//        try {
//            f.createNewFile();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        if (f.exists()) {
//            f.delete();
//        }
//        try {
//            FileOutputStream out = new FileOutputStream(f);
//            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
//            out.flush();
//            out.close();
//            LogUtil.i18("setCoverComplete");
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//    }
//}