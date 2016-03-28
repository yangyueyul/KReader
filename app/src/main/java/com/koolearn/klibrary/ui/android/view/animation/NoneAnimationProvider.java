package com.koolearn.klibrary.ui.android.view.animation;

import android.graphics.*;

import com.koolearn.android.util.LogUtil;
import com.koolearn.klibrary.core.view.ZLViewEnums;

import com.koolearn.klibrary.ui.android.view.ViewUtil;

public final class NoneAnimationProvider extends AnimationProvider {
    private final Paint myPaint = new Paint();

    public NoneAnimationProvider(BitmapManager bitmapManager) {
        super(bitmapManager);
    }

    @Override
    protected void drawInternal(Canvas canvas) {
        drawBitmapFrom(canvas, 0, 0, myPaint);
    }

    @Override
    public void doStep() {
        if (getMode().Auto) {
            terminate();
        }
    }

    @Override
    protected void setupAnimatedScrollingStart(Integer x, Integer y) {
        if (myDirection.IsHorizontal) {
            myStartX = mySpeed < 0 ? myWidth : 0;
            myEndX = myWidth - myStartX;
            myEndY = myStartY = 0;
        } else {
            myEndX = myStartX = 0;
            myStartY = mySpeed < 0 ? myHeight : 0;
            myEndY = myHeight - myStartY;
        }
    }

    @Override
    protected void startAnimatedScrollingInternal(int speed) {
    }

    // 是否翻页
    @Override
    public ZLViewEnums.PageIndex getPageToScrollTo(int x, int y) {
        if (myDirection == null) {
            LogUtil.i13("myDirection:");
            return ZLViewEnums.PageIndex.current;
        }
        //y 判断翻前/后/不翻页
        LogUtil.i13("myDirection:"+myDirection);
        switch (myDirection) {
            case rightToLeft:
                return myStartX < x ? ZLViewEnums.PageIndex.previous : ZLViewEnums.PageIndex.next;
            case leftToRight:
                return myStartX < x ? ZLViewEnums.PageIndex.next : ZLViewEnums.PageIndex.previous;
            case up:
                return myStartY < y ? ZLViewEnums.PageIndex.previous : ZLViewEnums.PageIndex.next;
            case down:
                return myStartY < y ? ZLViewEnums.PageIndex.next : ZLViewEnums.PageIndex.previous;
        }
        return ZLViewEnums.PageIndex.current;
    }

//    @Override
//    public void drawFooterBitmapInternal(Canvas canvas, Bitmap footerBitmap, int voffset) {
//        canvas.drawBitmap(footerBitmap, 0, voffset, myPaint);
//    }

    @Override
    protected void setFilter() {
        ViewUtil.setColorLevel(myPaint, myColorLevel);
    }
}
