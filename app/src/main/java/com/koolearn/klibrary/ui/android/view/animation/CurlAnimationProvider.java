package com.koolearn.klibrary.ui.android.view.animation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;

import com.koolearn.android.util.LogUtil;
import com.koolearn.klibrary.core.util.BitmapUtil;
import com.koolearn.klibrary.core.view.ZLViewEnums;
import com.koolearn.klibrary.ui.android.util.ZLAndroidColorUtil;
import com.koolearn.klibrary.ui.android.view.ViewUtil;

public final class CurlAnimationProvider extends AnimationProvider {
    private final Paint myPaint = new Paint();
    private final Paint myBackPaint = new Paint();
    private final Paint myEdgePaint = new Paint();

    final Path myFgPath = new Path();
    final Path myEdgePath = new Path();
    final Path myQuadPath = new Path();

    //    ColorMatrix cm = new ColorMatrix();
//    float array[] = { 0.65f, 0, 0, 0, 90.0f, R
//            0, 0.35f, 0, 0, 70.0f, G
//            0, 0, 0.65f, 0, 90.0f, B
//            0, 0, 0, 0.3f, 0 }; A
    ColorMatrix cm = new ColorMatrix();
    float array[] = {0.55f, 0, 0, 0, 80.0f, 0, 0.55f, 0, 0, 80.0f, 0, 0,
            0.55f, 0, 80.0f, 0, 0, 0, 0.2f, 0};

    private float mySpeedFactor = 1;

    public CurlAnimationProvider(BitmapManager bitmapManager) {
        super(bitmapManager);
        myBackPaint.setAntiAlias(true);
        myBackPaint.setAlpha(255);

        myEdgePaint.setAntiAlias(true); // 抗锯齿a
        myEdgePaint.setStyle(Paint.Style.FILL);
        myEdgePaint.setShadowLayer(15, 0, 0, 0xC0000000);
    }

    private Bitmap myBuffer;
    private volatile boolean myUseCanvasHack = true;

    @Override
    protected void drawInternal(Canvas canvas) {
        if (myUseCanvasHack) {
            // This is a hack that disables hardware acceleration
            //   1) for GLES20Canvas we got an UnsupportedOperationException in clipPath
            //   2) View.setLayerType(LAYER_TYPE_SOFTWARE) does not work properly in some cases
            if (myBuffer == null || myBuffer.getWidth() != myWidth || myBuffer.getHeight() != myHeight) {
                myBuffer = BitmapUtil.createBitmap(myWidth, myHeight, getBitmapTo().getConfig());
            }
            final Canvas softCanvas = new Canvas(myBuffer);
            drawInternalNoHack(softCanvas);
            // 在里面绘制好后,myBuffer中有当前动态页面的信息
            canvas.drawBitmap(myBuffer, 0, 0, myPaint);
        } else {
            try {
                drawInternalNoHack(canvas);
            } catch (UnsupportedOperationException e) {
                myUseCanvasHack = true;
                drawInternal(canvas);
            }
        }
    }

    private void drawInternalNoHack(Canvas canvas) {
        if (myDirection.IsHorizontal) {
            drawHorizontal(canvas);
        } else {
            drawVertical(canvas);
        }
    }

