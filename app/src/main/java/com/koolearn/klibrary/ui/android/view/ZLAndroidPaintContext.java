package com.koolearn.klibrary.ui.android.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.EmbossMaskFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;

import com.koolearn.klibrary.core.filesystem.ZLFile;
import com.koolearn.klibrary.core.fonts.FontEntry;
import com.koolearn.klibrary.core.image.ZLImageData;
import com.koolearn.klibrary.core.util.SystemInfo;
import com.koolearn.klibrary.core.util.ZLColor;
import com.koolearn.klibrary.core.view.ZLPaintContext;
import com.koolearn.klibrary.ui.android.image.ZLAndroidImageData;
import com.koolearn.klibrary.ui.android.library.ZLAndroidLibrary;
import com.koolearn.klibrary.ui.android.util.ZLAndroidColorUtil;
import com.koolearn.kooreader.bookmodel.TOCTree;
import com.koolearn.kooreader.kooreader.KooReaderApp;

import java.util.List;

public final class ZLAndroidPaintContext extends ZLPaintContext {
    //    public static ZLBooleanOption AntiAliasOption = new ZLBooleanOption("Fonts", "AntiAlias", true);
//    public static ZLBooleanOption DeviceKerningOption = new ZLBooleanOption("Fonts", "DeviceKerning", false);
//    public static ZLBooleanOption DitheringOption = new ZLBooleanOption("Fonts", "Dithering", false);
//    public static ZLBooleanOption SubpixelOption = new ZLBooleanOption("Fonts", "Subpixel", false);
    public static KooReaderApp myReader;
    public float dp_1 = ZLAndroidLibrary.Instance().getDPI();
    public float sp_1 = ZLAndroidLibrary.Instance().getSP();

    private final Canvas myCanvas;
    private final Paint myTextPaint = new Paint(); // 文字画笔
    private final Paint myLinePaint = new Paint();
    private final Paint myFillPaint = new Paint();
    private final Paint myFooterPaint = new Paint();
    private final Paint myTopPaint = new Paint();
    private final Paint myOutlinePaint = new Paint();

    /**
     * 画笔信息
     */
    private Paint mBatteryPaint;
    private Paint mPowerPaint;
    private float mBatteryStroke = 2f;
    private int loadComplete = 0;

    /**
     * 电池参数
     */
    private float mBatteryHeight = 10 * dp_1; // 电池的高度
    private float mBatteryWidth = 21 * dp_1; // 电池的宽度
    private float mCapHeight = 6 * dp_1; // 电池头高度
    private float mCapWidth = 2 * dp_1; // 电池头宽度
    /**
     * 电池电量
     */
    private float mPowerPadding = (float) (0.6 * dp_1);
    private float mPowerHeight = mBatteryHeight - mBatteryStroke
            - mPowerPadding * 2; // 电池身体的高度
    private float mPowerWidth = mBatteryWidth - mBatteryStroke - mPowerPadding
            * 2;// 电池身体的总宽度
    private float mPower = 0f;
    private String mTime;
    private String mPosition;
    /**
     * 矩形
     */
    private RectF mBatteryRect;
    private RectF mCapRect;
    private RectF mPowerRect;
    private final int leftMargin;
    private final int rightMargin;
    private String mBookTitle;
    private String mBookToc;

    public static final class Geometry {
        final Size ScreenSize;
        final Size AreaSize;
        final int LeftMargin;
        final int TopMargin;

        public Geometry(int screenWidth, int screenHeight, int width, int height, int leftMargin, int topMargin) {
            ScreenSize = new Size(screenWidth, screenHeight);
            AreaSize = new Size(width, height);
            LeftMargin = leftMargin;
            TopMargin = topMargin;
        }
    }

    private final Geometry myGeometry;
    private final int myScrollbarWidth;

    private ZLColor myBackgroundColor = new ZLColor(0, 0, 0);

