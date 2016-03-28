package com.koolearn.android.kooreader;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koolearn.android.util.LogUtil;
import com.koolearn.klibrary.ui.android.R;
import com.koolearn.kooreader.book.Book;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * ******************************************
 * 作    者 ：  杨越
 * 版    本 ：  1.0
 * 创建日期 ：  2015/12/13
 * 描    述 ：
 * 修订历史 ：
 * ******************************************
 */
public class MyBookAdapter extends RecyclerView.Adapter<MyBookAdapter.ViewHolder> implements View.OnClickListener {
    private static List<Book> myBook;
    private static Activity context;

    public MyBookAdapter(Activity context, List<Book> myBook) {
        this.myBook = myBook;
        this.context = context;
    }

    private OnRecyclerViewItemClickListener mOnItemClickListener = null;

    //define interface
    public static interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, Book data);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_staggered_grid, parent, false);
        //将创建的View注册点击事件
        view.setOnClickListener(this);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setData(position);
        holder.itemView.setTag(myBook.get(position));
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取数据
            mOnItemClickListener.onItemClick(v, (Book) v.getTag());
        }
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }


    //获取数据的数量
    @Override
    public int getItemCount() {
        return myBook.size();
    }

    //自定义的ViewHolder，持有每个Item的的所有界面元素
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView icon;
        public TextView name;
//        public ImageView picShallow;

        public ViewHolder(View view) {
            super(view);
            icon = (ImageView) view.findViewById(R.id.pic);
            name = (TextView) view.findViewById(R.id.name);
//            picShallow = (ImageView) view.findViewById(R.id.pic_shallow);
        }

        public void setData(int position) {
            name.setText((myBook.get(position).getTitle()));
            icon.setImageResource(R.mipmap.book_cover);
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.mipmap.book_cover)
                    .showImageOnFail(R.mipmap.book_cover)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();
            String myCoverPath = myBook.get(position).getMyCoverPath();
            LogUtil.i18("myCoverPath" + myCoverPath);
            if (myCoverPath != null) {
                ImageLoader.getInstance().displayImage("file://" + myCoverPath, icon, options);
//                name.setText("");
//                picShallow.setVisibility(View.VISIBLE);
            }
        }
    }
}