package com.koolearn.android.kooreader;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.koolearn.android.kooreader.library.LibraryActivity;
import com.koolearn.android.kooreader.libraryService.BookCollectionShadow;
import com.koolearn.android.kooreader.util.AndroidImageSynchronizer;
import com.koolearn.android.util.OrientationUtil;
import com.koolearn.klibrary.core.image.ZLImage;
import com.koolearn.klibrary.core.image.ZLImageProxy;
import com.koolearn.klibrary.ui.android.R;
import com.koolearn.klibrary.ui.android.image.ZLAndroidImageData;
import com.koolearn.klibrary.ui.android.image.ZLAndroidImageManager;
import com.koolearn.kooreader.Paths;
import com.koolearn.kooreader.book.Book;
import com.koolearn.kooreader.book.CoverUtil;
import com.koolearn.kooreader.formats.PluginCollection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * ******************************************
 * 作    者 ：  杨越
 * 版    本 ：  1.0
 * 创建日期 ：  2016/2/23
 * 描    述 ：
 * 修订历史 ：
 * ******************************************
 */
public class MainAppActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar mToolbar;
    private NavigationView mNavigationView;

    private Timer timer = null;
    private TimerTask timeTask = null;
    private boolean isExit = false;

    private List<Book> bookshelf = new ArrayList<Book>();
    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private MyBookAdapter mBookAdapter;
    private FloatingActionButton mFabSearch;

    private final BookCollectionShadow myCollection = new BookCollectionShadow();

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            refreshLayout.setRefreshing(false);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mFabSearch = (FloatingActionButton) findViewById(R.id.fab_search);
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);

        setSupportActionBar(mToolbar);
        setupDrawerContent(mNavigationView);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        setUpProfileImage();

        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeResources(R.color.progressBarBlue, R.color.progressBarBgWhiteOrange);
        refreshLayout.setProgressBackgroundColor(R.color.progressBarBgGreen);

        timer = new Timer();

        mFabSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OrientationUtil.startActivity(MainAppActivity.this, new Intent(MainAppActivity.this, LibraryActivity.class));
                overridePendingTransition(R.anim.tran_fade_in, R.anim.tran_fade_out);
            }
        });

        copyBooks();
    }


    private void displayBook() {
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        mBookAdapter = new MyBookAdapter(this, bookshelf);
        recyclerView.setAdapter(mBookAdapter);
        mBookAdapter.setOnItemClickListener(new MyBookAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, Book data) {
                KooReader.openBookActivity(MainAppActivity.this, data, null);
                overridePendingTransition(R.anim.tran_fade_in, R.anim.tran_fade_out);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (isExit) {
            finish();
        } else {
            isExit = true;
            Toast.makeText(this, "再按一次退出掌读", Toast.LENGTH_SHORT).show();
            timeTask = new TimerTask() {

                @Override
                public void run() {
                    isExit = false;
                }
            };
            timer.schedule(timeTask, 2000);
        }
    }

    @Override
    public void onRefresh() {
        handler.sendEmptyMessageDelayed(1, 2000);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getBooks();
    }

    /**
     * 字体拷贝
     */
    private void copyFonts(String fontName) {
        File destFile = new File(getFilesDir(), fontName);
        if (destFile.exists()) {
            System.out.println(destFile + "已存在");
            return;
        }

        FileOutputStream out = null;
        InputStream in = null;

        try {
            in = getAssets().open(fontName);
            out = new FileOutputStream(destFile);
            int len = 0;
            byte[] buffer = new byte[1024];
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyBooks() {
        new Thread() {
            @Override
            public void run() {
                copyFonts("hksv.ttf");
                copyFonts("wryh.ttf");
                copyEpub("harry.epub");
                copyEpub("abeaver.epub");
                copyEpub("silverchair.epub");

                copyEpub("ExaminationCloze.doc");
                copyEpub("function.doc");
//                copyEpubToSdCard("TheSilverChair.epub");
//                copyEpubToSdCard("ExaminationCloze.doc");
//                copyEpubToSdCard("function.doc");
            }
        }.start();
    }

    /**
     * epub拷贝
     */
    private void copyEpub(String epubName) {
        final String fileName = Paths.internalTempDirectoryValue(this) + "/" + epubName;
        File file = new File(fileName);
        if (file.exists()) {
            System.out.println(fileName + "已存在");
            return;
        }

        FileOutputStream out = null;
        InputStream in = null;

        try {
            in = getAssets().open(epubName);
            out = new FileOutputStream(file);
            int len = 0;
            byte[] buffer = new byte[1024];
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * epub拷贝
     */
    private void copyEpubToSdCard(String epubName) {
        File destFile = new File(Environment.getExternalStorageDirectory(), epubName); // 与Path路径中的设置一致,可以读到数据库中
        if (destFile.exists()) {
            return;
        }

        FileOutputStream out = null;
        InputStream in = null;

        try {
            in = getAssets().open(epubName);
            out = new FileOutputStream(destFile);

            int len = 0;
            byte[] buffer = new byte[1024];
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myCollection.unbind();
    }

    private void getBooks() {
        myCollection.bindToService(this, new Runnable() {
            public void run() {
                bookshelf.clear();
                bookshelf = myCollection.recentlyOpenedBooks(9);
                while (bookshelf.size() < 2) {
                    try {
                        Thread.sleep(1000);
                        bookshelf = myCollection.recentlyOpenedBooks(9);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                for (Book book : bookshelf) { // 缓存书籍封面至本地
                    setCover(book);
                }
                displayBook();
            }
        });
    }

    private void setCover(final Book book) {
        final String fileName = Paths.internalTempDirectoryValue(this) + "/" + book.getSortKey() + ".png";
        File file = new File(fileName);
        if (file.exists()) {
            book.setMyCoverPath(fileName);
            return; // 不再执行
        }
        AndroidImageSynchronizer myImageSynchronizer = new AndroidImageSynchronizer(this);
        PluginCollection pluginCollection = PluginCollection.Instance(Paths.systemInfo(this));
        final ZLImage image = CoverUtil.getCover(book, pluginCollection);
        if (image instanceof ZLImageProxy) {
            ((ZLImageProxy) image).startSynchronization(myImageSynchronizer, new Runnable() {
                public void run() {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            final ZLAndroidImageData data = ((ZLAndroidImageManager) ZLAndroidImageManager.Instance()).getImageData(image);
                            if (data != null) {
                                final DisplayMetrics metrics = new DisplayMetrics();
                                getWindowManager().getDefaultDisplay().getMetrics(metrics);
                                final int maxHeight = metrics.heightPixels * 2 / 3;
                                final int maxWidth = maxHeight * 2 / 3;
                                final Bitmap coverBitmap = data.getBitmap(2 * maxWidth, 2 * maxHeight);
                                try {
                                    book.setMyCoverPath(fileName);
                                    saveMyBitmap(fileName, coverBitmap);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }
            });
        }
    }

    public void saveMyBitmap(String fileName, Bitmap mBitmap) throws IOException {
        File file = new File(fileName);
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        mBookAdapter.notifyDataSetChanged();
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.navigation_local_book:
                        break;
                    case R.id.navigation_net_book:
                        break;
                    case R.id.navigation_bookmark:
                        break;
                    case R.id.navigation_share_book:
                        break;
                }
                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();
                return true;
            }
        });
    }

    private void setUpProfileImage() {
        findViewById(R.id.profile_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawers();
                mNavigationView.getMenu().getItem(0).setChecked(true);
            }
        });
    }

    protected void reload() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
    }
}