    public ZLAndroidPaintContext(SystemInfo systemInfo, Canvas canvas, Geometry geometry, int scrollbarWidth) {
        super(systemInfo);

        myCanvas = canvas;
        myGeometry = geometry;
        myScrollbarWidth = scrollbarWidth;

        myTextPaint.setLinearText(false); // 线性文本
        myTextPaint.setAntiAlias(true); // 防锯齿
//        myTextPaint.setAntiAlias(AntiAliasOption.getValue());
        if (true) {
//        if (DeviceKerningOption.getValue()) {
            myTextPaint.setFlags(myTextPaint.getFlags() | Paint.DEV_KERN_TEXT_FLAG);
        } else {
            myTextPaint.setFlags(myTextPaint.getFlags() & ~Paint.DEV_KERN_TEXT_FLAG);
        }
        // 图像的抖动处理，当每个颜色值以低于8位表示时，对应图像做抖动处理可以实现在可显示颜色总数比较低（比如256色）时还保持较好的显示效果
        myTextPaint.setDither(true);
//        myTextPaint.setDither(DitheringOption.getValue());
        // 将有助于文本在LCD屏幕上的显示效果
        myTextPaint.setSubpixelText(true);
//        myTextPaint.setSubpixelText(SubpixelOption.getValue());


        // Paint.Style.FILL    :填充内部
        // Paint.Style.FILL_AND_STROKE  ：填充内部和描边
        // Paint.Style.STROKE  ：仅描边
        myLinePaint.setStyle(Paint.Style.STROKE);

        myFillPaint.setAntiAlias(true);
//        myFillPaint.setAntiAlias(AntiAliasOption.getValue());

        myOutlinePaint.setAntiAlias(true);
        myOutlinePaint.setDither(true);
        myOutlinePaint.setStrokeWidth(4); // 画笔宽度
        myOutlinePaint.setStyle(Paint.Style.STROKE);
        // 这个方法一看就和path有关，顾名思义，它就是给path设置样式（效果）的。PathEffect这个路径效果类没有具体的实现，效果是由它的六个子类实现的
        myOutlinePaint.setPathEffect(new CornerPathEffect(5));
        // http://www.myext.cn/other/a_32202.html 阴影设置
        myOutlinePaint.setMaskFilter(new EmbossMaskFilter(new float[]{1, 1, 1}, .4f, 6f, 3.5f));


        /**
         * 设置电池画笔
         */
        mBatteryPaint = new Paint();
        mBatteryPaint.setColor(Color.GRAY);
        mBatteryPaint.setAntiAlias(true);
        mBatteryPaint.setStyle(Paint.Style.STROKE);
        mBatteryPaint.setStrokeWidth(mBatteryStroke);

        /**
         * 设置电量画笔
         */
        mPowerPaint = new Paint();
        mPowerPaint.setColor(Color.GRAY);
        mPowerPaint.setAntiAlias(true);
        mPowerPaint.setStyle(Paint.Style.FILL);
        mPowerPaint.setStrokeWidth(mBatteryStroke);

//        ColorProfile colorProfile = myReader.ViewOptions.getColorProfile();
        myFooterPaint.setTextSize(12 * sp_1);
        myFooterPaint.setLinearText(false);
        myFooterPaint.setAntiAlias(true);
        myFooterPaint.setDither(true);
        myFooterPaint.setSubpixelText(true);
        myFooterPaint.setAlpha(199);

        myTopPaint.setTextSize(11 * sp_1);
        myTopPaint.setLinearText(false);
        myTopPaint.setAntiAlias(true);
        myTopPaint.setDither(true);
        myTopPaint.setSubpixelText(true);
        myTopPaint.setAlpha(199);

        leftMargin = myReader.ViewOptions.LeftMargin.getValue(); // 24
        rightMargin = myReader.ViewOptions.RightMargin.getValue();
    }

    private static ZLFile ourWallpaperFile;
    private static Bitmap ourWallpaper;
    private static FillMode ourFillMode;

