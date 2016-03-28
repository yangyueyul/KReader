//package com.koolearn.klibrary.ui.android.view;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.Canvas;
//import android.graphics.Paint;
//
//import com.koolearn.klibrary.core.application.ZLApplication;
//import com.koolearn.klibrary.core.util.SystemInfo;
//import com.koolearn.klibrary.core.view.ZLView;
//import com.koolearn.klibrary.ui.android.library.ZLAndroidLibrary;
//import com.koolearn.klibrary.ui.android.view.animation.BitmapManager;
//import com.koolearn.kooreader.Paths;
//
//public final class BitmapManagerCurlImpl implements BitmapManager {
//    private final int SIZE = 2;
//    private final Bitmap[] myBitmaps = new Bitmap[SIZE];
//    private final ZLView.PageIndex[] myIndexes = new ZLView.PageIndex[SIZE];
//
//    private int myWidth;
//    private int myHeight;
//
//    private final ZLAndroidCurlWidget myWidget;
//    private final SystemInfo mySystemInfo;
//
//
//    public BitmapManagerCurlImpl(ZLAndroidCurlWidget widget, Context context) {
//        myWidget = widget;
//        mySystemInfo = Paths.systemInfo(context); // 缓存相关
//    }
//
//    public void setSize(int w, int h) {
//        if (myWidth != w || myHeight != h) {
//            myWidth = w;
//            myHeight = h;
//            for (int i = 0; i < SIZE; ++i) {
//                myBitmaps[i] = null;
//                myIndexes[i] = null;
//            }
//            System.gc();
//            System.gc();
//            System.gc();
//        }
//    }
//
//    public Bitmap getBitmap(ZLView.PageIndex index) {
//        for (int i = 0; i < SIZE; ++i) {
//            if (index == myIndexes[i]) {
//                return myBitmaps[i];
//            }
//        }
//        final int iIndex = getInternalIndex(index);
//        myIndexes[iIndex] = index;
//        if (myBitmaps[iIndex] == null) {
//            try {
//                myBitmaps[iIndex] = Bitmap.createBitmap(myWidth, myHeight, Bitmap.Config.RGB_565);
//            } catch (OutOfMemoryError e) {
//                System.gc();
//                System.gc();
//                myBitmaps[iIndex] = Bitmap.createBitmap(myWidth, myHeight, Bitmap.Config.RGB_565);
//            }
//        }
////        myWidget.drawOnBitmap(myBitmaps[iIndex], index);
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
//
//        final ZLAndroidPaintContext contextFoot = new ZLAndroidPaintContext(
//                mySystemInfo,
//                new Canvas(myBitmaps[iIndex]),
//                new ZLAndroidPaintContext.Geometry(
//                        ZLAndroidLibrary.Instance().getScreenWidth(),
//                        ZLAndroidLibrary.Instance().getScreenHeight(),
//                        ZLAndroidLibrary.Instance().getScreenWidth(),
//                        ZLAndroidLibrary.Instance().getScreenHeight(),
//                        0,
//                        ZLAndroidLibrary.Instance().getScreenHeight()
//                ), 0);
//        view.paint(context, contextFoot, index);
//        return myBitmaps[iIndex];
//    }
//
//    public void drawBitmap(Canvas canvas, int x, int y, ZLView.PageIndex index, Paint paint) {
//        canvas.drawBitmap(getBitmap(index), x, y, paint);
//    }
//
//    private int getInternalIndex(ZLView.PageIndex index) {
//        for (int i = 0; i < SIZE; ++i) {
//            if (myIndexes[i] == null) {
//                return i;
//            }
//        }
//        for (int i = 0; i < SIZE; ++i) {
//            if (myIndexes[i] != ZLView.PageIndex.current) {
//                return i;
//            }
//        }
//        throw new RuntimeException("That's impossible");
//    }
//
//    public void reset() {
//        for (int i = 0; i < SIZE; ++i) {
//            myIndexes[i] = null;
//        }
//    }
//
//    public void shift(boolean forward) {
//        for (int i = 0; i < SIZE; ++i) {
//            if (myIndexes[i] == null) {
//                continue;
//            }
//            myIndexes[i] = forward ? myIndexes[i].getPrevious() : myIndexes[i].getNext();
//        }
//    }
//}