package com.koolearn.klibrary.ui.android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.koolearn.android.kooreader.KooReader;
import com.koolearn.android.kooreader.KooReaderMainActivity;
import com.koolearn.android.util.LogUtil;
import com.koolearn.klibrary.core.application.ZLApplication;
import com.koolearn.klibrary.core.application.ZLKeyBindings;
import com.koolearn.klibrary.core.util.SystemInfo;
import com.koolearn.klibrary.core.view.ZLView;
import com.koolearn.klibrary.core.view.ZLViewWidget;
import com.koolearn.klibrary.ui.android.curl.CurlView;
import com.koolearn.klibrary.ui.android.view.animation.AnimationProvider;
import com.koolearn.klibrary.ui.android.view.animation.CurlAnimationProvider;
import com.koolearn.klibrary.ui.android.view.animation.CurlPageProviderImpl;
import com.koolearn.klibrary.ui.android.view.animation.NoneAnimationProvider;
import com.koolearn.klibrary.ui.android.view.animation.ShiftAnimationProvider;
import com.koolearn.klibrary.ui.android.view.animation.SlideAnimationProvider;
import com.koolearn.kooreader.Paths;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ZLAndroidCurlWidget extends CurlView {
    public static ZLAndroidCurlWidget Instance() {
        return ourImplementation;
    }

    private static ZLAndroidCurlWidget ourImplementation;
    private final SystemInfo mySystemInfo;

    public ZLAndroidCurlWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mySystemInfo = Paths.systemInfo(context); // 缓存相关
        init();
    }

    public ZLAndroidCurlWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        mySystemInfo = Paths.systemInfo(context);
        init();
    }

    public ZLAndroidCurlWidget(Context context) {
        super(context);
        mySystemInfo = Paths.systemInfo(context);
        init();
    }

    private void init() {
        ourImplementation = this;
        // next line prevent ignoring first onKeyDown DPad event
        // after any dialog was closed
        setFocusableInTouchMode(true);
        setDrawingCacheEnabled(false);
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 新打开时调用
//        getAnimationProvider().terminate();
//        if (myScreenIsTouched) {
//            // 暂未遇到
//            final ZLView view = ZLApplication.Instance().getCurrentView();
//            myScreenIsTouched = false;
//            view.onScrollingFinished(ZLView.PageIndex.current);
//        }
    }

    @Override
    public void onDrawFrame() {
        super.onDrawFrame();
    }

    private AnimationProvider myAnimationProvider;
    private ZLView.Animation myAnimationType;

    private AnimationProvider getAnimationProvider() {
        final ZLView.Animation type = ZLApplication.Instance().getCurrentView().getAnimationType();
        if (myAnimationProvider == null || myAnimationType != type) {
            myAnimationType = type;
            switch (type) {
//                case none:
//                    myAnimationProvider = new NoneAnimationProvider(myBitmapManager);
//                    break;
//                case curl:
//                    myAnimationProvider = new CurlPageProviderImpl(myBitmapManager);
//                    break;
//                case slide:
//                    myAnimationProvider = new SlideAnimationProvider(myBitmapManager);
//                    break;
//                case shift:
//                    myAnimationProvider = new ShiftAnimationProvider(myBitmapManager);
            }
        }
        return myAnimationProvider;
    }



}