    @Override
    public void clear(ZLFile wallpaperFile, FillMode mode) {
        if (!wallpaperFile.equals(ourWallpaperFile) || mode != ourFillMode) {
            ourWallpaperFile = wallpaperFile;
            ourFillMode = mode;
            ourWallpaper = null;
            try {
                final Bitmap fileBitmap = BitmapFactory.decodeStream(wallpaperFile.getInputStream());
                switch (mode) {
                    default:
                        ourWallpaper = fileBitmap;
                        break;
                    case tileMirror: {
                        final int w = fileBitmap.getWidth();
                        final int h = fileBitmap.getHeight();
                        final Bitmap wallpaper = Bitmap.createBitmap(2 * w, 2 * h, fileBitmap.getConfig());
                        final Canvas wallpaperCanvas = new Canvas(wallpaper);
                        final Paint wallpaperPaint = new Paint();

                        Matrix m = new Matrix();
                        wallpaperCanvas.drawBitmap(fileBitmap, m, wallpaperPaint);
                        m.preScale(-1, 1);
                        m.postTranslate(2 * w, 0);
                        wallpaperCanvas.drawBitmap(fileBitmap, m, wallpaperPaint);
                        m.preScale(1, -1);
                        m.postTranslate(0, 2 * h);
                        wallpaperCanvas.drawBitmap(fileBitmap, m, wallpaperPaint);
                        m.preScale(-1, 1);
                        m.postTranslate(-2 * w, 0);
                        wallpaperCanvas.drawBitmap(fileBitmap, m, wallpaperPaint);
                        ourWallpaper = wallpaper;

                        ourWallpaper = fileBitmap;

                        break;
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        if (ourWallpaper != null) {
            myBackgroundColor = ZLAndroidColorUtil.getAverageColor(ourWallpaper);
            final int w = ourWallpaper.getWidth();
            final int h = ourWallpaper.getHeight();
            final Geometry g = myGeometry;
            switch (mode) {
                case fullscreen: {
                    final Matrix m = new Matrix();
                    m.preScale(1f * g.ScreenSize.Width / w, 1f * g.ScreenSize.Height / h);
                    m.postTranslate(-g.LeftMargin, -g.TopMargin);
                    myCanvas.drawBitmap(ourWallpaper, m, myFillPaint);
                    break;
                }
                case stretch: {
                    final Matrix m = new Matrix();
                    final float sw = 1f * g.ScreenSize.Width / w;
                    final float sh = 1f * g.ScreenSize.Height / h;
                    final float scale;
                    float dx = g.LeftMargin;
                    float dy = g.TopMargin;
                    if (sw < sh) {
                        scale = sh;
                        dx += (scale * w - g.ScreenSize.Width) / 2;
                    } else {
                        scale = sw;
                        dy += (scale * h - g.ScreenSize.Height) / 2;
                    }
                    m.preScale(scale, scale);
                    m.postTranslate(-dx, -dy);
                    myCanvas.drawBitmap(ourWallpaper, m, myFillPaint);
                    break;
                }
                case tileVertically: {
                    final Matrix m = new Matrix();
                    final int dx = g.LeftMargin;
                    final int dy = g.TopMargin % h;
                    m.preScale(1f * g.ScreenSize.Width / w, 1);
                    m.postTranslate(-dx, -dy);
                    for (int ch = g.AreaSize.Height + dy; ch > 0; ch -= h) {
                        myCanvas.drawBitmap(ourWallpaper, m, myFillPaint);
                        m.postTranslate(0, h);
                    }
                    break;
                }
                case tileHorizontally: {
                    final Matrix m = new Matrix();
                    final int dx = g.LeftMargin % w;
                    final int dy = g.TopMargin;
                    m.preScale(1, 1f * g.ScreenSize.Height / h);
                    m.postTranslate(-dx, -dy);
                    for (int cw = g.AreaSize.Width + dx; cw > 0; cw -= w) {
                        myCanvas.drawBitmap(ourWallpaper, m, myFillPaint);
                        m.postTranslate(w, 0);
                    }
                    break;
                }
                case tile:
                case tileMirror: {
                    final int dx = g.LeftMargin % w;
                    final int dy = g.TopMargin % h;
                    final int fullw = g.AreaSize.Width + dx;
                    final int fullh = g.AreaSize.Height + dy;
                    for (int cw = 0; cw < fullw; cw += w) {
                        for (int ch = 0; ch < fullh; ch += h) {
                            myCanvas.drawBitmap(ourWallpaper, cw - dx, ch - dy, myFillPaint);
                        }
                    }
                    break;
                }
            }
        } else {
            clear(new ZLColor(128, 128, 128));
        }
    }

    @Override
    public void clear(ZLColor color) {
        myBackgroundColor = color;
        myFillPaint.setColor(ZLAndroidColorUtil.rgb(color));
        myCanvas.drawRect(0, 0, myGeometry.AreaSize.Width, myGeometry.AreaSize.Height, myFillPaint);
    }

    @Override
    public ZLColor getBackgroundColor() {
        return myBackgroundColor;
    }

    public void fillPolygon(int[] xs, int[] ys) {
        final Path path = new Path();
        final int last = xs.length - 1;
        path.moveTo(xs[last], ys[last]);
        for (int i = 0; i <= last; ++i) {
            path.lineTo(xs[i], ys[i]);
        }
        myCanvas.drawPath(path, myFillPaint);
    }

    public void drawPolygonalLine(int[] xs, int[] ys) { // 多边形
        final Path path = new Path();
        final int last = xs.length - 1;
        path.moveTo(xs[last], ys[last]);
        for (int i = 0; i <= last; ++i) {
            path.lineTo(xs[i], ys[i]);
        }
        myCanvas.drawPath(path, myLinePaint);
    }

    public void drawOutline(int[] xs, int[] ys) {
//        LogUtil.i25("共有 " + xs.length + " 个点");
        final int last = xs.length - 1;
        int xStart = (xs[0] + xs[last]) / 2;
        int yStart = (ys[0] + ys[last]) / 2;
        int xEnd = xStart;
        int yEnd = yStart;
        if (xs[0] != xs[last]) {
            if (xs[0] > xs[last]) {
                xStart -= 2;
                xEnd += 2;
            } else {
                xStart += 2;
                xEnd -= 2;
            }
        } else {
            if (ys[0] > ys[last]) {
                yStart -= 2;
                yEnd += 2;
            } else {
                yStart += 2;
                yEnd -= 2;
            }
        }

        final Path path = new Path();
        path.moveTo(xStart, yStart);
        for (int i = 0; i <= last; ++i) {
            path.lineTo(xs[i], ys[i]);
        }
        path.lineTo(xEnd, yEnd);
//        myCanvas.drawLine(myLinePaint);
        myCanvas.drawPath(path, myOutlinePaint);
    }

    @Override
    protected void setFontInternal(List<FontEntry> entries, int size, boolean bold, boolean italic, boolean underline, boolean strikeThrought) {
        Typeface typeface = null;
        for (FontEntry e : entries) {
            typeface = AndroidFontUtil.typeface(getSystemInfo(), e, bold, italic);
            if (typeface != null) {
                break;
            }
        }
        myTextPaint.setTypeface(typeface);
        myTextPaint.setTextSize(size);
        myTextPaint.setUnderlineText(underline);
        myTextPaint.setStrikeThruText(strikeThrought);
    }

    @Override
    public void setTextColor(ZLColor color) {
        if (color != null) {
            myTextPaint.setColor(ZLAndroidColorUtil.rgb(color));
        }
    }

    @Override
    public void setLineColor(ZLColor color) {
        if (color != null) {
            myLinePaint.setColor(ZLAndroidColorUtil.rgb(color));
            myOutlinePaint.setColor(ZLAndroidColorUtil.rgb(color));
        }
    }

    @Override
    public void setLineWidth(int width) {
        myLinePaint.setStrokeWidth(width);
    }

    @Override
    public void setFillColor(ZLColor color, int alpha) {
        if (color != null) {
            myFillPaint.setColor(ZLAndroidColorUtil.rgba(color, alpha));
        }
    }

    public int getWidth() {
        return myGeometry.AreaSize.Width - myScrollbarWidth;
    }

    public int getHeight() {
        return myGeometry.AreaSize.Height;
    }

    @Override
    public int getStringWidth(char[] string, int offset, int length) {
        boolean containsSoftHyphen = false;
        for (int i = offset; i < offset + length; ++i) {
            if (string[i] == (char) 0xAD) {
                containsSoftHyphen = true;
                break;
            }
        }
        if (!containsSoftHyphen) {
            return (int) (myTextPaint.measureText(new String(string, offset, length)) + 0.5f);
        } else {
            final char[] corrected = new char[length];
            int len = 0;
            for (int o = offset; o < offset + length; ++o) {
                final char chr = string[o];
                if (chr != (char) 0xAD) {
                    corrected[len++] = chr;
                }
            }
            return (int) (myTextPaint.measureText(corrected, 0, len) + 0.5f);
        }
    }

    @Override
    protected int getSpaceWidthInternal() {
        return (int) (myTextPaint.measureText(" ", 0, 1) + 0.5f);
    }

    @Override
    protected int getCharHeightInternal(char chr) {
        final Rect r = new Rect();
        final char[] txt = new char[]{chr};
        myTextPaint.getTextBounds(txt, 0, 1, r);
        return r.bottom - r.top;
    }

    @Override
    protected int getStringHeightInternal() {
        return (int) (myTextPaint.getTextSize() + 0.5f);
    }

    @Override
    protected int getDescentInternal() {
        return (int) (myTextPaint.descent() + 0.5f);
    }


    @Override
    public void drawString(int x, int y, char[] string, int offset, int length) {
//        LogUtil.i10("drawString1:" + x + ":" + y);
//        LogUtil.i12("drawString2:" + Arrays.toString(string));
//        LogUtil.i10("drawString3:" + offset + ":" + length);
//        y = y + 20;
        boolean containsSoftHyphen = false;
        for (int i = offset; i < offset + length; ++i) {
            if (string[i] == (char) 0xAD) {
                containsSoftHyphen = true;
                break;
            }
        }
        if (!containsSoftHyphen) {
            myCanvas.drawText(string, offset, length, x, y, myTextPaint);
        } else {
            final char[] corrected = new char[length];
            int len = 0;
            for (int o = offset; o < offset + length; ++o) {
                final char chr = string[o];
                if (chr != (char) 0xAD) {
                    corrected[len++] = chr;
                }
            }
            myCanvas.drawText(corrected, 0, len, x, y, myTextPaint);
        }
    }

    @Override
    public Size imageSize(ZLImageData imageData, Size maxSize, ScalingType scaling) {
        final Bitmap bitmap = ((ZLAndroidImageData) imageData).getBitmap(maxSize, scaling);
        return (bitmap != null && !bitmap.isRecycled())
                ? new Size(bitmap.getWidth(), bitmap.getHeight()) : null;
    }

    @Override
    public void drawImage(int x, int y, ZLImageData imageData, Size maxSize, ScalingType scaling, ColorAdjustingMode adjustingMode) {
        final Bitmap bitmap = ((ZLAndroidImageData) imageData).getBitmap(maxSize, scaling);
        if (bitmap != null && !bitmap.isRecycled()) {
            switch (adjustingMode) {
                case LIGHTEN_TO_BACKGROUND:
                    myFillPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.LIGHTEN));
                    break;
                case DARKEN_TO_BACKGROUND:
                    myFillPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));
                    break;
                case NONE:
                    break;
            }
            myCanvas.drawBitmap(bitmap, x, y - bitmap.getHeight(), myFillPaint);
            myFillPaint.setXfermode(null);
        }
    }

