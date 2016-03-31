package com.koolearn.android.kooreader.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.koolearn.android.kooreader.KooReader;
import com.koolearn.android.kooreader.MyBookAdapter;
import com.koolearn.android.kooreader.animation.SlideInLeftAnimator;
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
import java.util.ArrayList;
import java.util.List;


/**
 * ******************************************
 * 作    者 ：  杨越
 * 版    本 ：  1.0
 * 创建日期 ：  2016/3/29 ${time}
 * 描    述 ：
 * 修订历史 ：
 * ******************************************
 */
public class LocalBooksFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private FloatingActionButton mFabSearch;
    private ProgressBar mProgressBar;

    private List<Book> bookshelf = new ArrayList<>();
    private MyBookAdapter mBookAdapter;
    private final BookCollectionShadow myCollection = new BookCollectionShadow();

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            refreshLayout.setRefreshing(false);
            updateUI();
        }
    };
    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_local_books, null);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);

        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        getBooks();

        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeResources(R.color.progressBarBlue, R.color.progressBarBgWhiteOrange);
        refreshLayout.setProgressBackgroundColor(R.color.progressBarBgGreen);
        Toast.makeText(getActivity(), Environment.getExternalStorageDirectory()+"",Toast.LENGTH_LONG).show();
        setUpFAB(view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getBooks();
    }

    private void getBooks() {
        myCollection.bindToService(getActivity(), new Runnable() {
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

    private void displayBook() {
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setItemAnimator(new SlideInLeftAnimator());
        recyclerView.getItemAnimator().setAddDuration(1000);
        recyclerView.getItemAnimator().setRemoveDuration(1000);
        recyclerView.getItemAnimator().setMoveDuration(1000);
        recyclerView.getItemAnimator().setChangeDuration(1000);

        recyclerView.setAdapter(mBookAdapter);
        mBookAdapter = new MyBookAdapter(getActivity(), bookshelf);
        mBookAdapter.setOnItemClickListener(new MyBookAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, Book data) {
                KooReader.openBookActivity(getActivity(), data, null);
                getActivity().overridePendingTransition(R.anim.tran_fade_in, R.anim.tran_fade_out);
            }
        });
    }

    private void setCover(final Book book) {
        final String fileName = Paths.internalTempDirectoryValue(getActivity()) + "/" + book.getSortKey() + ".png";
        File file = new File(fileName);
        if (file.exists()) {
            book.setMyCoverPath(fileName);
            return; // 不再执行
        }
        AndroidImageSynchronizer myImageSynchronizer = new AndroidImageSynchronizer(getActivity());
        PluginCollection pluginCollection = PluginCollection.Instance(Paths.systemInfo(getActivity()));
        final ZLImage image = CoverUtil.getCover(book, pluginCollection);
        if (image instanceof ZLImageProxy) {
            ((ZLImageProxy) image).startSynchronization(myImageSynchronizer, new Runnable() {
                public void run() {
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            final ZLAndroidImageData data = ((ZLAndroidImageManager) ZLAndroidImageManager.Instance()).getImageData(image);
                            if (data != null) {
                                final DisplayMetrics metrics = new DisplayMetrics();
                                getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
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
        updateUI();
//        mBookAdapter.notifyDataSetChanged();
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mFabSearch.setTranslationY(2 * getResources().getDimensionPixelOffset(R.dimen.btn_fab_size));
        startFABAnimation();
    }

    private void setUpFAB(View view) {
        mFabSearch = (FloatingActionButton) view.findViewById(R.id.fab_search);
        mFabSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OrientationUtil.startActivity(getActivity(), new Intent(getActivity(), LibraryActivity.class));
                getActivity().overridePendingTransition(R.anim.tran_fade_in, R.anim.tran_fade_out);
            }
        });
    }

    private void startFABAnimation() {
        mFabSearch.animate()
                .translationY(0)
                .setInterpolator(new OvershootInterpolator(1.f))
                .setStartDelay(500)
                .setDuration(400)
                .start();
    }

    private void updateUI(){
        mBookAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myCollection.unbind();
    }

    @Override
    public void onRefresh() {
        handler.sendEmptyMessageDelayed(1, 2000);
    }
}