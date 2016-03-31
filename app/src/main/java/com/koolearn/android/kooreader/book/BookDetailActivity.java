package com.koolearn.android.kooreader.book;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.koolearn.android.kooreader.KooReader;
import com.koolearn.android.kooreader.fragment.DetailFragment;
import com.koolearn.android.kooreader.libraryService.BookCollectionShadow;
import com.koolearn.android.kooreader.view.DownloadProcessButton;
import com.koolearn.klibrary.ui.android.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.http.Header;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BookDetailActivity extends AppCompatActivity {
    private ViewPager mViewPager;
    private Book mBook;
    private DownloadProcessButton mBtnDownload;
    private Toolbar mToolbar;

    private final BookCollectionShadow myCollection = new BookCollectionShadow();
    private static AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);
    private static final String BOOK_PATH = "/mnt/sdcard/KooBook/";
    private String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appbar_detail);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mBtnDownload = (DownloadProcessButton) findViewById(R.id.btn_download);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        mBook = (Book) getIntent().getSerializableExtra("book");
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(mBook.getTitle());

        ImageView ivImage = (ImageView) findViewById(R.id.ivImage);
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.mipmap.book_cover)
                .showImageOnFail(R.mipmap.book_cover)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        ImageLoader.getInstance().displayImage(mBook.getImage(), ivImage, options);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(mViewPager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.addTab(tabLayout.newTab().setText("内容简介"));
        tabLayout.addTab(tabLayout.newTab().setText("作者简介"));
        tabLayout.addTab(tabLayout.newTab().setText("目录"));
        tabLayout.setupWithViewPager(mViewPager);

        filePath = BOOK_PATH + mBook.getTitle() + ".epub";
        File fileDir = new File(filePath);
        if (fileDir.exists()) {
            mBtnDownload.setProgress(100);
        }

        mBtnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBtnDownload.getProgress() == 100) {
                    startOpenBookByPath(filePath);
                } else {
                    mBtnDownload.setEnabled(false);
                    downloadBook();
                }
            }
        });
    }

    private void setupViewPager(ViewPager mViewPager) {
        MyPagerAdapter adapter = new MyPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(DetailFragment.newInstance(mBook.getSummary()), "内容简介");
        adapter.addFragment(DetailFragment.newInstance(mBook.getAuthor_intro()), "作者简介");
        adapter.addFragment(DetailFragment.newInstance(mBook.getCatalog()), "目录");
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(1, true);
    }

    static class MyPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }

    private void downloadBook() {
        File fileDir = new File(filePath);
        if (!fileDir.getParentFile().exists()) {
            fileDir.getParentFile().mkdirs();
        }

        // http://45.78.20.53:8080/read.epub

        client.get("http://file.bmob.cn/" + mBook.getUrl(), null, new FileAsyncHttpResponseHandler(fileDir) {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                Toast.makeText(BookDetailActivity.this, "下载失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, File file) {
                mBtnDownload.setProgress(100);
                mBtnDownload.setEnabled(true);
                Toast.makeText(BookDetailActivity.this, "下载成功", Toast.LENGTH_SHORT).show();
                startOpenBookByPath(file.getPath());
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                mBtnDownload.setProgress((int) ((bytesWritten * 1.0 / totalSize) * 100));
//                super.onProgress(bytesWritten, totalSize);
            }
        });
    }

    /**
     * 通过已经下载好的路径打开书
     *
     * @param bookPath
     */
    private void startOpenBookByPath(final String bookPath) {
        myCollection.bindToService(this, new Runnable() {
            public void run() {
                com.koolearn.kooreader.book.Book book = myCollection.getBookByFile(bookPath);
                openBook(book);
            }
        });
    }

    private void openBook(com.koolearn.kooreader.book.Book data) {
        KooReader.openBookActivity(this, data, null);
        overridePendingTransition(R.anim.tran_fade_in, R.anim.tran_fade_out);
    }

}