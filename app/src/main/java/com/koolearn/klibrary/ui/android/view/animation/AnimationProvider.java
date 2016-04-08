package com.koolearn.klibrary.ui.android.view.animation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.koolearn.android.util.LogUtil;
import com.koolearn.klibrary.core.library.ZLibrary;
import com.koolearn.klibrary.core.view.ZLViewEnums;

import java.util.LinkedList;
import java.util.List;

public abstract class AnimationProvider {
    public static enum Mode {
        NoScrolling(false),
        PreManualScrolling(false),
        ManualScrolling(false), // 手动滚动
        AnimatedScrollingForward(true), // 向前滚动 变为向前滚动时,mode变为auto
        AnimatedScrollingBackward(true); // 向后滚动

        public final boolean Auto;

        Mode(boolean auto) {
            Auto = auto;
        }
    }

    private Mode myMode = Mode.NoScrolling;

    private final BitmapManager myBitmapManager;
    protected int myStartX;
    protected int myStartY;
    protected int myEndX;
    protected int myEndY;
    protected ZLViewEnums.Direction myDirection;
    protected float mySpeed;

    protected int myWidth;
    protected int myHeight;
    protected Integer myColorLevel;

    protected AnimationProvider(BitmapManager bitmapManager) {
        myBitmapManager = bitmapManager;
    }

    public Mode getMode() {
        return myMode;
    }

    public final void terminate() {
        myMode = Mode.NoScrolling;
        mySpeed = 0;
        myDrawInfos.clear();
    }

    public final void startManualScrolling(int x, int y) {
        if (!myMode.Auto) {
            myMode = Mode.PreManualScrolling;
            myEndX = myStartX = x;
            myEndY = myStartY = y;
        }
    }

    // 1. 都没达到 一直返回PreManualScrolling
    // 2. 达到滑动要求 返回ManualScrolling
    // 3. 达到不滑动要求 返回NoScrolling
    private final Mode detectManualMode() {
        final int dX = Math.abs(myStartX - myEndX);
        final int dY = Math.abs(myStartY - myEndY);
        if (myDirection.IsHorizontal) {
            if (dY > ZLibrary.Instance().getDisplayDPI() / 2 && dY > dX) {
                return Mode.NoScrolling;
            } else if (dX > ZLibrary.Instance().getDisplayDPI() / 10) {
                return Mode.ManualScrolling;
            }
        } else {
            if (dX > ZLibrary.Instance().getDisplayDPI() / 2 && dX > dY) {
                return Mode.NoScrolling;
            } else if (dY > ZLibrary.Instance().getDisplayDPI() / 10) {
                return Mode.ManualScrolling;
            }
        }
        return Mode.PreManualScrolling;
    }

    public final void scrollTo(int x, int y) {
        LogUtil.i13("myMode" + ":" + myMode + ":" + x + ":" + y);
        switch (myMode) {
            case ManualScrolling:
                myEndX = x;
                myEndY = y;
                break;
            case PreManualScrolling:
                myEndX = x;
                myEndY = y;
                myMode = detectManualMode();
                break;
        }
    }

    public final void startAnimatedScrolling(int x, int y, int speed) {
        if (myMode != Mode.ManualScrolling) {
            return;
        }

        if (getPageToScrollTo(x, y) == ZLViewEnums.PageIndex.current) {
            return;
        }

        final int dpi = ZLibrary.Instance().getDisplayDPI();
        final int diff = myDirection.IsHorizontal ? x - myStartX : y - myStartY;
        final int minDiff = myDirection.IsHorizontal
                ? (myWidth > myHeight ? myWidth / 4 : myWidth / 3)
                : (myHeight > myWidth ? myHeight / 4 : myHeight / 3);
        boolean forward = Math.abs(diff) > Math.min(minDiff, dpi / 2);

        myMode = forward ? Mode.AnimatedScrollingForward : Mode.AnimatedScrollingBackward;

        float velocity = 15;
        if (myDrawInfos.size() > 1) {
            int duration = 0;
            for (DrawInfo info : myDrawInfos) {
                duration += info.Duration;
            }
            duration /= myDrawInfos.size();
            final long time = System.currentTimeMillis();
            myDrawInfos.add(new DrawInfo(x, y, time, time + duration));
            velocity = 0;
            for (int i = 1; i < myDrawInfos.size(); ++i) {
                final DrawInfo info0 = myDrawInfos.get(i - 1);
                final DrawInfo info1 = myDrawInfos.get(i);
                final float dX = info0.X - info1.X;
                final float dY = info0.Y - info1.Y;
                velocity += Math.sqrt(dX * dX + dY * dY) / Math.max(1, info1.Start - info0.Start);
            }
            velocity /= myDrawInfos.size() - 1;
            velocity *= duration;
            velocity = Math.min(100, Math.max(15, velocity));
        }
        myDrawInfos.clear();

        if (getPageToScrollTo() == ZLViewEnums.PageIndex.previous) {
            forward = !forward;
        }

        switch (myDirection) {
            case up:
            case rightToLeft:
                mySpeed = forward ? -velocity : velocity;
                break;
            case leftToRight:
            case down:
                mySpeed = forward ? velocity : -velocity;
                break;
        }

        startAnimatedScrollingInternal(speed);
    }

