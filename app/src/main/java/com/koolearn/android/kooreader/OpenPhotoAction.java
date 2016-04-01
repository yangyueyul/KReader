package com.koolearn.android.kooreader;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;

import com.koolearn.android.util.LogUtil;
import com.koolearn.klibrary.core.image.ZLFileImage;
import com.koolearn.klibrary.core.image.ZLImageData;
import com.koolearn.klibrary.core.image.ZLImageManager;
import com.koolearn.klibrary.ui.android.image.ZLAndroidImageData;
import com.koolearn.klibrary.ui.android.library.ZLAndroidLibrary;
import com.koolearn.kooreader.kooreader.KooReaderApp;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class OpenPhotoAction extends KooAndroidAction {
    static Activity myActivity;
    static ViewGroup mContainer;
    public static boolean isOpen = false;
    private static PhotoView photoView;
    private static RelativeLayout relativeLayout;
    private static float mScaleD;

    OpenPhotoAction(KooReader baseActivity, KooReaderApp kooreader, ViewGroup container) {
        super(baseActivity, kooreader);
        myActivity = baseActivity;
        mContainer = container;
    }

    public static void openImage(String url, int left, int top, int right, int bottom) {
        isOpen = true;
        if (isOpen) {
            final float screenWidth = ZLAndroidLibrary.Instance().getScreenWidth();
            final float screenHeight = ZLAndroidLibrary.Instance().getScreenHeight();
            int mWidth = right - left;
            int mHeight = bottom - top;
//            float mScale = (float) screenWidth / mWidth;
            int orientation = myActivity.getResources().getConfiguration().orientation;
            LogUtil.i8("orientationOption:" + orientation);
            if (orientation == 1) {
                mScaleD = (float) mWidth / screenWidth;
            } else {
                mScaleD = (float) mHeight / screenHeight;
            }
//            float mTop = (screenHeight - mHeight * mScale) / 2;
            final float mTop = screenHeight / 2 - (float) mHeight / 2 - (float) top;

            final String prefix = ZLFileImage.SCHEME + "://";
            if (url != null && url.startsWith(prefix)) {
                ZLFileImage image = ZLFileImage.byUrlPath(url.substring(prefix.length()));
                if (image == null) {
                    // TODO: error message (?)
                    return;
                }
                try {
                    ZLImageData imageData = ZLImageManager.Instance().getImageData(image); // InputStreamImageData
                    Bitmap myBitmap = ((ZLAndroidImageData) imageData).getBitmap(mWidth, mHeight);

                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    relativeLayout = new RelativeLayout(myActivity);
                    relativeLayout.setBackgroundColor(0xF1000000);
                    photoView = new PhotoView(myActivity);
                    photoView.setImageBitmap(myBitmap);
                    mContainer.addView(relativeLayout, params);
                    mContainer.addView(photoView, params);

                    AnimatorSet setIn = new AnimatorSet();
                    setIn.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            AlphaAnimation aa = new AlphaAnimation(0, 1);
                            aa.setDuration(520);
                            relativeLayout.startAnimation(aa);
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {

                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    setIn.setTarget(photoView);
                    ObjectAnimator oa1 = ObjectAnimator.ofFloat(photoView, "scaleX", mScaleD, 1);
                    ObjectAnimator oa2 = ObjectAnimator.ofFloat(photoView, "scaleY", mScaleD, 1);
//                    ObjectAnimator oa3 = ObjectAnimator.ofFloat(photoView, "alpha", 0, 1);
                    ObjectAnimator oa5 = ObjectAnimator.ofFloat(photoView, "translationX", 0, 0);
                    ObjectAnimator oa6 = ObjectAnimator.ofFloat(photoView, "translationY", 0 - mTop, 0);
                    setIn.setDuration(520);
                    setIn.playTogether(oa1, oa2, oa5, oa6);
                    setIn.start();

                    photoView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
                        @Override
                        public void onViewTap(View view, float x, float y) {
                            AnimatorSet setIn = new AnimatorSet();
                            setIn.addListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    AlphaAnimation aa = new AlphaAnimation(1, 0);
                                    aa.setDuration(520);
                                    relativeLayout.startAnimation(aa);
                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    mContainer.removeView(relativeLayout);
                                    mContainer.removeView(photoView);
                                    isOpen = false;
                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {

                                }

                                @Override
                                public void onAnimationRepeat(Animator animation) {

                                }
                            });
                            setIn.setTarget(photoView);
                            ObjectAnimator oa1 = ObjectAnimator.ofFloat(photoView, "scaleX", 1, mScaleD);
                            ObjectAnimator oa2 = ObjectAnimator.ofFloat(photoView, "scaleY", 1, mScaleD);
//                            ObjectAnimator oa3 = ObjectAnimator.ofFloat(photoView, "alpha", 1, 0);
                            ObjectAnimator oa5 = ObjectAnimator.ofFloat(photoView, "translationX", 0, 0);
                            ObjectAnimator oa6 = ObjectAnimator.ofFloat(photoView, "translationY", 0, 0 - mTop);
                            setIn.setDuration(520);
                            setIn.playTogether(oa1, oa2, oa5, oa6);
                            setIn.start();
                        }
                    });
                } catch (Exception e) {
                    // TODO: error message (?)
                    e.printStackTrace();
                }
            } else {
                // TODO: error message (?)
                return;
            }
        }
    }

    @Override
    protected void run(Object... params) {

    }
}
