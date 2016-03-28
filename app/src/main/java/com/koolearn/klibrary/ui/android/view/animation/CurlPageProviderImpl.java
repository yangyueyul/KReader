//package com.koolearn.klibrary.ui.android.view.animation;
//
//import android.graphics.Bitmap;
//import android.graphics.Canvas;
//
//import com.koolearn.klibrary.core.view.ZLViewEnums;
//import com.koolearn.klibrary.ui.android.curl.CurlPage;
//import com.koolearn.klibrary.ui.android.curl.CurlView;
//
//public class CurlPageProviderImpl extends AnimationProvider implements CurlView.PageProvider {
//
//    private CurlView curlView;
//    private Bitmap myBuffer;
//
//    public CurlPageProviderImpl(BitmapManager bitmapManager) {
//        super(bitmapManager);
//    }
//
//    private volatile boolean myUseCanvasHack = true;
//
//    @Override
//    protected void drawInternal(Canvas canvas) {
//
//    }
//
//    @Override
//    public ZLViewEnums.PageIndex getPageToScrollTo(int x, int y) {
//        if (myDirection == null) {
//            return ZLViewEnums.PageIndex.current;
//        }
//        switch (myDirection) {
//            case leftToRight:
//                return myStartX < myWidth / 2 ? ZLViewEnums.PageIndex.next : ZLViewEnums.PageIndex.previous;
//            case rightToLeft:
//                return myStartX < myWidth / 2 ? ZLViewEnums.PageIndex.previous : ZLViewEnums.PageIndex.next;
//            case up:
//                return myStartY < myHeight / 2 ? ZLViewEnums.PageIndex.previous : ZLViewEnums.PageIndex.next;
//            case down:
//                return myStartY < myHeight / 2 ? ZLViewEnums.PageIndex.next : ZLViewEnums.PageIndex.previous;
//        }
//        return ZLViewEnums.PageIndex.current;
//    }
//
//    @Override
//    protected void startAnimatedScrollingInternal(int speed) {
//
//    }
//
//    @Override
//    protected void setupAnimatedScrollingStart(Integer x, Integer y) {
//        if (x == null || y == null) {
//            if (myDirection.IsHorizontal) {
//                x = mySpeed < 0 ? myWidth - 3 : 3;
//                y = 1;
//            } else {
//                x = 1;
//                y = mySpeed < 0 ? myHeight - 3 : 3;
//            }
//        } else {
//            final int cornerX = x > myWidth / 2 ? myWidth : 0;
//            final int cornerY = y > myHeight / 2 ? myHeight : 0;
//            int deltaX = Math.min(Math.abs(x - cornerX), myWidth / 5);
//            int deltaY = Math.min(Math.abs(y - cornerY), myHeight / 5);
//            if (myDirection.IsHorizontal) {
//                deltaY = Math.min(deltaY, deltaX / 3);
//            } else {
//                deltaX = Math.min(deltaX, deltaY / 3);
//            }
//            x = Math.abs(cornerX - deltaX);
//            y = Math.abs(cornerY - deltaY);
//        }
//        myEndX = myStartX = x;
//        myEndY = myStartY = y;
//    }
//
//    @Override
//    public void doStep() {
//
//    }
//
//
//    @Override
//    protected void setFilter() {
//    }
//
//
////    @Override
////    public boolean hasNextPage() {
////        return false;
////    }
////
////    @Override
////    public boolean hasPreviousPage() {
////        return false;
////    }
////
////    @Override
////    public void shift(boolean i) {
////
////    }
////
////    @Override
////    public void updatePage(CurlPage page, int width, int height, ZLView.PageIndex index) {
////        page.setTexture(myBuffer, CurlPage.SIDE_BOTH);
////        page.setColor(Color.argb(127, 255, 255, 255), CurlPage.SIDE_BACK);
////    }
//
//    @Override
//    public int getPageCount() {
//        return 0;
//    }
//
//    @Override
//    public void updatePage(CurlPage page, int width, int height, int index) {
//
//    }
//}