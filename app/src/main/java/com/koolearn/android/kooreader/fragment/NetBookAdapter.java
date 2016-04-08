package com.koolearn.android.kooreader.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.koolearn.android.kooreader.book.Book;
import com.koolearn.klibrary.ui.android.R;
import com.koolearn.klibrary.ui.android.library.ZLAndroidLibrary;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * ******************************************
 * 作    者 ：  杨越
 * 版    本 ：  1.0
 * 创建日期 ：  2016/3/31
 * 描    述 ：
 * 修订历史 ：
 * ******************************************
 */
public class NetBookAdapter extends RecyclerView.Adapter<NetBookAdapter.ViewHolder> {
    private final int mBackground;
    private List<Book> mBooks = new ArrayList<Book>();
    private final TypedValue mTypedValue = new TypedValue();

    private static final int ANIMATED_ITEMS_COUNT = 4;

    private boolean animateItems = false;
    private int lastAnimatedPosition = -1;

    public NetBookAdapter(Context context) {
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
        mBackground = mTypedValue.resourceId;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ImageView ivBook;
        public TextView tvTitle;
        public TextView tvDesc;

        public int position;

        public ViewHolder(View v) {
            super(v);
            ivBook = (ImageView) v.findViewById(R.id.ivBook);
            tvTitle = (TextView) v.findViewById(R.id.tvTitle);
            tvDesc = (TextView) v.findViewById(R.id.tvDesc);
        }
    }


    private void runEnterAnimation(View view, int position) {
        if (!animateItems || position >= ANIMATED_ITEMS_COUNT - 1) {
            return;
        }

        if (position > lastAnimatedPosition) {
            lastAnimatedPosition = position;
            view.setTranslationY(ZLAndroidLibrary.Instance().getScreenHeight());
            view.animate()
                    .translationY(0)
                    .setStartDelay(100 * position)
                    .setInterpolator(new DecelerateInterpolator(3.f))
                    .setDuration(700)
                    .start();
        }
    }

    public void updateItems(List<Book> books, boolean animated) {
        animateItems = animated;
        lastAnimatedPosition = -1;
        mBooks.addAll(books);
        notifyDataSetChanged();
    }

    public void clearItems() {
        mBooks.clear();
        notifyDataSetChanged();
    }

    @Override
    public NetBookAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_net_book, parent, false);
        //v.setBackgroundResource(mBackground);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        runEnterAnimation(holder.itemView, position);
        Book book = mBooks.get(position);
        holder.tvTitle.setText(book.getTitle());
        String desc = "作者: " + book.getAuthor()
                + "\n出版年: " + book.getPubdate() + "\n页数: " + book.getPages() + "\n定价:" + book.getPrice();
        holder.tvDesc.setText(desc);
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.book_cover)
                .showImageOnFail(R.drawable.book_cover)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        ImageLoader.getInstance().displayImage(book.getImage(), holder.ivBook, options);
    }

    @Override
    public int getItemCount() {
        return mBooks.size();
    }

    public Book getBook(int pos) {
        return mBooks.get(pos);
    }
}