//package com.koolearn.klibrary.ui.android.view;
//
//import android.content.Context;
//import android.graphics.Canvas;
//import android.graphics.Paint;
//import android.util.AttributeSet;
//import android.view.KeyEvent;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.ViewConfiguration;
//
//import com.koolearn.android.kooreader.KooReader;
//import com.koolearn.android.kooreader.KooReaderMainActivity;
//import com.koolearn.android.util.LogUtil;
//import com.koolearn.klibrary.core.application.ZLApplication;
//import com.koolearn.klibrary.core.application.ZLKeyBindings;
//import com.koolearn.klibrary.core.util.SystemInfo;
//import com.koolearn.klibrary.core.view.ZLView;
//import com.koolearn.klibrary.core.view.ZLViewWidget;
//import com.koolearn.klibrary.ui.android.view.animation.AnimationProvider;
//import com.koolearn.klibrary.ui.android.view.animation.CurlAnimationProvider;
//import com.koolearn.klibrary.ui.android.view.animation.NoneAnimationProvider;
//import com.koolearn.klibrary.ui.android.view.animation.ShiftAnimationProvider;
//import com.koolearn.klibrary.ui.android.view.animation.SlideAnimationProvider;
//import com.koolearn.klibrary.ui.android.view.animation.SlideOldStyleAnimationProvider;
//import com.koolearn.kooreader.Paths;
//
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//public class ZLAndroidCurlWidget extends View implements ZLViewWidget {
//    public static ZLAndroidCurlWidget Instance() {
//        return ourImplementation;
//    }
//
//    private static ZLAndroidCurlWidget ourImplementation;
//    protected Integer myColorLevel;
//
//    public final ExecutorService PrepareService = Executors.newSingleThreadExecutor();
//
//    private final Paint myPaint = new Paint();
//
//    private final BitmapManagerCurlImpl myBitmapManager = new BitmapManagerCurlImpl(this, getContext());
//    private final SystemInfo mySystemInfo;
//
//    public static ZLAndroidWidget myMainView;
//    public static ZLAndroidCurlWidget myCurlView;
//
//
//    public ZLAndroidCurlWidget(Context context, AttributeSet attrs, int defStyle) {
//        super(context, attrs, defStyle);
//        mySystemInfo = Paths.systemInfo(context); // 缓存相关
//        init();
//    }
//
//    public ZLAndroidCurlWidget(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        mySystemInfo = Paths.systemInfo(context);
//        init();
//    }
//
//    public ZLAndroidCurlWidget(Context context) {
//        super(context);
//        mySystemInfo = Paths.systemInfo(context);
//        init();
//    }
//
//    private void init() {
//        ourImplementation = this;
//        // next line prevent ignoring first onKeyDown DPad event
//        // after any dialog was closed
//        setFocusableInTouchMode(true);
//        setDrawingCacheEnabled(false);
//    }
//
//    @Override
//    public void onSizeChanged(int w, int h, int oldw, int oldh) {
//        super.onSizeChanged(w, h, oldw, oldh);
//        // 新打开时调用
//        getAnimationProvider().terminate();
//        if (myScreenIsTouched) {
//            // 暂未遇到
//            final ZLView view = ZLApplication.Instance().getCurrentView();
//            myScreenIsTouched = false;
//            view.onScrollingFinished(ZLView.PageIndex.current);
//        }
//    }
//
//    @Override
//    public void onDraw(final Canvas canvas) {
//        final Context context = getContext();
//        if (context instanceof KooReader) {
//            ((KooReader) context).createWakeLock();
//        } else {
//            System.err.println("A surprise: view's context is not an KooReader");
//        }
//        myBitmapManager.setSize(getWidth(), getHeight());
//        if (getAnimationProvider().inProgress()) {
//            LogUtil.i12("setProvider()--->");
//            onDrawInScrolling(canvas); // 翻页过程中调用
//        } else {
//            LogUtil.i12("setProvider()");
////            setProvider();
//            onDrawStatic(canvas); // 静态时不停调用
//            ZLApplication.Instance().onRepaintFinished();
//        }
//    }
//
//    private AnimationProvider myAnimationProvider;
//    private ZLView.Animation myAnimationType;
//
//    private AnimationProvider getAnimationProvider() {
//        final ZLView.Animation type = ZLApplication.Instance().getCurrentView().getAnimationType();
//        if (myAnimationProvider == null || myAnimationType != type) {
//            myAnimationType = type;
//            switch (type) {
//                case none:
////                    myCurlView.setVisibility(GONE);
//                    myAnimationProvider = new NoneAnimationProvider(myBitmapManager);
//                    ZLApplication.Instance().getViewWidget().reset(); // 画框后应用
//                    ZLApplication.Instance().getViewWidget().repaint(); // 画框后应用
//                    break;
//                case curl:
////                    myCurlView.setVisibility(VISIBLE);
////                    myMainView.setVisibility(INVISIBLE);
//                    myAnimationProvider = new CurlAnimationProvider(myBitmapManager);
////                    myAnimationProvider = new CurlPageProviderImpl(myBitmapManager);
////                    setProvider();
//                    break;
//                case slide:
////                    myCurlView.setVisibility(INVISIBLE);
//                    myAnimationProvider = new SlideAnimationProvider(myBitmapManager);
//                    break;
//                case slideOldStyle:
////                    myCurlView.setVisibility(INVISIBLE);
//                    myAnimationProvider = new SlideOldStyleAnimationProvider(myBitmapManager);
//                    break;
//                case shift:
////                    myCurlView.setVisibility(INVISIBLE);
//                    myAnimationProvider = new ShiftAnimationProvider(myBitmapManager);
//                    break;
//            }
//        }
//        return myAnimationProvider;
//    }
//
////    private void setProvider() {
////        myBitmapManager.setSize(getWidth(), getHeight());
////        myCurlView.setPageProvider(new CurlView.PageProvider() {
////            @Override
////            public int getPageCount() {
////                return 12;
////            }
////
////            @Override
////            public void updatePage(CurlPage page, int width, int height, int index) {
////                LogUtil.i12("index:" + index);
////                switch (index) {
////                    case 0:
////                        page.setTexture(myBitmapManager.getBitmap(ZLView.PageIndex.previous), CurlPage.SIDE_BOTH);
//////                                    page.setTexture(BitmapFactory.decodeResource(getResources(), R.drawable.bg_green), CurlPage.SIDE_BOTH);
////                        page.setColor(Color.argb(100, 255, 255, 255), CurlPage.SIDE_BACK);
////                        break;
////                    case 1:
////                        page.setTexture(myBitmapManager.getBitmap(ZLView.PageIndex.current), CurlPage.SIDE_BOTH);
//////                                    page.setTexture(BitmapFactory.decodeResource(getResources(), R.drawable.book_pre), CurlPage.SIDE_BOTH);
////                        page.setColor(Color.argb(100, 255, 255, 255), CurlPage.SIDE_BACK);
////                        break;
////                    case 2:
////                        page.setTexture(myBitmapManager.getBitmap(ZLView.PageIndex.next), CurlPage.SIDE_BOTH);
//////                                    page.setTexture(BitmapFactory.decodeResource(getResources(), R.drawable.bg_night), CurlPage.SIDE_BOTH);
////                        page.setColor(Color.argb(100, 255, 255, 255), CurlPage.SIDE_BACK);
////                        break;
////                }
////            }
////        });
////        myCurlView.setCurrentIndex(1);
////    }
//
//    private void onDrawInScrolling(Canvas canvas) {
//        final ZLView view = ZLApplication.Instance().getCurrentView();
//        final AnimationProvider animator = getAnimationProvider();
//        final AnimationProvider.Mode oldMode = animator.getMode();
//        animator.doStep();
//        if (animator.inProgress()) {
//            animator.draw(canvas);
//            if (animator.getMode().Auto) {
//                postInvalidate();
//            }
//        } else {
//            switch (oldMode) {
//                case AnimatedScrollingForward: {
//                    final ZLView.PageIndex index = animator.getPageToScrollTo();
//                    myBitmapManager.shift(index == ZLView.PageIndex.next);
//                    view.onScrollingFinished(index);
//                    ZLApplication.Instance().onRepaintFinished();
//                    break;
//                }
//                case AnimatedScrollingBackward:
//                    view.onScrollingFinished(ZLView.PageIndex.current);
//                    break;
//            }
//            onDrawStatic(canvas);
//        }
//    }
//
//    @Override
//    public void reset() {
//        myBitmapManager.reset();
//    }
//
//    @Override
//    public void repaint() {
//        postInvalidate();
//    }
//
//    @Override
//    public void startManualScrolling(int x, int y, ZLView.Direction direction) {
//        final AnimationProvider animator = getAnimationProvider();
//        animator.setup(direction, getWidth(), getHeight(), myColorLevel);
//        animator.startManualScrolling(x, y);
//    }
//
//    // onFingerMove
//    @Override
//    public void scrollManuallyTo(int x, int y) {
//        final ZLView view = ZLApplication.Instance().getCurrentView();
//        final AnimationProvider animator = getAnimationProvider();
//        if (view.canScroll(animator.getPageToScrollTo(x, y))) {
//            animator.scrollTo(x, y);
//            postInvalidate();
//        }
//    }
//
//    @Override
//    public void startAnimatedScrolling(ZLView.PageIndex pageIndex, int x, int y, ZLView.Direction direction, int speed) {
//        final ZLView view = ZLApplication.Instance().getCurrentView();
//        if (pageIndex == ZLView.PageIndex.current || !view.canScroll(pageIndex)) {
//            return;
//        }
//        final AnimationProvider animator = getAnimationProvider();
//        animator.setup(direction, getWidth(), getHeight(), myColorLevel);
//        animator.startAnimatedScrolling(pageIndex, x, y, speed);
//        if (animator.getMode().Auto) {
//            postInvalidate();
//        }
//    }
//
//    @Override
//    public void startAnimatedScrolling(ZLView.PageIndex pageIndex, ZLView.Direction direction, int speed) {
//        final ZLView view = ZLApplication.Instance().getCurrentView();
//        if (pageIndex == ZLView.PageIndex.current || !view.canScroll(pageIndex)) {
//            return;
//        }
//        final AnimationProvider animator = getAnimationProvider();
//        animator.setup(direction, getWidth(), getHeight(), myColorLevel);
//        animator.startAnimatedScrolling(pageIndex, null, null, speed);
//        if (animator.getMode().Auto) {
//            postInvalidate();
//        }
//    }
//
//    @Override
//    public void startAnimatedScrolling(int x, int y, int speed) { // 翻页滑动
//        final ZLView view = ZLApplication.Instance().getCurrentView();
//        final AnimationProvider animator = getAnimationProvider();
//        if (!view.canScroll(animator.getPageToScrollTo(x, y))) {
//            animator.terminate();
//            return;
//        }
//        animator.startAnimatedScrolling(x, y, speed);
//        postInvalidate(); // 更新视图
//    }
//
////    void drawOnBitmap(Bitmap bitmap, ZLView.PageIndex index) {
////        final ZLView view = ZLApplication.Instance().getCurrentView();
////        if (view == null) {
////            return;
////        }
////        final ZLAndroidPaintContext context = new ZLAndroidPaintContext(
////                mySystemInfo,
////                new Canvas(bitmap),
////                new ZLAndroidPaintContext.Geometry(
////                        getWidth(),
////                        getHeight(),
////                        getWidth(),
////                        getHeight(),
////                        0,
////                        0
////                ),
////                view.isScrollbarShown() ? getVerticalScrollbarWidth() : 0
////        );
////
////        final ZLAndroidPaintContext contextFoot = new ZLAndroidPaintContext(
////                mySystemInfo,
////                new Canvas(bitmap),
////                new ZLAndroidPaintContext.Geometry(
////                        getWidth(),
////                        getHeight(),
////                        getWidth(),
////                        getHeight(),
////                        0,
////                        getHeight()
////                ),
////                view.isScrollbarShown() ? getVerticalScrollbarWidth() : 0
////        );
////        view.paint(context, contextFoot, index);
////    }
//
//    private void onDrawStatic(final Canvas canvas) {
////        setProvider();
//        canvas.drawBitmap(myBitmapManager.getBitmap(ZLView.PageIndex.current), 0, 0, myPaint);
//        post(new Runnable() {
//            public void run() {
//                PrepareService.execute(new Runnable() {
//                    public void run() {
//                        final ZLView view = ZLApplication.Instance().getCurrentView();
//                        final ZLAndroidPaintContext context = new ZLAndroidPaintContext(
//                                mySystemInfo,
//                                canvas,
//                                new ZLAndroidPaintContext.Geometry(
//                                        getWidth(),
//                                        getHeight(),
//                                        getWidth(),
//                                        getHeight(),
//                                        0,
//                                        0
//                                ),
//                                view.isScrollbarShown() ? getVerticalScrollbarWidth() : 0
//                        );
//                        view.preparePage(context, ZLView.PageIndex.next);
//                    }
//                });
//            }
//        });
//    }
//
//    @Override
//    public boolean onTrackballEvent(MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//            onKeyDown(KeyEvent.KEYCODE_DPAD_CENTER, null);
//        } else {
//            ZLApplication.Instance().getCurrentView().onTrackballRotated((int) (10 * event.getX()), (int) (10 * event.getY()));
//        }
//        return true;
//    }
//
//    private volatile boolean myLongClickPerformed;
//
//    private volatile boolean myPendingPress;
//    private int myPressedX, myPressedY;
//    private boolean myScreenIsTouched;
//
//    public void setMyPressedX(int myPressedX) {
//        this.myPressedX = myPressedX;
//    }
//
//    public void setMyPressedY(int myPressedY) {
//        this.myPressedY = myPressedY;
//    }
//
//    public void setMyPendingPress(boolean myPendingPress) {
//        this.myPendingPress = myPendingPress;
//    }
//
//    public void setMyScreenIsTouched(boolean myScreenIsTouched) {
//        this.myScreenIsTouched = myScreenIsTouched;
//    }
//
//    public int getMyPressedX() {
//        return myPressedX;
//    }
//
//    public int getMyPressedY() {
//        return myPressedY;
//    }
//
//    public boolean isMyScreenIsTouched() {
//        return myScreenIsTouched;
//    }
//
//    public boolean isMyPendingPress() {
//        return myPendingPress;
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        int x = (int) event.getX();
//        int y = (int) event.getY();
//
//        final ZLView view = ZLApplication.Instance().getCurrentView();
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                myPendingPress = true;
//                myScreenIsTouched = true;
//                myPressedX = x;
//                myPressedY = y;
//                break;
//            case MotionEvent.ACTION_UP:
//                if (myPendingPress) {
//                    view.onFingerSingleTap(x, y);
//                } else {
//                    view.onFingerRelease(x, y);
//                }
//                myPendingPress = false;
//                myScreenIsTouched = false;
//                break;
//            case MotionEvent.ACTION_MOVE: {
//                final int slop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
//                final boolean isAMove = Math.abs(myPressedX - x) > slop || Math.abs(myPressedY - y) > slop; // 判断是否在移动
//                if (myPendingPress) {
//                    if (isAMove) {
//                        view.onFingerPress(myPressedX, myPressedY);
//                        myPendingPress = false;
//                    }
//                }
//                if (!myPendingPress) {
//                    view.onFingerMove(x, y);
//                }
//                break;
//            }
//            case MotionEvent.ACTION_CANCEL:
//                myPendingPress = false;
//                myScreenIsTouched = false;
//                myLongClickPerformed = false;
////                view.onFingerEventCancelled();
//                break;
//        }
//        return true;
//    }
//
////    @Override
////    public boolean onLongClick(View v) {
////        LogUtil.i3("yulonLongClick");
////
////        final ZLView view = ZLApplication.Instance().getCurrentView();
////        return view.onFingerLongPress(myPressedX, myPressedY);
////    }
//
//    private int myKeyUnderTracking = -1;
//    private long myTrackingStartTime;
//
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        final ZLApplication application = ZLApplication.Instance();
//        final ZLKeyBindings bindings = application.keyBindings();
//
//        if (bindings.hasBinding(keyCode, true) ||
//                bindings.hasBinding(keyCode, false)) {
//            if (myKeyUnderTracking != -1) {
//                if (myKeyUnderTracking == keyCode) {
//                    return true;
//                } else {
//                    myKeyUnderTracking = -1;
//                }
//            }
//            if (bindings.hasBinding(keyCode, true)) {
//                myKeyUnderTracking = keyCode;
//                myTrackingStartTime = System.currentTimeMillis();
//                return true;
//            } else {
//                return application.runActionByKey(keyCode, false);
//            }
//        } else {
//            return false;
//        }
//    }
//
//    @Override
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        if (myKeyUnderTracking != -1) {
//            if (myKeyUnderTracking == keyCode) {
//                final boolean longPress = System.currentTimeMillis() >
//                        myTrackingStartTime + ViewConfiguration.getLongPressTimeout();
//                ZLApplication.Instance().runActionByKey(keyCode, longPress);
//            }
//            myKeyUnderTracking = -1;
//            return true;
//        } else {
//            final ZLKeyBindings bindings = ZLApplication.Instance().keyBindings();
//            return
//                    bindings.hasBinding(keyCode, false) ||
//                            bindings.hasBinding(keyCode, true);
//        }
//    }
//
//    @Override
//    protected int computeVerticalScrollExtent() {
//        final ZLView view = ZLApplication.Instance().getCurrentView();
//        if (!view.isScrollbarShown()) {
//            return 0;
//        }
//        final AnimationProvider animator = getAnimationProvider();
//        if (animator.inProgress()) {
//            final int from = view.getScrollbarThumbLength(ZLView.PageIndex.current);
//            final int to = view.getScrollbarThumbLength(animator.getPageToScrollTo());
//            final int percent = animator.getScrolledPercent();
//            return (from * (100 - percent) + to * percent) / 100;
//        } else {
//            return view.getScrollbarThumbLength(ZLView.PageIndex.current);
//        }
//    }
//
//    @Override
//    protected int computeVerticalScrollOffset() {
//        final ZLView view = ZLApplication.Instance().getCurrentView();
//        if (!view.isScrollbarShown()) {
//            return 0;
//        }
//        final AnimationProvider animator = getAnimationProvider();
//        if (animator.inProgress()) {
//            final int from = view.getScrollbarThumbPosition(ZLView.PageIndex.current);
//            final int to = view.getScrollbarThumbPosition(animator.getPageToScrollTo());
//            final int percent = animator.getScrolledPercent();
//            return (from * (100 - percent) + to * percent) / 100;
//        } else {
//            return view.getScrollbarThumbPosition(ZLView.PageIndex.current);
//        }
//    }
//
//    @Override
//    protected int computeVerticalScrollRange() {
//        final ZLView view = ZLApplication.Instance().getCurrentView();
//        if (!view.isScrollbarShown()) {
//            return 0;
//        }
//        return view.getScrollbarFullSize();
//    }
//
//    protected void updateColorLevel() {
//        ViewUtil.setColorLevel(myPaint, myColorLevel);
//    }
//
//    public final void setScreenBrightness(int percent) { // 禁止子类覆盖
//        if (percent < 1) {
//            percent = 1;
//        } else if (percent > 100) {
//            percent = 100;
//        }
//
//        final Context context = getContext();
//        if (!(context instanceof KooReaderMainActivity)) {
//            return;
//        }
//
//        final float level;
//        final Integer oldColorLevel = myColorLevel;
//        if (percent >= 25) {
//            // 100 => 1f; 25 => .01f
//            level = .01f + (percent - 25) * .99f / 75;
//            myColorLevel = null;
//        } else {
//            level = .01f;
//            myColorLevel = 0x60 + (0xFF - 0x60) * Math.max(percent, 0) / 25;
//        }
//
//        final KooReaderMainActivity activity = (KooReaderMainActivity) context;
//        activity.getZLibrary().ScreenBrightnessLevelOption.setValue(percent);
//        activity.setScreenBrightnessSystem(level);
//        if (oldColorLevel != myColorLevel) {
//            updateColorLevel();
//            postInvalidate();
//        }
//    }
//
//    public final int getScreenBrightness() {
//        if (myColorLevel != null) {
//            return (myColorLevel - 0x60) * 25 / (0xFF - 0x60);
//        }
//
//        final Context context = getContext();
//        if (!(context instanceof KooReaderMainActivity)) {
//            return 50;
//        }
//        final float level = ((KooReaderMainActivity) context).getScreenBrightnessSystem();
//        // level = .01f + (percent - 25) * .99f / 75;
//        return 25 + (int) ((level - .01f) * 75 / .99f);
//    }
//}