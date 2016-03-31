package com.koolearn.android.kooreader.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.koolearn.android.kooreader.util.AndroidImageSynchronizer;
import com.koolearn.android.util.LogUtil;
import com.koolearn.klibrary.core.image.ZLImage;
import com.koolearn.klibrary.core.image.ZLImageProxy;
import com.koolearn.klibrary.ui.android.R;
import com.koolearn.klibrary.ui.android.image.ZLAndroidImageData;
import com.koolearn.klibrary.ui.android.image.ZLAndroidImageManager;
import com.koolearn.klibrary.ui.android.library.ZLAndroidLibrary;
import com.koolearn.kooreader.Paths;
import com.koolearn.kooreader.book.Book;
import com.koolearn.kooreader.book.CoverUtil;
import com.koolearn.kooreader.formats.PluginCollection;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

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
 * 创建日期 ：  2015/12/13
 * 描    述 ：
 * 修订历史 ：
 * ******************************************
 */
public class LocalBookAdapter extends RecyclerView.Adapter<LocalBookAdapter.ViewHolder> {
    private List<Book> mBooks = new ArrayList<Book>();
    private Activity context;
    private String cacheDir;


    private int ANIMATED_ITEMS_COUNT = 20;

    private boolean animateItems = false;
    private int lastAnimatedPosition = -1;


    public LocalBookAdapter(Activity context) {
        this.context = context;
        cacheDir = getExternalCacheDirPath();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView icon;
        public TextView name;

        public ViewHolder(View view) {
            super(view);
            icon = (ImageView) view.findViewById(R.id.pic);
            name = (TextView) view.findViewById(R.id.name);
        }
    }

    private void runEnterAnimation(View view, int position) {
        if (!animateItems) {
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

    public void removeItems(int position) {
        mBooks.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public LocalBookAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_local_book, parent, false);
        //v.setBackgroundResource(mBackground);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        runEnterAnimation(holder.itemView, position);
        final Book book = mBooks.get(position);
        holder.name.setText(book.getTitle());

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.book_cover)
                .showImageOnFail(R.drawable.book_cover)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        ImageLoader.getInstance().displayImage("file://" + cacheDir + "/" + book.getSortKey() + ".png", holder.icon, options, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {

            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                setCoverCache(book);

            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {

            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        });
        holder.itemView.setTag(mBooks.get(position));
    }

    private String getExternalCacheDirPath() {
        File d = context.getExternalCacheDir();
        if (d != null) {
            d.mkdirs();
            if (d.exists() && d.isDirectory()) {
                return d.getPath();
            }
        }
        return null;
    }

    private void setCoverCache(final Book book) {
        final String fileName = getExternalCacheDirPath() + "/" + book.getSortKey() + ".png";
        File file = new File(fileName);
        if (file.exists()) {
            return;
        }
        AndroidImageSynchronizer myImageSynchronizer = new AndroidImageSynchronizer(context);
        PluginCollection pluginCollection = PluginCollection.Instance(Paths.systemInfo(context));
        final ZLImage image = CoverUtil.getCover(book, pluginCollection);
        if (image instanceof ZLImageProxy) {
            ((ZLImageProxy) image).startSynchronization(myImageSynchronizer, new Runnable() {
                public void run() {
                    context.runOnUiThread(new Runnable() {
                        public void run() {
                            ZLAndroidImageData data = ((ZLAndroidImageManager) ZLAndroidImageManager.Instance()).getImageData(image);
                            if (data != null) {
                                DisplayMetrics metrics = new DisplayMetrics();
                                context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
                                int maxHeight = metrics.heightPixels * 1 / 3;
                                int maxWidth = maxHeight * 1 / 3;
                                Bitmap coverBitmap = data.getBitmap(maxWidth, maxHeight);
                                try {
                                    saveMyBitmap(fileName, coverBitmap, book);
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

    //获取数据的数量
    @Override
    public int getItemCount() {
        return mBooks.size();
    }

    public Book getBook(int pos) {
        return mBooks.get(pos);
    }

    private void saveMyBitmap(String fileName, Bitmap mBitmap, Book book) throws IOException {
        File file = new File(fileName);
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mBitmap.compress(Bitmap.CompressFormat.PNG, 50, fOut);
        notifyDataSetChanged();
        LogUtil.i8("fileNameyyyyyyyyyyy:" + file.getPath());
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