    private void drawHorizontal(Canvas canvas) {
        final int dx = myEndX - myStartX;
        if (dx < 0) {
            myStartX = myWidth;
            // 1.先在底部绘制出下一页
            drawBitmapTo(canvas, 0, 0, myPaint);
            /**
             * 左上 cornerX = 0         cornerY = 0
             * 左下 cornerX = 0         cornerY = myHeight
             * 右上 cornerX = myWidth   cornerY = 0
             * 右下 cornerX = myWidth   cornerY = myHeight
             */
            final int cornerX = myStartX > myWidth / 2 ? myWidth : 0; // 判断左边还是右边
            final int cornerY = myStartY > myHeight / 2 ? myHeight : 0; // 判断手在上部还是下部
            /**
             * 左上 oppositeX = myWidth  oppositeY = myHeight
             * 左下 oppositeX = myWidth  oppositeY = 0
             * 右上 oppositeX = 0        oppositeY = myHeight
             * 右下 oppositeX = 0        oppositeY = 0
             */
            final int oppositeX = Math.abs(myWidth - cornerX);
            final int oppositeY = Math.abs(myHeight - cornerY);
            int x;
            final int y;
//            if (cornerX == 0) {
//                x = Math.max(1, Math.min(myWidth / 2, myEndX));
//            } else {
//                x = Math.max(myWidth / 2, Math.min(myWidth - 1, myEndX));
//            }
            x = myEndX;
//            if (getMode().Auto) { // 松手后变为true
//                if (cornerY == 0) { // 左上 右上
//                    /**
//                     * 在最边时Y最小,最小设为为1,防止出现bug
//                     * Y最大值为 屏幕高度 / 2
//                     */
//                    y = Math.max(1, Math.min(myHeight / 4, myEndY));
//                } else { // 左下 右下
//                    /**
//                     * 在最下面时Y最大,最大设为屏幕高度-1,防止出现bug
//                     * Y最大值为 屏幕高度 / 2
//                     */
//                    y = (int) Math.max(0.75 * myHeight, Math.min(myHeight - 1, myEndY));
//                }
//            } else {
            if (cornerY == 0) { // 左上 右上
                /**
                 * 在最边时Y最小,最小设为为1,防止出现bug
                 * Y最大值为 屏幕高度 / 2
                 */
                y = Math.max(1, Math.min(myHeight / 4, myEndY));
            } else { // 左下 右下
                /**
                 * 在最下面时Y最大,最大设为屏幕高度-1,防止出现bug
                 * Y最大值为 屏幕高度 / 2
                 */
                y = (int) Math.max(0.75 * myHeight, Math.min(myHeight - 1, myEndY));
            }
//            }

            final int dX = Math.max(1, Math.abs(x - cornerX));
            final int dY = Math.max(1, Math.abs(y - cornerY));

            final int x1 = cornerX == 0
                    ? (dY * dY / dX + dX) / 2
                    : cornerX - (dY * dY / dX + dX) / 2;
            final int y1 = cornerY == 0
                    ? (dX * dX / dY + dY) / 2
                    : cornerY - (dX * dX / dY + dY) / 2;
            float sX, sY;
            {
                float d1 = x - x1;
                float d2 = y - cornerY;
                sX = (float) (Math.sqrt(d1 * d1 + d2 * d2) / 2);
                if (cornerX == 0) {
                    sX = -sX;
                }
            }
            {
                float d1 = x - cornerX;
                float d2 = y - y1;
                sY = (float) (Math.sqrt(d1 * d1 + d2 * d2) / 2);
                if (cornerY == 0) {
                    sY = -sY;
                }
            }
            /**
             * 裁出当前页剩余部分
             */
            myFgPath.rewind(); // 清除掉path里的线条和曲线,但是会保留内部的数据结构以便重用
            myFgPath.moveTo(x, y); // 不绘制,只用于移动画笔
            myFgPath.lineTo((x + cornerX) / 2, (y + y1) / 2); // 直线绘制 B4
            // mPath.quadTo(x1, y1, x2, y2) (x1,y1) 为控制点，(x2,y2)为结束点
            myFgPath.quadTo(cornerX, y1, cornerX, y1 - sY); // 绘制贝塞尔曲线 B5 B6

            if (Math.abs(y1 - sY - cornerY) < myHeight) {
                myFgPath.lineTo(cornerX, oppositeY); // 屏幕宽,0
            }
            myFgPath.lineTo(oppositeX, oppositeY); // 0,0
            if (Math.abs(x1 - sX - cornerX) < myWidth) {
                myFgPath.lineTo(oppositeX, cornerY); // 0,屏幕高
            }
            myFgPath.lineTo(x1 - sX, cornerY); // B1
            myFgPath.quadTo(x1, cornerY, (x + x1) / 2, (y + cornerY) / 2); // B2 B3

            // 2.两边阴影的绘制 myQuadPath
            /**
             * 贝赛尔曲线的起始点
             * path.moveTo(startX, startY);
             * 设置贝赛尔曲线的操作点以及终止点
             * path.quadTo(controlX, controlY, endX, endY);
             */
            LogUtil.i8("cornerY:" + cornerY);
            myQuadPath.moveTo(x1 - sX, cornerY); // 起点B1
            myQuadPath.quadTo(x1, cornerY, (x + x1) / 2, (y + cornerY) / 2); // 控制点B2 终点B3
            canvas.drawPath(myQuadPath, myEdgePaint);
            myQuadPath.rewind();

            myQuadPath.moveTo((x + cornerX) / 2, (y + y1) / 2); // B4
            myQuadPath.quadTo(cornerX, y1, cornerX, y1 - sY); // B5 B6
            canvas.drawPath(myQuadPath, myEdgePaint);
            myQuadPath.rewind();

            // 3.绘制当前页剩余的部分
            canvas.save();
            canvas.clipPath(myFgPath);
            drawBitmapFrom(canvas, 0, 0, myPaint);
            canvas.restore();

            // 4.最后绘制翻起页背面+周围的三角形阴影
            myEdgePaint.setColor(ZLAndroidColorUtil.rgb(ZLAndroidColorUtil.getAverageColor(getBitmapFrom())));

            myEdgePath.rewind();
            myEdgePath.moveTo(x, y); // A1
            myEdgePath.lineTo((x + cornerX) / 2, (y + y1) / 2); // B4
            myEdgePath.quadTo(
                    (x + 3 * cornerX) / 4, // c1
                    (y + 3 * y1) / 4,
                    (x + 7 * cornerX) / 8, // c2
                    (y + 7 * y1 - 2 * sY) / 8
            );

            myEdgePath.lineTo(
                    (x + 7 * x1 - 2 * sX) / 8, // c3
                    (y + 7 * cornerY) / 8
            );
            myEdgePath.quadTo(
                    (x + 3 * x1) / 4, // c4
                    (y + 3 * cornerY) / 4,
                    (x + x1) / 2, // B3
                    (y + cornerY) / 2
            );
            canvas.drawPath(myEdgePath, myEdgePaint);

            // 在背面上覆盖文字
            canvas.save();
            canvas.clipPath(myEdgePath);
            final Matrix matrix = new Matrix();

            cm.set(array);
            ColorMatrixColorFilter mColorMatrixFilter = new ColorMatrixColorFilter(cm);
            myBackPaint.setColorFilter(mColorMatrixFilter);
            myBackPaint.setAlpha(255);

            matrix.postScale(1, -1); // 缩放比例,x不变,y变负,界面变镜像
            matrix.postTranslate(x - cornerX, y + cornerY); // 平移

            final float angle;
            if (cornerY == 0) { // 计算旋转角度
                angle = -180 / 3.1416f * (float) Math.atan2(x - cornerX, y - y1);
            } else {
                angle = 180 - 180 / 3.1416f * (float) Math.atan2(x - cornerX, y - y1);
            }
            matrix.postRotate(angle, x, y); // 按角度进行进行旋转
            canvas.drawBitmap(getBitmapFrom(), matrix, myBackPaint);
            canvas.restore();
        } else if (dx > 0) { // 向右滑
            myStartX = 0;
            drawBitmapFrom(canvas, 0, 0, myPaint);
            /**
             * 裁出下一页出现部分
             */
            myFgPath.rewind(); // 清除掉path里的线条和曲线,但是会保留内部的数据结构以便重用
            myFgPath.moveTo(myEndX, myEndY); // 不绘制,只用于移动画笔
            myFgPath.lineTo(myEndX, 0); // 不绘制,只用于移动画笔
            myFgPath.lineTo(0, 0); // 直线绘制 B4
            myFgPath.lineTo(0, myHeight); // 直线绘制 B4
            myFgPath.lineTo(myEndX, myHeight); // 直线绘制 B4
            // 1.绘制当前页剩余的部分
            canvas.save();
            canvas.clipPath(myFgPath);
            drawBitmapTo(canvas, 0, 0, myPaint);
            canvas.restore();

            // 2.绘制翻起页背面
            myEdgePaint.setColor(ZLAndroidColorUtil.rgb(ZLAndroidColorUtil.getAverageColor(getBitmapFrom())));

            myEdgePath.rewind();
            myFgPath.moveTo(myEndX, 0); // 不绘制,只用于移动画笔
            float v = myEndX + (float) (myWidth - myEndX) / 2;
            myEdgePath.lineTo(v, 0);
            myEdgePath.lineTo(v, myHeight);
            myEdgePath.lineTo(myEndX, myHeight);
            myEdgePath.lineTo(myEndX, 0);
            canvas.drawPath(myEdgePath, myEdgePaint);

            // 3.在背面上覆盖文字
            canvas.save();
            canvas.clipPath(myEdgePath);
            final Matrix matrix = new Matrix();
            cm.set(array);
            ColorMatrixColorFilter mColorMatrixFilter = new ColorMatrixColorFilter(cm);
            myBackPaint.setColorFilter(mColorMatrixFilter);
            myBackPaint.setAlpha(255);
            matrix.postScale(1, -1); // 缩放比例,x不变,y变负,界面变镜像
            matrix.postTranslate(myEndX - myWidth, myHeight * 2); // 平移
            matrix.postRotate(180, myEndX, myHeight); // 按角度进行进行旋转
            canvas.drawBitmap(getBitmapTo(), matrix, myBackPaint);
            canvas.restore();
        }
    }

