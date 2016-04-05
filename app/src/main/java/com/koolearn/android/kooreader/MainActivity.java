package com.koolearn.android.kooreader;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.koolearn.android.kooreader.fragment.BookMarkFragment;
import com.koolearn.android.kooreader.fragment.BookNoteFragment;
import com.koolearn.android.kooreader.fragment.NetWorkBooksFragment;
import com.koolearn.android.kooreader.fragment.LocalBooksFragment;
import com.koolearn.klibrary.ui.android.R;
import com.koolearn.kooreader.Paths;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private Timer timer = null;
    private TimerTask timeTask = null;
    private boolean isExit = false;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        setUpProfileImage();

        timer = new Timer();
        copyBooks();
        switchNetWorkBook();
    }

    private void switchToLocalBook() {
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_content, new LocalBooksFragment()).commit();
        mToolbar.setTitle(R.string.local_book);
    }

    private void switchNetWorkBook() {
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_content, new NetWorkBooksFragment()).commit();
        mToolbar.setTitle(R.string.network_book);
    }

    private void switchToBookNote() {
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_content, new BookNoteFragment()).commit();
        mToolbar.setTitle(R.string.book_note);
    }

    private void switchToBookMark() {
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_content, new BookMarkFragment()).commit();
        mToolbar.setTitle(R.string.book_mark);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            doubleExit();
        }
    }

    /**
     * DoubleExit
     */
    private void doubleExit() {
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

    /**
     * 字体拷贝
     */
    private void copyFonts(String fontName) {
        File destFile = new File(getFilesDir(), fontName);
        if (destFile.exists()) {
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
//                copyEpub("harry.epub");
//                copyEpub("abeaver.epub");
//                copyEpub("silverchair.epub");

//                copyEpub("ExaminationCloze.doc");
//                copyEpub("function.doc");
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
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

    private void setUpProfileImage() {
        findViewById(R.id.profile_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.navigation_local_book) {
            switchToLocalBook();

        } else if (id == R.id.navigation_net_book) {
            switchNetWorkBook();

        } else if (id == R.id.navigation_book_note) {
            switchToBookNote();

        } else if (id == R.id.navigation_bookmark) {
            switchToBookMark();
        }

        mDrawerLayout.closeDrawers();
        return true;
    }
}