    public void startAnimatedScrolling(ZLViewEnums.PageIndex pageIndex, Integer x, Integer y, int speed) {
        if (myMode.Auto) {
            return;
        }

        terminate();
        myMode = Mode.AnimatedScrollingForward;

        switch (myDirection) {
            case up:
            case rightToLeft:
                mySpeed = pageIndex == ZLViewEnums.PageIndex.next ? -15 : 15;
                break;
            case leftToRight:
            case down:
                mySpeed = pageIndex == ZLViewEnums.PageIndex.next ? 15 : -15;
                break;
        }
        setupAnimatedScrollingStart(x, y);
        startAnimatedScrollingInternal(speed);
    }

    protected abstract void startAnimatedScrollingInternal(int speed);

    protected abstract void setupAnimatedScrollingStart(Integer x, Integer y);

    public boolean inProgress() {
        switch (myMode) {
            case NoScrolling:
            case PreManualScrolling:
                return false;
            default:
                return true;
        }
    }

    protected int getScrollingShift() {
        return myDirection.IsHorizontal ? myEndX - myStartX : myEndY - myStartY;
    }

    public final void setup(ZLViewEnums.Direction direction, int width, int height, Integer colorLevel) {
        myDirection = direction;
        myWidth = width;
        myHeight = height;
        myColorLevel = colorLevel;
    }

    public abstract void doStep();

    public int getScrolledPercent() {
        final int full = myDirection.IsHorizontal ? myWidth : myHeight;
        final int shift = Math.abs(getScrollingShift());
        return 100 * shift / full;
    }

    static class DrawInfo {
        final int X, Y;
        final long Start;
        final int Duration;

        DrawInfo(int x, int y, long start, long finish) {
            X = x;
            Y = y;
            Start = start;
            Duration = (int) (finish - start);
        }
    }

    final private List<DrawInfo> myDrawInfos = new LinkedList<DrawInfo>();

    public final void draw(Canvas canvas) {
        final long start = System.currentTimeMillis();
        setFilter();
        drawInternal(canvas);
        myDrawInfos.add(new DrawInfo(myEndX, myEndY, start, System.currentTimeMillis())); // 保存位置信息
        if (myDrawInfos.size() > 3) {
            myDrawInfos.remove(0);
        }
    }

//    public final void drawFooterBitmap(Canvas canvas, Bitmap footerBitmap, int voffset) {
//        setFilter();
//        drawFooterBitmapInternal(canvas, footerBitmap, voffset);
//    }

    protected abstract void setFilter();

    protected abstract void drawInternal(Canvas canvas);

//    protected abstract void drawFooterBitmapInternal(Canvas canvas, Bitmap footerBitmap, int voffset);

    public abstract ZLViewEnums.PageIndex getPageToScrollTo(int x, int y);

    /**
     * 传入endX 与startX进行对比,来判断 往前翻页 还是 往后翻页
     */
    public final ZLViewEnums.PageIndex getPageToScrollTo() {
        return getPageToScrollTo(myEndX, myEndY);
    }

    public Bitmap getBitmapFrom() {
        return myBitmapManager.getBitmap(ZLViewEnums.PageIndex.current);
    }

    public Bitmap getBitmapTo() {
        return myBitmapManager.getBitmap(getPageToScrollTo());
    }

    // added by leixun
    public Bitmap getBitmapByPageIndex(ZLViewEnums.PageIndex index){
        return myBitmapManager.getBitmap(index);
    }

    // true 左边翻到右边 ；false 右边翻到左边 added by leixun
    protected void forwardShift(boolean params){
        myBitmapManager.shift(params);
    }

    protected void drawBitmapFrom(Canvas canvas, int x, int y, Paint paint) {
        myBitmapManager.drawBitmap(canvas, x, y, ZLViewEnums.PageIndex.current, paint);
    }

    protected void drawBitmapTo(Canvas canvas, int x, int y, Paint paint) {
        myBitmapManager.drawBitmap(canvas, x, y, getPageToScrollTo(), paint);
    }

}