    @Override
    public void drawLine(int x0, int y0, int x1, int y1) {
        final Canvas canvas = myCanvas;
        final Paint paint = myLinePaint;
        paint.setAntiAlias(false);
        canvas.drawLine(x0, y0, x1, y1, paint);
        canvas.drawPoint(x0, y0, paint);
        canvas.drawPoint(x1, y1, paint);
        paint.setAntiAlias(true);
    }

    private String getCurrentTOC() {
        final TOCTree tocElement = myReader.getCurrentTOCElement();
        return tocElement == null ? "" : tocElement.getText();
    }

    @Override
    public void drawFooter(String time, String page) {
        mPower = myReader.getBatteryLevel();

        /**
         * 设置电池矩形
         */
        mBatteryRect = new RectF(mCapWidth, 0, mBatteryWidth, mBatteryHeight);

        /**
         * 设置电池盖矩形
         */
        mCapRect = new RectF(0, (mBatteryHeight - mCapHeight) / 2, mCapWidth,
                (mBatteryHeight - mCapHeight) / 2 + mCapHeight);
        /**
         * 设置电量矩形
         */
        mPowerRect = new RectF(mCapWidth + mBatteryStroke / 2 + mPowerPadding + mPowerWidth * ((100f - mPower) / 100f), // 需要调整左边的位置
                mPowerPadding + mBatteryStroke / 2, // 需要考虑到 画笔的宽度
                mBatteryWidth - mPowerPadding * 2,
                mBatteryStroke / 2 + mPowerPadding + mPowerHeight);

        myCanvas.save();
        myCanvas.translate(leftMargin, getHeight() - 13 * dp_1); //y 平移
        myCanvas.drawRoundRect(mBatteryRect, 2f, 2f, mBatteryPaint); // 画电池轮廓需要考虑 画笔的宽度 画圆角矩形
        myCanvas.drawRoundRect(mCapRect, 2f, 2f, mBatteryPaint);// 画电池盖
        myCanvas.drawRect(mPowerRect, mPowerPaint);// 画电量
        myCanvas.restore();

        String value = myReader.ViewOptions.ColorProfileName.getValue();
        if (value.equals("defaultDark")) {
            myFooterPaint.setARGB(255, 192, 192, 192);
            myTopPaint.setARGB(235, 192, 192, 192);
        } else {
            myFooterPaint.setARGB(255, 0, 0, 0);
            myTopPaint.setARGB(235, 0, 0, 0);
        }

        mBookTitle = myReader.getCurrentBook().getTitle();
        mBookToc = getCurrentTOC();

        if (mBookTitle.length() > 13) {
            mBookTitle = mBookTitle.substring(0, 12) + "...";
        }
        if (mBookToc.length() > 12) {
            mBookTitle = mBookToc.substring(0, 11) + "...";
        }

        myCanvas.drawText(mBookTitle, leftMargin, 13 * dp_1, myTopPaint);
        myCanvas.drawText(mBookToc, getWidth() - myTopPaint.measureText(mBookToc) - rightMargin, 13 * dp_1, myTopPaint);

        myCanvas.drawText(time, 45 * dp_1, getHeight() - 4 * dp_1, myFooterPaint);
        myCanvas.drawText(page, getWidth() - myFooterPaint.measureText(page) - rightMargin, getHeight() - 4 * dp_1, myFooterPaint);
    }

    @Override
    public void fillRectangle(int x0, int y0, int x1, int y1) {
        if (x1 < x0) {
            int swap = x1;
            x1 = x0;
            x0 = swap;
        }
        if (y1 < y0) {
            int swap = y1;
            y1 = y0;
            y0 = swap;
        }
        myCanvas.drawRect(x0, y0, x1 + 1, y1 + 1, myFillPaint);
    }

    @Override
    public void fillCircle(int x, int y, int radius) {
        myCanvas.drawCircle(x, y, radius, myFillPaint);
    }
}
