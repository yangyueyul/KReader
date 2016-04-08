///*
// * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
// *
// * This program is free software; you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation; either version 2 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program; if not, write to the Free Software
// * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
// * 02110-1301, USA.
// */
//
//package com.koolearn.android.kooreader;
//
//import android.animation.Animator;
//import android.animation.AnimatorSet;
//import android.animation.ObjectAnimator;
//import android.app.Activity;
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.view.View;
//import android.widget.LinearLayout;
//
//import com.koolearn.klibrary.core.image.ZLFileImage;
//import com.koolearn.klibrary.core.image.ZLImageData;
//import com.koolearn.klibrary.core.image.ZLImageManager;
//import com.koolearn.klibrary.ui.android.R;
//import com.koolearn.klibrary.ui.android.image.ZLAndroidImageData;
//
//import uk.co.senab.photoview.PhotoView;
//import uk.co.senab.photoview.PhotoViewAttacher;
//
//public class PhotoViewActivity extends Activity {
//    public static final String URL_KEY = "kooreader.imageview.url";
//    //    public static final String LIGHT = "kooreader.imageview.light";
//    private Bitmap myBitmap;
//    private PhotoView photoView;
//    private LinearLayout llPhotoView;
//    Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            finish();
//            overridePendingTransition(R.anim.tran_fade_in_long, R.anim.tran_fade_out_long);
//        }
//    };
//
//    @Override
//    protected void onCreate(Bundle icicle) {
//        super.onCreate(icicle);
//        setContentView(R.layout.activity_photoview);
//        photoView = (PhotoView) findViewById(R.id.iv_photo_view);
//        llPhotoView = (LinearLayout) findViewById(R.id.ll_photo_view);
//
//        AnimatorSet setIn = new AnimatorSet();
//        setIn.setTarget(photoView);
//        ObjectAnimator oa1 = ObjectAnimator.ofFloat(photoView, "scaleX", 0.5f, 1);
//        oa1.setDuration(420);
//        ObjectAnimator oa2 = ObjectAnimator.ofFloat(photoView, "scaleY", 0.5f, 1);
//        oa2.setDuration(420);
//        ObjectAnimator oa3 = ObjectAnimator.ofFloat(photoView, "alpha", 0, 1);
//        oa3.setDuration(420);
//        setIn.playTogether(oa1, oa2, oa3);
//        setIn.start();
//
////        final int brightnessLevel =
////                ((ZLAndroidApplication) getApplication()).library().ScreenBrightnessLevelOption.getValue();
////        LogUtil.i5("brightnessLevel:"+brightnessLevel);
////        if (brightnessLevel != 0) {
////            final WindowManager.LayoutParams attrs = getWindow().getAttributes();
////            attrs.screenBrightness = brightnessLevel;
////            getWindow().setAttributes(attrs);
////        }
//
//        final Intent intent = getIntent();
//
//        final String url = intent.getStringExtra(URL_KEY);
//        final String prefix = ZLFileImage.SCHEME + "://";
//        if (url != null && url.startsWith(prefix)) {
//            final ZLFileImage image = ZLFileImage.byUrlPath(url.substring(prefix.length()));
//            if (image == null) {
//                // TODO: error message (?)
//                finish();
//            }
//            try {
//                final ZLImageData imageData = ZLImageManager.Instance().getImageData(image);
//                myBitmap = ((ZLAndroidImageData) imageData).getFullSizeBitmap();
//                photoView.setImageBitmap(myBitmap);
//                photoView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
//                    @Override
//                    public void onViewTap(View view, float x, float y) {
//                        back();
//                    }
//                });
//            } catch (Exception e) {
//                // TODO: error message (?)
//                e.printStackTrace();
//                finish();
//            }
//        } else {
//            // TODO: error message (?)
//            finish();
//        }
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (myBitmap != null) {
//            myBitmap.recycle();
//        }
//        myBitmap = null;
//    }
//
//    private void back() {
//        AnimatorSet set = new AnimatorSet();
//        set.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//                handler.sendEmptyMessageDelayed(1, 280);
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//
//            }
//        });
//        set.setTarget(photoView);
//        ObjectAnimator oa1 = ObjectAnimator.ofFloat(photoView, "scaleX", 1, 0);
//        oa1.setDuration(360);
//        ObjectAnimator oa2 = ObjectAnimator.ofFloat(photoView, "scaleY", 1, 0);
//        oa2.setDuration(360);
//        ObjectAnimator oa3 = ObjectAnimator.ofFloat(photoView, "alpha", 1, 0);
//        oa3.setDuration(360);
//        set.playTogether(oa1, oa2, oa3);
//        set.start();
////        finish();
////        overridePendingTransition(R.anim.tran_fade_in_long, R.anim.tran_fade_out_long);
//    }
//
//    @Override
//    public void onBackPressed() {
//        back();
//    }
//}
