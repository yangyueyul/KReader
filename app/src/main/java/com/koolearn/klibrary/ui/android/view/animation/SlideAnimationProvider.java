package com.koolearn.klibrary.ui.android.view.animation;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;

import com.koolearn.klibrary.ui.android.view.ViewUtil;

public final class SlideAnimationProvider extends SimpleAnimationProvider {
    private final Paint myDarkPaint = new Paint();
    private final Paint myPaint = new Paint();

    public SlideAnimationProvider(BitmapManager bitmapManager) {
        super(bitmapManager);
    }

    private void setDarkFilter(int visible, int full) { // 滤镜
        int darkColorLevel = 145 + 100 * Math.abs(visible) / full;
        if (myColorLevel != null) { // 当电量<25%时, myColorLevel != null myColorLevel = 0x60 + (0xFF - 0x60) * Math.max(percent, 0) / 25;
            darkColorLevel = darkColorLevel * myColorLevel / 0xFF; // 屏幕过暗,需要设置不同的暗度
        }
        ViewUtil.setColorLevel(myDarkPaint, darkColorLevel);
    }

    @Override
    protected void setFilter() {
        ViewUtil.setColorLevel(myPaint, myColorLevel);
    }

    private void drawShadowHorizontal(Canvas canvas, int left, int right, int dY) {
        final GradientDrawable.Orientation orientation = GradientDrawable.Orientation.TOP_BOTTOM;
//                dY > 0 ? GradientDrawable.Orientation.BOTTOM_TOP : GradientDrawable.Orientation.TOP_BOTTOM;
        final int[] colors = new int[]{0x46000000, 0x00000000};
        final GradientDrawable gradient = new GradientDrawable(orientation, colors);
        gradient.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        gradient.setDither(true);
        if (dY > 0) {
            gradient.setBounds(left, dY, right, dY + 16);
        } else {
            gradient.setBounds(left, myHeight + dY, right, myHeight + dY + 16);
        }
        gradient.draw(canvas);
    }

    private void drawShadowVertical(Canvas canvas, int top, int bottom, int dX) {
        final GradientDrawable.Orientation orientation = GradientDrawable.Orientation.LEFT_RIGHT;
//                dX > 0 ? GradientDrawable.Orientation.RIGHT_LEFT : GradientDrawable.Orientation.LEFT_RIGHT;
        final int[] colors = new int[]{0x46000000, 0x00000000};
        final GradientDrawable gradient = new GradientDrawable(orientation, colors); // 渐变方向,颜色值区间 从左到右逐渐变透明
        gradient.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        gradient.setDither(true);
        if (dX > 0) {
            gradient.setBounds(dX, top, dX + 16, bottom);
        } else {
            gradient.setBounds(myWidth + dX, top, myWidth + dX + 16, bottom); // 设置阴影宽度和高度
        }
        gradient.draw(canvas);
    }

    @Override
    protected void drawInternal(Canvas canvas) {
        if (myDirection.IsHorizontal) {
            final int dX = myEndX - myStartX;
            if (dX < 0) {
                setDarkFilter(dX, myWidth); // 根据dx来设置 myDarkPaint的 滤镜效果
                drawBitmapTo(canvas, 0, 0, myDarkPaint);
                drawBitmapFrom(canvas, dX, 0, myPaint);
                drawShadowVertical(canvas, 0, myHeight, dX); // 绘制阴影
            } else {
                setDarkFilter(dX - myWidth, myWidth);
                drawBitmapFrom(canvas, 0, 0, myDarkPaint);
                drawBitmapTo(canvas, dX - myWidth, 0, myPaint);
                drawShadowVertical(canvas, 0, myHeight, dX);
            }
        } else {
            final int dY = myEndY - myStartY;
            if (dY < 0) {
                setDarkFilter(dY, myHeight);
                drawBitmapTo(canvas, 0, 0, myDarkPaint);
                drawBitmapFrom(canvas, 0, dY, myPaint);
                drawShadowHorizontal(canvas, 0, myWidth, dY);
            } else {
                setDarkFilter(dY - myHeight, myHeight);
                drawBitmapFrom(canvas, 0, 0, myDarkPaint);
                drawBitmapTo(canvas, 0, dY - myHeight, myPaint);
                drawShadowHorizontal(canvas, 0, myWidth, dY);
            }
        }
    }

//    private void drawBitmapInternal(Canvas canvas, Bitmap bm, int left, int right, int height, int voffset, Paint paint) {
//        canvas.drawBitmap(
//                bm,
//                new Rect(left, 0, right, height),
//                new Rect(left, voffset, right, voffset + height),
//                paint
//        );
//    }

//    @Override
//    protected void drawFooterBitmapInternal(Canvas canvas, Bitmap footerBitmap, int voffset) {
//        if (myDirection.IsHorizontal) { // 横屏翻阅
//            final int dX = myEndX - myStartX;
//            setDarkFilter(dX, myWidth);
//            final int h = footerBitmap.getHeight();
//            if (dX > 0) {
//                drawBitmapInternal(canvas, footerBitmap, 0, dX, h, voffset, myDarkPaint);
//                drawBitmapInternal(canvas, footerBitmap, dX, myWidth, h, voffset, myPaint);
//            } else {
//                drawBitmapInternal(canvas, footerBitmap, myWidth + dX, myWidth, h, voffset, myDarkPaint);
//                drawBitmapInternal(canvas, footerBitmap, 0, myWidth + dX, h, voffset, myPaint);
//            }
//            drawShadowVertical(canvas, voffset, voffset + h, dX);
//        } else {
//            final int dY = myEndY - myStartY;
//            setDarkFilter(dY, myHeight);
//            if (dY > 0) {
//                canvas.drawBitmap(footerBitmap, 0, voffset, myPaint); // 竖屏翻页
//            } else {
//                canvas.drawBitmap(footerBitmap, 0, voffset, myDarkPaint); // 竖屏翻页
//            }
//        }
//    }
}
