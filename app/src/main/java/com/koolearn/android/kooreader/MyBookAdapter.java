package com.koolearn.android.kooreader;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koolearn.android.kooreader.util.AndroidImageSynchronizer;
import com.koolearn.klibrary.core.image.ZLImage;
import com.koolearn.klibrary.core.image.ZLImageProxy;
import com.koolearn.klibrary.ui.android.R;
import com.koolearn.klibrary.ui.android.image.ZLAndroidImageData;
import com.koolearn.klibrary.ui.android.image.ZLAndroidImageManager;
import com.koolearn.kooreader.Paths;
import com.koolearn.kooreader.book.Book;
import com.koolearn.kooreader.book.CoverUtil;
import com.koolearn.kooreader.formats.PluginCollection;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
public class MyBookAdapter extends RecyclerView.Adapter<MyBookAdapter.ViewHolder> {
    private static List<Book> myBook;
    private static Activity context;

    public MyBookAdapter(Activity context, List<Book> myBook) {
        this.myBook = myBook;
        this.context = context;
    }

    //define interface
    public static interface OnItemClickLitener {
        void onItemClick(View view, Book data);

        void onItemLongClick(View view, Book data, int position);
    }

    private OnItemClickLitener mOnItemClickListener = null;

    public void setOnItemClickListener(OnItemClickLitener listener) {
        this.mOnItemClickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_staggered_grid, parent, false);
        //将创建的View注册点击事件
//        view.setOnClickListener(this);
//        view.setOnLongClickListener(this);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.setData(position);
        holder.itemView.setTag(myBook.get(position));

        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(v, (Book) v.getTag());
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickListener.onItemLongClick(holder.itemView, (Book) v.getTag(), pos);
                    return false;
                }
            });
        }
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

        public ViewHolder(View view) {
            super(view);
            icon = (ImageView) view.findViewById(R.id.pic);
            name = (TextView) view.findViewById(R.id.name);
        }

        public void setData(final int position) {
            name.setText((myBook.get(position).getTitle()));
            icon.setImageResource(R.mipmap.book_cover);
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.mipmap.book_cover)
                    .showImageOnFail(R.mipmap.book_cover)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();
            final String fileName = Paths.internalTempDirectoryValue(context) + "/" + myBook.get(position).getSortKey() + ".png";
            File file = new File(fileName);
            if (file.exists()) {
                ImageLoader.getInstance().displayImage("file://" + fileName, icon, options);
            } else {
                AndroidImageSynchronizer myImageSynchronizer = new AndroidImageSynchronizer(context);
                PluginCollection pluginCollection = PluginCollection.Instance(Paths.systemInfo(context));
                final ZLImage image = CoverUtil.getCover(myBook.get(position), pluginCollection);
                if (image instanceof ZLImageProxy) {
                    ((ZLImageProxy) image).startSynchronization(myImageSynchronizer, new Runnable() {
                        public void run() {
                            context.runOnUiThread(new Runnable() {
                                public void run() {
                                    final ZLAndroidImageData data = ((ZLAndroidImageManager) ZLAndroidImageManager.Instance()).getImageData(image);
                                    if (data != null) {
                                        DisplayMetrics metrics = new DisplayMetrics();
                                        context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
                                        int maxHeight = metrics.heightPixels * 2 / 3;
                                        int maxWidth = maxHeight * 2 / 3;
                                        final Bitmap coverBitmap = data.getBitmap(2 * maxWidth, 2 * maxHeight);
                                        icon.setImageBitmap(coverBitmap);
                                        new Thread() {
                                            @Override
                                            public void run() {
                                                super.run();
                                                try {
                                                    saveMyBitmap(fileName, coverBitmap, myBook.get(position));
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }.start();
                                    }
                                }
                            });
                        }
                    });

                }
            }
        }
    }

    private static void saveMyBitmap(String fileName, Bitmap mBitmap, Book book) throws IOException {
        File file = new File(fileName);
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
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
}