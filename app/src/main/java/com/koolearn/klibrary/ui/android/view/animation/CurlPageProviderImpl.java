package com.koolearn.klibrary.ui.android.view.animation;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.koolearn.klibrary.core.application.ZLApplication;
import com.koolearn.klibrary.core.view.ZLView;
import com.koolearn.klibrary.core.view.ZLViewEnums;
import com.koolearn.klibrary.ui.android.curl.CurlPage;
import com.koolearn.klibrary.ui.android.curl.CurlView;

public class CurlPageProviderImpl extends AnimationProvider implements PageProvider {

    private CurlView curlView;
    private Bitmap myBitmap;

    public CurlPageProviderImpl(BitmapManager bitmapManager) {
        super(bitmapManager);
    }

    @Override
    protected void drawInternal(Canvas canvas) {

    }

    @Override
    public ZLViewEnums.PageIndex getPageToScrollTo(int x, int y) {
        if (myDirection == null) {
            return ZLViewEnums.PageIndex.current;
        }
        switch (myDirection) {
            case leftToRight:
                return myStartX < myWidth / 2 ? ZLViewEnums.PageIndex.next : ZLViewEnums.PageIndex.previous;
            case rightToLeft:
                return myStartX < myWidth / 2 ? ZLViewEnums.PageIndex.previous : ZLViewEnums.PageIndex.next;
            case up:
                return myStartY < myHeight / 2 ? ZLViewEnums.PageIndex.previous : ZLViewEnums.PageIndex.next;
            case down:
                return myStartY < myHeight / 2 ? ZLViewEnums.PageIndex.next : ZLViewEnums.PageIndex.previous;
        }
        return ZLViewEnums.PageIndex.current;
    }

    @Override
    protected void startAnimatedScrollingInternal(int speed) {
//        mySpeedFactor = (float) Math.pow(2.0, 0.25 * speed);
        mySpeed *= 1.5;
        doStep();
    }

    @Override
    protected void setupAnimatedScrollingStart(Integer x, Integer y) {
        if (x == null || y == null) {
            if (myDirection.IsHorizontal) {
                x = mySpeed < 0 ? myWidth - 3 : 3;
                y = 1;
            } else {
                x = 1;
                y = mySpeed < 0 ? myHeight - 3 : 3;
            }
        } else {
            final int cornerX = x > myWidth / 2 ? myWidth : 0;
            final int cornerY = y > myHeight / 2 ? myHeight : 0;
            int deltaX = Math.min(Math.abs(x - cornerX), myWidth / 5);
            int deltaY = Math.min(Math.abs(y - cornerY), myHeight / 5);
            if (myDirection.IsHorizontal) {
                deltaY = Math.min(deltaY, deltaX / 3);
            } else {
                deltaX = Math.min(deltaX, deltaY / 3);
            }
            x = Math.abs(cornerX - deltaX);
            y = Math.abs(cornerY - deltaY);
        }
        myEndX = myStartX = x;
        myEndY = myStartY = y;
    }

    @Override
    public void doStep() {

    }


    @Override
    protected void setFilter() {
    }


    @Override
    public boolean hasNextPage() {
        if (ZLApplication.Instance() != null && ZLApplication.Instance().getCurrentView() != null)
            return ZLApplication.Instance().getCurrentView().canScroll(ZLView.PageIndex.next);
        return false;
    }

    @Override
    public boolean hasPreviousPage() {
        if (ZLApplication.Instance() != null && ZLApplication.Instance().getCurrentView() != null)
            return ZLApplication.Instance().getCurrentView().canScroll(ZLView.PageIndex.previous);
        return false;
    }

    @Override
    public void shift(boolean i) {
//        ZLView localZLView = ZLApplication.Instance().getCurrentView();
//        switch (CurlPageProviderImpl.1.$SwitchMap$com$unicom$zworeader$readercore$view$core$AnimationProvider$Mode[a().ordinal()])
//        {
//            default:
//            case leftToRight:
//            case rightToLeft:
//        }
//        while (true)
//        {
//            b();
//            return;
//            ZLView.PageIndex localPageIndex = g();
//            hs localhs = this.a;
//            if (localPageIndex == ZLView.PageIndex.next);
//            for (boolean bool = true; ; bool = false)
//            {
//                localhs.a(bool);
//                localZLView.a(localPageIndex);
//                ZLApplication.p().v();
//                com.unicom.zworeader.framework.util.BookUtil.d = true;
//                com.unicom.zworeader.framework.util.BookUtil.c = true;
//                com.unicom.zworeader.framework.util.BookUtil.e = true;
//                break;
//            }
//            localZLView.a(ZLView.PageIndex.current);
//        }
    }

    @Override
    public void updatePage(CurlPage page, int width, int height, ZLView.PageIndex index) {
//        if (width == 0 || height == 0) {
//            myBitmap
//
//        }
//        while (bitmap == null) {
//
//        }
//        page.setTexture(myBuffer, CurlPage.SIDE_BOTH);
//        page.setColor(Color.argb(127, 255, 255, 255), CurlPage.SIDE_BACK);
    }
}