    private void drawVertical(Canvas canvas) {
        // 1.先在底部绘制出下一页
        drawBitmapTo(canvas, 0, 0, myPaint);
        final int cornerX = myStartX > myWidth / 2 ? myWidth : 0; // 判断左边还是右边
        final int cornerY = myStartY > myHeight / 2 ? myHeight : 0; // 判断手在上部还是下部
        final int oppositeX = Math.abs(myWidth - cornerX);
        final int oppositeY = Math.abs(myHeight - cornerY);
        final int x, y;
        y = myEndY;
        if (getMode().Auto) {
            x = myEndX;
        } else {
            if (cornerX == 0) {
                x = Math.max(1, Math.min(myWidth / 2, myEndX));
            } else {
                x = Math.max(myWidth / 2, Math.min(myWidth - 1, myEndX));
            }
        }


        final int dX = Math.max(1, Math.abs(x - cornerX));
        final int dY = Math.max(1, Math.abs(y - cornerY));

        final int x1 = cornerX == 0
                ? (dY * dY / dX + dX) / 2
                : cornerX - (dY * dY / dX + dX) / 2;
        final int y1 = cornerY == 0
                ? (dX * dX / dY + dY) / 2
                : cornerY - (dX * dX / dY + dY) / 2;
        float sX, sY;
        {
            float d1 = x - x1;
            float d2 = y - cornerY;
            sX = (float) (Math.sqrt(d1 * d1 + d2 * d2) / 2);
            if (cornerX == 0) {
                sX = -sX;
            }
        }
        {
            float d1 = x - cornerX;
            float d2 = y - y1;
            sY = (float) (Math.sqrt(d1 * d1 + d2 * d2) / 2);
            if (cornerY == 0) {
                sY = -sY;
            }
        }

        /**
         * 裁出当前页剩余部分
         */
        myFgPath.rewind(); // 清除掉path里的线条和曲线,但是会保留内部的数据结构以便重用
        myFgPath.moveTo(x, y); // 不绘制,只用于移动画笔
        myFgPath.lineTo((x + cornerX) / 2, (y + y1) / 2); // 直线绘制 B4
        // mPath.quadTo(x1, y1, x2, y2) (x1,y1) 为控制点，(x2,y2)为结束点
        myFgPath.quadTo(cornerX, y1, cornerX, y1 - sY); // 绘制贝塞尔曲线 B5 B6

        if (Math.abs(y1 - sY - cornerY) < myHeight) {
            myFgPath.lineTo(cornerX, oppositeY); // 屏幕宽,0
        }
        myFgPath.lineTo(oppositeX, oppositeY); // 0,0
        if (Math.abs(x1 - sX - cornerX) < myWidth) {
            myFgPath.lineTo(oppositeX, cornerY); // 0,屏幕高
        }
        myFgPath.lineTo(x1 - sX, cornerY); // B1
        myFgPath.quadTo(x1, cornerY, (x + x1) / 2, (y + cornerY) / 2); // B2 B3

        // 2.两边阴影的绘制 myQuadPath
        /**
         * 贝赛尔曲线的起始点
         * path.moveTo(startX, startY);
         * 设置贝赛尔曲线的操作点以及终止点
         * path.quadTo(controlX, controlY, endX, endY);
         */
        myQuadPath.moveTo(x1 - sX, cornerY); // 起点B1
        myQuadPath.quadTo(x1, cornerY, (x + x1) / 2, (y + cornerY) / 2); // 控制点B2 终点B3
        canvas.drawPath(myQuadPath, myEdgePaint);
        myQuadPath.rewind();

        myQuadPath.moveTo((x + cornerX) / 2, (y + y1) / 2); // B4
        myQuadPath.quadTo(cornerX, y1, cornerX, y1 - sY); // B5 B6
        canvas.drawPath(myQuadPath, myEdgePaint);
        myQuadPath.rewind();

        // 3.绘制当前页剩余的部分
        canvas.save();
        canvas.clipPath(myFgPath);
        drawBitmapFrom(canvas, 0, 0, myPaint);
        canvas.restore();

        // 4.最后绘制翻起页背面+周围的三角形阴影
        myEdgePaint.setColor(ZLAndroidColorUtil.rgb(ZLAndroidColorUtil.getAverageColor(getBitmapFrom())));

        myEdgePath.rewind();
        myEdgePath.moveTo(x, y); // A1
        myEdgePath.lineTo((x + cornerX) / 2, (y + y1) / 2); // B4
        myEdgePath.quadTo(
                (x + 3 * cornerX) / 4, // c1
                (y + 3 * y1) / 4,
                (x + 7 * cornerX) / 8, // c2
                (y + 7 * y1 - 2 * sY) / 8
        );

        myEdgePath.lineTo(
                (x + 7 * x1 - 2 * sX) / 8, // c3
                (y + 7 * cornerY) / 8
        );
        myEdgePath.quadTo(
                (x + 3 * x1) / 4, // c4
                (y + 3 * cornerY) / 4,
                (x + x1) / 2, // B3
                (y + cornerY) / 2
        );
        canvas.drawPath(myEdgePath, myEdgePaint);

        // 在背面上覆盖文字
        canvas.save();
        canvas.clipPath(myEdgePath);
        final Matrix matrix = new Matrix();

        cm.set(array);
        ColorMatrixColorFilter mColorMatrixFilter = new ColorMatrixColorFilter(cm);
        myBackPaint.setColorFilter(mColorMatrixFilter);
        myBackPaint.setAlpha(255);

        matrix.postScale(1, -1); // 缩放比例,x不变,y变负,界面变镜像
        matrix.postTranslate(x - cornerX, y + cornerY); // 平移

        final float angle;
        if (cornerY == 0) { // 计算旋转角度
            angle = -180 / 3.1416f * (float) Math.atan2(x - cornerX, y - y1);
        } else {
            angle = 180 - 180 / 3.1416f * (float) Math.atan2(x - cornerX, y - y1);
        }
        matrix.postRotate(angle, x, y); // 按角度进行进行旋转
        canvas.drawBitmap(getBitmapFrom(), matrix, myBackPaint);
        canvas.restore();
    }

