package com.koolearn.android.kooreader.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

/**
 * ******************************************
 * 作    者 ：  杨越
 * 版    本 ：  1.0
 * 创建日期 ：  2016/3/30
 * 描    述 ：
 * 修订历史 ：
 * ******************************************
 */
public class DownloadProcessButton extends ProcessButton {

    public DownloadProcessButton(Context context) {
        super(context);
    }

    public DownloadProcessButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DownloadProcessButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void drawProgress(Canvas canvas) {
        float scale = (float) getProgress() / (float) getMaxProgress();
        float indicatorWidth = (float) getMeasuredWidth() * scale;

        getProgressDrawable().setBounds(0, 0, (int) indicatorWidth, getMeasuredHeight());
        getProgressDrawable().draw(canvas);
    }

}