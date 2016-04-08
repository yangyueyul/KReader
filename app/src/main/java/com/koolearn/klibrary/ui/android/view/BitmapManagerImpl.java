package com.koolearn.klibrary.ui.android.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.koolearn.klibrary.core.util.SystemInfo;
import com.koolearn.klibrary.core.view.ZLView;
import com.koolearn.klibrary.ui.android.view.animation.BitmapManager;
import com.koolearn.kooreader.Paths;

public final class BitmapManagerImpl implements BitmapManager {
    private final int SIZE = 2;
    private final Bitmap[] myBitmaps = new Bitmap[SIZE];
    private final ZLView.PageIndex[] myIndexes = new ZLView.PageIndex[SIZE];

    private int myWidth;
    private int myHeight;

    private final SystemInfo mySystemInfo;
    private ZLAndroidWidget myWidget;

    public BitmapManagerImpl(ZLAndroidWidget widget, Context context) {
        this.myWidget = widget;
        mySystemInfo = Paths.systemInfo(context); // 缓存相关
    }

    public void setSize(int w, int h) {
        if (myWidth != w || myHeight != h) {
            myWidth = w;
            myHeight = h;
            for (int i = 0; i < SIZE; ++i) {
                myBitmaps[i] = null;
                myIndexes[i] = null;
            }
            System.gc();
            System.gc();
            System.gc();
        }
    }

    public Bitmap getBitmap(ZLView.PageIndex index) {
        for (int i = 0; i < SIZE; ++i) {
            if (index == myIndexes[i]) {
                return myBitmaps[i];
            }
        }
        final int iIndex = getInternalIndex(index);
        myIndexes[iIndex] = index;
        if (myBitmaps[iIndex] == null) {
            try {
                myBitmaps[iIndex] = Bitmap.createBitmap(myWidth, myHeight, Bitmap.Config.RGB_565);
            } catch (OutOfMemoryError e) {
                System.gc();
                System.gc();
                myBitmaps[iIndex] = Bitmap.createBitmap(myWidth, myHeight, Bitmap.Config.RGB_565);
            }
        }
        // 在Bitmap上绘制,传入一张空白的bitmap,和当前的index
        myWidget.drawOnBitmap(myBitmaps[iIndex], index);
//        /**
//         * 这个view是自定义类型的KooView
//         */
//        final ZLView view = ZLApplication.Instance().getCurrentView();
//        final ZLAndroidPaintContext context = new ZLAndroidPaintContext(
//                mySystemInfo,
//                new Canvas(myBitmaps[iIndex]),
//                new ZLAndroidPaintContext.Geometry(
//                        ZLAndroidLibrary.Instance().getScreenWidth(),
//                        ZLAndroidLibrary.Instance().getScreenHeight(),
//                        ZLAndroidLibrary.Instance().getScreenWidth(),
//                        ZLAndroidLibrary.Instance().getScreenHeight(),
//                        0,
//                        0
//                ), 0);
//        /**
//         * 把ZLAndroidWigetPaintContext与Bitmap进行绑定
//         * 向myBitmaps[index]上画bitmap
//         * 在ZLAndroidWidget绘制文字等信息
//         */
//        view.paint(context, index);
//        /**
//         * 将绘制好的Bitmap返回
//         */
        return myBitmaps[iIndex];
    }

    public void drawBitmap(Canvas canvas, int x, int y, ZLView.PageIndex index, Paint paint) {
        canvas.drawBitmap(getBitmap(index), x, y, paint);
    }

    private int getInternalIndex(ZLView.PageIndex index) {
        for (int i = 0; i < SIZE; ++i) {
            if (myIndexes[i] == null) {
                return i;
            }
        }
        for (int i = 0; i < SIZE; ++i) {
            if (myIndexes[i] != ZLView.PageIndex.current) {
                return i;
            }
        }
        throw new RuntimeException("That's impossible");
    }

    public void reset() {
        for (int i = 0; i < SIZE; ++i) {
            myIndexes[i] = null;
        }
    }

    @Override
    public void shift(boolean forward) {
        for (int i = 0; i < SIZE; ++i) {
            if (myIndexes[i] == null) {
                continue;
            }
            myIndexes[i] = forward ? myIndexes[i].getPrevious() : myIndexes[i].getNext();
        }
    }
}