    @Override
    public ZLViewEnums.PageIndex getPageToScrollTo(int x, int y) {
        if (myDirection == null) {
            return ZLViewEnums.PageIndex.current;
        }
        switch (myDirection) {
            case leftToRight:
                return myStartX < myWidth / 2 ? ZLViewEnums.PageIndex.next : ZLViewEnums.PageIndex.previous;
            case rightToLeft: // 左右走这
                return myStartX < myWidth / 2 ? ZLViewEnums.PageIndex.previous : ZLViewEnums.PageIndex.next;
            case up: // 上下走这
                return myStartY < myHeight / 2 ? ZLViewEnums.PageIndex.previous : ZLViewEnums.PageIndex.next;
            case down:
                return myStartY < myHeight / 2 ? ZLViewEnums.PageIndex.next : ZLViewEnums.PageIndex.previous;
        }
        return ZLViewEnums.PageIndex.current;
    }

    // 拖动后翻页
    @Override
    protected void startAnimatedScrollingInternal(int speed) {
        LogUtil.i16("speed:" + speed);
        mySpeedFactor = (float) Math.pow(2.0, 0.25 * 3);
//        mySpeedFactor = (float) Math.pow(2.0, 0.25 * speed);
        mySpeed *= 1.5;
        doStep();
    }

