package com.koolearn.android.kooreader;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

public class SettingWindow extends LinearLayout {
    public SettingWindow(Context context) {
        super(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            initAnimator();
        }
    }

    public SettingWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            initAnimator();
        }
    }

    public SettingWindow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            initAnimator();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }


    private Animator myShowHideAnimator;
    private Animator.AnimatorListener myEndShowListener;
    private Animator.AnimatorListener myEndHideListener;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void initAnimator() {
        myEndShowListener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                myShowHideAnimator = null;
                requestLayout();
            }
        };

        myEndHideListener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                myShowHideAnimator = null;
                setVisibility(View.GONE);
            }
        };
    }


    public void show() {
        post(new Runnable() {
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    showAnimatedInternal();
                } else {
                    setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void showAnimatedInternal() {
        if (myShowHideAnimator != null) {
            myShowHideAnimator.end();
        }
        if (getVisibility() == View.VISIBLE) {
            return;
        }
        setVisibility(View.VISIBLE);
        setAlpha(0);
        final AnimatorSet animator = new AnimatorSet();
        animator.setDuration(420);
        animator.play(ObjectAnimator.ofFloat(this, "alpha", 1));
        animator.addListener(myEndShowListener);
        myShowHideAnimator = animator;
        animator.start();
    }

    public void hide() {
        post(new Runnable() {
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    hideAnimatedInternal();
                } else {
                    setVisibility(View.GONE);
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void hideAnimatedInternal() {
        if (myShowHideAnimator != null) {
            myShowHideAnimator.end();
        }
        if (getVisibility() == View.GONE) {
            return;
        }
        setAlpha(1);
        final AnimatorSet animator = new AnimatorSet();
        animator.setDuration(420);
        animator.play(ObjectAnimator.ofFloat(this, "alpha", 0));
        animator.addListener(myEndHideListener);
        myShowHideAnimator = animator;
        animator.start();
    }
}