package com.koolearn.klibrary.ui.android.view.animation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.widget.RelativeLayout;

import com.koolearn.klibrary.core.application.ZLApplication;
import com.koolearn.klibrary.core.view.ZLView;
import com.koolearn.klibrary.core.view.ZLViewEnums;
import com.koolearn.klibrary.ui.android.curl.CurlPage;
import com.koolearn.klibrary.ui.android.curl.CurlView;
import com.koolearn.klibrary.ui.android.view.BitmapManagerCurlImpl;
import com.koolearn.klibrary.ui.android.view.ZLAndroidCurlWidget;

public class CurlPageProviderImpl extends AnimationProvider implements PageProvider{

    private CurlView curlView;

    public CurlPageProviderImpl(BitmapManager bitmapManager) {
        super(bitmapManager);
        curlView = ZLAndroidCurlWidget.Instance();
        curlView.setPageProvider(this);
        curlView.setViewMode(CurlView.SHOW_ONE_PAGE);
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
        if(ZLApplication.Instance()!=null && ZLApplication.Instance().getCurrentView()!=null){
            return ZLApplication.Instance().getCurrentView().canScroll(ZLView.PageIndex.next);
        }else{
            return false;
        }
    }

    @Override
    public boolean hasPreviousPage() {
        if(ZLApplication.Instance()!=null && ZLApplication.Instance().getCurrentView()!=null){
            return ZLApplication.Instance().getCurrentView().canScroll(ZLView.PageIndex.previous);
        }else{
            return false;
        }
    }

    @Override
    public void shift(int paramBoolean) {
        switch (paramBoolean){
            case CurlView.CURL_RIGHT:
                forwardShift(true);
                ZLApplication.Instance().getCurrentView().onScrollingFinished(ZLView.PageIndex.next);
                break;
            case CurlView.CURL_LEFT:
                forwardShift(false);
                ZLApplication.Instance().getCurrentView().onScrollingFinished(ZLView.PageIndex.previous);
                break;
            case CurlView.CURL_NONE:
                ZLApplication.Instance().getCurrentView().onScrollingFinished(ZLView.PageIndex.current);
                break;
        }

    }

    @Override
    public void updatePage(CurlPage page, int paramInt1, int paramInt2, ZLView.PageIndex paramPageIndex) {
        page.setTexture(getBitmapByPageIndex(paramPageIndex), CurlPage.SIDE_BOTH);
//        myColorLevel
        page.setColor(Color.argb(127, 255, 255, 255), CurlPage.SIDE_BACK);
    }
}