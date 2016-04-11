package com.koolearn.klibrary.ui.android.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.koolearn.android.kooreader.KooReader;
import com.koolearn.android.kooreader.KooReaderMainActivity;
import com.koolearn.klibrary.core.application.ZLApplication;
import com.koolearn.klibrary.core.application.ZLKeyBindings;
import com.koolearn.klibrary.core.util.SystemInfo;
import com.koolearn.klibrary.core.view.ZLView;
import com.koolearn.klibrary.core.view.ZLViewWidget;
import com.koolearn.klibrary.ui.android.view.animation.AnimationProvider;
import com.koolearn.klibrary.ui.android.view.animation.CurlAnimationProvider;
import com.koolearn.klibrary.ui.android.view.animation.CurlPageProviderImpl;
import com.koolearn.klibrary.ui.android.view.animation.NoneAnimationProvider;
import com.koolearn.klibrary.ui.android.view.animation.ShiftAnimationProvider;
import com.koolearn.klibrary.ui.android.view.animation.SlideAnimationProvider;
import com.koolearn.kooreader.Paths;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ZLAndroidWidget extends View implements ZLViewWidget, View.OnLongClickListener {
    protected Integer myColorLevel;

    public final ExecutorService PrepareService = Executors.newSingleThreadExecutor();

    private final Paint myPaint = new Paint();

    private final BitmapManagerImpl myBitmapManager = new BitmapManagerImpl(this, getContext());
    private final SystemInfo mySystemInfo;

    public ZLAndroidWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mySystemInfo = Paths.systemInfo(context); // 缓存相关
        init();
    }

    public ZLAndroidWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        mySystemInfo = Paths.systemInfo(context);
        init();
    }

    public ZLAndroidWidget(Context context) {
        super(context);
        mySystemInfo = Paths.systemInfo(context);
        init();
    }

    private void init() {
        // next line prevent ignoring first onKeyDown DPad event
        // after any dialog was closed
        setFocusableInTouchMode(true);
        setDrawingCacheEnabled(false);
        setOnLongClickListener(this);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 新打开时调用
        getAnimationProvider().terminate();
        if (myScreenIsTouched) {
            // 手指触摸屏幕,并旋转手机
            final ZLView view = ZLApplication.Instance().getCurrentView();
            myScreenIsTouched = false;
            view.onScrollingFinished(ZLView.PageIndex.current);
        }
    }

    @Override
    public void onDraw(final Canvas canvas) {
        final Context context = getContext();
        if (context instanceof KooReader) {
            ((KooReader) context).createWakeLock();
        } else {
            System.err.println("A surprise: view's context is not an KooReader");
        }

        myBitmapManager.setSize(getWidth(), getHeight());

        if (getAnimationProvider().inProgress()) {
            onDrawInScrolling(canvas); // 翻页过程中调用
        } else {

            onDrawStatic(canvas); // 首次/页面跳转时调用,防止黑屏
            ZLApplication.Instance().onRepaintFinished();
        }
    }

    private AnimationProvider myAnimationProvider;
    private ZLView.Animation myAnimationType;

    private AnimationProvider getAnimationProvider() {
        final ZLView.Animation type = ZLApplication.Instance().getCurrentView().getAnimationType();
        if (myAnimationProvider == null || myAnimationType != type) {
            myAnimationType = type;
            switch (type) {
                case none:
                    myAnimationProvider = new NoneAnimationProvider(myBitmapManager);
                    break;
                case curl:
                    myAnimationProvider = new CurlAnimationProvider(myBitmapManager);
//                    myAnimationProvider = new CurlPageProviderImpl(myBitmapManager);
                    break;
                case slide:
                    myAnimationProvider = new SlideAnimationProvider(myBitmapManager);
                    break;
                case shift:
                    myAnimationProvider = new ShiftAnimationProvider(myBitmapManager);
                    break;
            }
        }
        return myAnimationProvider;
    }

    public boolean isCurlAnimation(){
        if(getAnimationProvider() instanceof CurlPageProviderImpl){
            return true;
        }else{
            return false;
        }
    }

    private void onDrawInScrolling(Canvas canvas) {
        final ZLView view = ZLApplication.Instance().getCurrentView();

        final AnimationProvider animator = getAnimationProvider();
        final AnimationProvider.Mode oldMode = animator.getMode();
        animator.doStep();
        if (animator.inProgress()) { // 动画过程中执行
            animator.draw(canvas); // 动画绘制
            if (animator.getMode().Auto) { // 松手后完成后续绘制
                postInvalidate();
            }
        } else {                     // 动画结束后执行, 无动画情况只会调用这个
            switch (oldMode) {
                case AnimatedScrollingForward: { // 当翻到 下一页/上一页 时调用
                    final ZLView.PageIndex index = animator.getPageToScrollTo(); // 得到翻页后的KooView 向左翻->next 向右翻->previous
                    // 若为next     -> next->current,current->previous
                    // 若为previous -> current->next,previous->current
                    myBitmapManager.shift(index == ZLView.PageIndex.next);
                    view.onScrollingFinished(index);
                    ZLApplication.Instance().onRepaintFinished();
                    break;
                }
                case AnimatedScrollingBackward: // 没有翻到 下一页/上一页 则还在当前页
                    view.onScrollingFinished(ZLView.PageIndex.current);
                    break;
            }
            onDrawStatic(canvas);
        }
    }

    @Override
    public void reset() {
        myBitmapManager.reset();
    }

    @Override
    public void repaint() {
        postInvalidate();
    }

    // onFingerPress
    // 是否支持拖动翻页
    // 开始翻页
    @Override
    public void startManualScrolling(int x, int y, ZLView.Direction direction) {
        final AnimationProvider animator = getAnimationProvider();
        animator.setup(direction, getWidth(), getHeight(), myColorLevel);
        animator.startManualScrolling(x, y); // PreManualScrolling 先pre 然后判断是ManualScrolling还是NoScrolling
    }

    // onFingerMove
    @Override
    public void scrollManuallyTo(int x, int y) {
        final ZLView view = ZLApplication.Instance().getCurrentView();
        final AnimationProvider animator = getAnimationProvider();
        if (view.canScroll(animator.getPageToScrollTo(x, y))) { // 判断是否可以翻(是否有上/下一页)
            animator.scrollTo(x, y); // 一直在改变Mode的状态
            postInvalidate();
        }
    }

    @Override
    public void startAnimatedScrolling(ZLView.PageIndex pageIndex, int x, int y, ZLView.Direction direction, int speed) {
        final ZLView view = ZLApplication.Instance().getCurrentView();
        if (pageIndex == ZLView.PageIndex.current || !view.canScroll(pageIndex)) {
            return;
        }
        final AnimationProvider animator = getAnimationProvider();
        animator.setup(direction, getWidth(), getHeight(), myColorLevel);
        animator.startAnimatedScrolling(pageIndex, x, y, speed);
        if (animator.getMode().Auto) {
            postInvalidate();
        }
    }

    @Override
    public void startAnimatedScrolling(ZLView.PageIndex pageIndex, ZLView.Direction direction, int speed) {
        final ZLView view = ZLApplication.Instance().getCurrentView();
        if (pageIndex == ZLView.PageIndex.current || !view.canScroll(pageIndex)) {
            return;
        }
        final AnimationProvider animator = getAnimationProvider();
        animator.setup(direction, getWidth(), getHeight(), myColorLevel);
        animator.startAnimatedScrolling(pageIndex, null, null, speed);
        if (animator.getMode().Auto) {
            postInvalidate();
        }
    }

    @Override
    public void startAnimatedScrolling(int x, int y, int speed) { // 翻页滑动
        final ZLView view = ZLApplication.Instance().getCurrentView();
        final AnimationProvider animator = getAnimationProvider();
        if (!view.canScroll(animator.getPageToScrollTo(x, y))) {
            animator.terminate();
            return;
        }
        animator.startAnimatedScrolling(x, y, speed);
        postInvalidate(); // 更新视图
    }

    void drawOnBitmap(Bitmap bitmap, ZLView.PageIndex index) {
        final ZLView view = ZLApplication.Instance().getCurrentView();
        if (view == null) {
            return;
        }

        final ZLAndroidPaintContext context = new ZLAndroidPaintContext(
                mySystemInfo,
                new Canvas(bitmap),
                new ZLAndroidPaintContext.Geometry(
                        getWidth(),
                        getHeight(),
                        getWidth(),
                        getHeight(),
                        0,
                        0
                ), 0);
        view.paint(context, index);
    }


    private void onDrawStatic(final Canvas canvas) {  // 滑动完后调用静态时调用
        /**
         * 从myBitmapManager获取一张Bitmap,画到画布上
         * myBitmapManager.getBitmap(ZLView.PageIndex.current)是自己创建的canvas,将该view的canva和其连起来才可以显示在view上
         */
        canvas.drawBitmap(myBitmapManager.getBitmap(ZLView.PageIndex.current), 0, 0, myPaint);
        post(new Runnable() { // 将runnable放到消息队列中
            public void run() {
                PrepareService.execute(new Runnable() {
                    public void run() {
                        final ZLView view = ZLApplication.Instance().getCurrentView(); // 得到当前view
                        final ZLAndroidPaintContext context = new ZLAndroidPaintContext(
                                mySystemInfo, canvas,
                                new ZLAndroidPaintContext.Geometry(getWidth(), getHeight(), getWidth(), getHeight(), 0, 0), 0);
                        view.preparePage(context, ZLView.PageIndex.next); // 准备下一页
                    }
                });
            }
        });
    }

    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            onKeyDown(KeyEvent.KEYCODE_DPAD_CENTER, null);
        } else {
            ZLApplication.Instance().getCurrentView().onTrackballRotated((int) (10 * event.getX()), (int) (10 * event.getY()));
        }
        return true;
    }

    private class LongClickRunnable implements Runnable {
        @Override
        public void run() {
            if (performLongClick()) {
                myLongClickPerformed = true;
            }
        }
    }

    private volatile LongClickRunnable myPendingLongClickRunnable;
    private volatile boolean myLongClickPerformed;

    private void postLongClickRunnable() {
        myLongClickPerformed = false;
        myPendingPress = false;
        if (myPendingLongClickRunnable == null) {
            myPendingLongClickRunnable = new LongClickRunnable();
        }
        postDelayed(myPendingLongClickRunnable, 2 * ViewConfiguration.getLongPressTimeout());
    }

    private class ShortClickRunnable implements Runnable {
        @Override
        public void run() {
            final ZLView view = ZLApplication.Instance().getCurrentView();
            view.onFingerSingleTap(myPressedX, myPressedY);
            myPendingPress = false;
            myPendingShortClickRunnable = null;
        }
    }

    private volatile ShortClickRunnable myPendingShortClickRunnable;

    private volatile boolean myPendingPress;
    private volatile boolean myPendingDoubleTap;
    private int myPressedX, myPressedY;
    private boolean myScreenIsTouched;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        final ZLView view = ZLApplication.Instance().getCurrentView();
        switch (event.getAction()) {
            case MotionEvent.ACTION_CANCEL:
                myPendingDoubleTap = false;
                myPendingPress = false;
                myScreenIsTouched = false;
                myLongClickPerformed = false;
                if (myPendingShortClickRunnable != null) {
                    removeCallbacks(myPendingShortClickRunnable);
                    myPendingShortClickRunnable = null;
                }
                if (myPendingLongClickRunnable != null) {
                    removeCallbacks(myPendingLongClickRunnable);
                    myPendingLongClickRunnable = null;
                }
                view.onFingerEventCancelled();
                break;
            case MotionEvent.ACTION_UP:
                if (myPendingDoubleTap) {
                    view.onFingerDoubleTap(x, y);
                } else if (myLongClickPerformed) {
                    view.onFingerReleaseAfterLongPress(x, y);
                } else {
                    if (myPendingLongClickRunnable != null) {
                        removeCallbacks(myPendingLongClickRunnable);
                        myPendingLongClickRunnable = null;
                    }
                    if (myPendingPress) {
                        if (view.isDoubleTapSupported()) {
                            if (myPendingShortClickRunnable == null) {
                                myPendingShortClickRunnable = new ShortClickRunnable();
                            }
                            postDelayed(myPendingShortClickRunnable, ViewConfiguration.getDoubleTapTimeout());
                        } else {
                            view.onFingerSingleTap(x, y);
                        }
                    } else {
                        view.onFingerRelease(x, y);
                    }
                }
                myPendingDoubleTap = false;
                myPendingPress = false;
                myScreenIsTouched = false;
                break;
            case MotionEvent.ACTION_DOWN:
                if (myPendingShortClickRunnable != null) {
                    removeCallbacks(myPendingShortClickRunnable);
                    myPendingShortClickRunnable = null;
                    myPendingDoubleTap = true;
                } else {
                    postLongClickRunnable();
                    myPendingPress = true;
                }
                myScreenIsTouched = true;
                myPressedX = x;
                myPressedY = y;
                break;
            case MotionEvent.ACTION_MOVE: {
                final int slop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
                final boolean isAMove =
                        Math.abs(myPressedX - x) > slop || Math.abs(myPressedY - y) > slop;
                if (isAMove) {
                    myPendingDoubleTap = false;
                }
                if (myLongClickPerformed) {
                    view.onFingerMoveAfterLongPress(x, y);
                } else {
                    if (myPendingPress) {
                        if (isAMove) {
                            if (myPendingShortClickRunnable != null) {
                                removeCallbacks(myPendingShortClickRunnable);
                                myPendingShortClickRunnable = null;
                            }
                            if (myPendingLongClickRunnable != null) {
                                removeCallbacks(myPendingLongClickRunnable);
                            }
                            view.onFingerPress(myPressedX, myPressedY);
                            myPendingPress = false;
                        }
                    }
                    if (!myPendingPress) {
                        // 开始切换 surfaceview
                        if(isCurlAnimation()){
                            ZLApplication.Instance().getMyWindow().hideViewWidget(true);
                            return true;
                        }
                        view.onFingerMove(x, y);
                    }
                }
                break;
            }
        }
        return true;
    }

    @Override
    public boolean onLongClick(View v) {
        final ZLView view = ZLApplication.Instance().getCurrentView();
        return view.onFingerLongPress(myPressedX, myPressedY);
    }

    private int myKeyUnderTracking = -1;
    private long myTrackingStartTime;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        final ZLApplication application = ZLApplication.Instance();
        final ZLKeyBindings bindings = application.keyBindings();

        if (bindings.hasBinding(keyCode, true) ||
                bindings.hasBinding(keyCode, false)) {
            if (myKeyUnderTracking != -1) {
                if (myKeyUnderTracking == keyCode) {
                    return true;
                } else {
                    myKeyUnderTracking = -1;
                }
            }
            if (bindings.hasBinding(keyCode, true)) {
                myKeyUnderTracking = keyCode;
                myTrackingStartTime = System.currentTimeMillis();
                return true;
            } else {
                return application.runActionByKey(keyCode, false);
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (myKeyUnderTracking != -1) {
            if (myKeyUnderTracking == keyCode) {
                final boolean longPress = System.currentTimeMillis() >
                        myTrackingStartTime + ViewConfiguration.getLongPressTimeout();
                ZLApplication.Instance().runActionByKey(keyCode, longPress);
            }
            myKeyUnderTracking = -1;
            return true;
        } else {
            final ZLKeyBindings bindings = ZLApplication.Instance().keyBindings();
            return
                    bindings.hasBinding(keyCode, false) ||
                            bindings.hasBinding(keyCode, true);
        }
    }

    @Override
    protected int computeVerticalScrollExtent() {
        final ZLView view = ZLApplication.Instance().getCurrentView();
        if (!view.isScrollbarShown()) {
            return 0;
        }
        final AnimationProvider animator = getAnimationProvider();
        if (animator.inProgress()) {
            final int from = view.getScrollbarThumbLength(ZLView.PageIndex.current);
            final int to = view.getScrollbarThumbLength(animator.getPageToScrollTo());
            final int percent = animator.getScrolledPercent();
            return (from * (100 - percent) + to * percent) / 100;
        } else {
            return view.getScrollbarThumbLength(ZLView.PageIndex.current);
        }
    }

    @Override
    protected int computeVerticalScrollOffset() {
        final ZLView view = ZLApplication.Instance().getCurrentView();
        if (!view.isScrollbarShown()) {
            return 0;
        }
        final AnimationProvider animator = getAnimationProvider();
        if (animator.inProgress()) {
            final int from = view.getScrollbarThumbPosition(ZLView.PageIndex.current);
            final int to = view.getScrollbarThumbPosition(animator.getPageToScrollTo());
            final int percent = animator.getScrolledPercent();
            return (from * (100 - percent) + to * percent) / 100;
        } else {
            return view.getScrollbarThumbPosition(ZLView.PageIndex.current);
        }
    }

    @Override
    protected int computeVerticalScrollRange() {
        final ZLView view = ZLApplication.Instance().getCurrentView();
        if (!view.isScrollbarShown()) {
            return 0;
        }
        return view.getScrollbarFullSize();
    }

    protected void updateColorLevel() {
        ViewUtil.setColorLevel(myPaint, myColorLevel);
    }

    public final void setScreenBrightness(int percent) { // 禁止子类覆盖
        if (percent < 1) {
            percent = 1;
        } else if (percent > 100) {
            percent = 100;
        }

        final Context context = getContext();
        if (!(context instanceof KooReaderMainActivity)) {
            return;
        }

        final float level;
        final Integer oldColorLevel = myColorLevel;
        if (percent >= 25) {
            // 100 => 1f; 25 => .01f
            level = .01f + (percent - 25) * .99f / 75;
            myColorLevel = null;
        } else {
            level = .01f;
            myColorLevel = 0x60 + (0xFF - 0x60) * Math.max(percent, 0) / 25;
        }

        final KooReaderMainActivity activity = (KooReaderMainActivity) context;
        activity.getZLibrary().ScreenBrightnessLevelOption.setValue(percent);
        activity.setScreenBrightnessSystem(level);
        if (oldColorLevel != myColorLevel) {
            updateColorLevel();
            postInvalidate();
        }
    }

    public final int getScreenBrightness() {
        if (myColorLevel != null) {
            return (myColorLevel - 0x60) * 25 / (0xFF - 0x60);
        }

        final Context context = getContext();
        if (!(context instanceof KooReaderMainActivity)) {
            return 50;
        }
        final float level = ((KooReaderMainActivity) context).getScreenBrightnessSystem();
        // level = .01f + (percent - 25) * .99f / 75;
        return 25 + (int) ((level - .01f) * 75 / .99f);
    }
}