    // 点按翻阅
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
        if (!getMode().Auto) {
            return;
        }

        final int speed = (int) Math.abs(mySpeed);
        mySpeed *= mySpeedFactor;

        final int cornerX = myStartX > myWidth / 2 ? myWidth : 0;
        final int cornerY = myStartY > myHeight / 2 ? myHeight : 0;

        final int boundX, boundY;
        if (getMode() == Mode.AnimatedScrollingForward) {
            boundX = cornerX == 0 ? 2 * myWidth : -myWidth;
            boundY = cornerY == 0 ? 2 * myHeight : -myHeight;
        } else {
            boundX = cornerX;
            boundY = cornerY;
        }

        final int deltaX = Math.abs(myEndX - cornerX);
        final int deltaY = Math.abs(myEndY - cornerY);
        final int speedX, speedY;
        if (deltaX == 0 || deltaY == 0) {
            speedX = speed;
            speedY = speed;
        } else if (deltaX < deltaY) {
            speedX = speed;
            speedY = speed * deltaY / deltaX;
        } else {
            speedX = speed * deltaX / deltaY;
            speedY = speed;
        }

        final boolean xSpeedIsPositive, ySpeedIsPositive;
        if (getMode() == Mode.AnimatedScrollingForward) {
            xSpeedIsPositive = cornerX == 0;
            ySpeedIsPositive = cornerY == 0;
        } else {
            xSpeedIsPositive = cornerX != 0;
            ySpeedIsPositive = cornerY != 0;
        }

        if (xSpeedIsPositive) {
            myEndX += speedX;
            if (myEndX >= boundX) {
                terminate();
            }
        } else {
            myEndX -= speedX;
            if (myEndX <= boundX) {
                terminate();
            }
        }

        if (ySpeedIsPositive) {
            myEndY += speedY;
            if (myEndY >= boundY) {
                terminate();
            }
        } else {
            myEndY -= speedY;
            if (myEndY <= boundY) {
                terminate();
            }
        }
    }

//	@Override
//	public void drawFooterBitmapInternal(Canvas canvas, Bitmap footerBitmap, int voffset) {
//		canvas.drawBitmap(footerBitmap, 0, voffset, myPaint);
//	}

    @Override
    protected void setFilter() {
        ViewUtil.setColorLevel(myPaint, myColorLevel);
        ViewUtil.setColorLevel(myBackPaint, myColorLevel);
        ViewUtil.setColorLevel(myEdgePaint, myColorLevel);
    }
}