package com.koolearn.android.kooreader.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
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
import com.koolearn.android.util.LogUtil;
import com.koolearn.android.util.OrientationUtil;
import com.koolearn.klibrary.ui.android.R;
import com.koolearn.kooreader.book.Book;

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
//                while (bookshelf.size() < 2) {
//                    try {
//                        Thread.sleep(1000);
//                        bookshelf = myCollection.recentlyOpenedBooks(9);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
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
        mBookAdapter.setOnItemClickListener(new MyBookAdapter.OnItemClickLitener() {
            @Override
            public void onItemClick(View view, Book data) {
                KooReader.openBookActivity(getActivity(), data, null);
                getActivity().overridePendingTransition(R.anim.tran_fade_in, R.anim.tran_fade_out);
            }

            @Override
            public void onItemLongClick(final View view, final Book data, final int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("删除书籍?").setNegativeButton("取消", null);
                LogUtil.i8("position:" + position);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //                notifyItemInserted(position)与notifyItemRemoved(position)
                        myCollection.removeBook(data, true);
                        int position = bookshelf.indexOf(data);
                        bookshelf.remove(position);
                        recyclerView.removeViewAt(position);
                        mBookAdapter.notifyItemRemoved(position);
//                        mBookAdapter.notifyItemRemoved(0);
                        Snackbar.make(view, "删除成功", Snackbar.LENGTH_SHORT).show();
                    }
                });
                builder.show();
            }
        });
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