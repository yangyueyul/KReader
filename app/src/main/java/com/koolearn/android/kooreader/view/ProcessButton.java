package com.koolearn.android.kooreader.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.GradientDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import com.koolearn.klibrary.ui.android.R;

/**
 * ******************************************
 * 作    者 ：  杨越
 * 版    本 ：  1.0
 * 创建日期 ：  2016/3/30
 * 描    述 ：
 * 修订历史 ：
 * ******************************************
 */
public abstract class ProcessButton extends FlatButton {

    private int mProgress;
    private int mMaxProgress;
    private int mMinProgress;

    private GradientDrawable mProgressDrawable;
    private GradientDrawable mCompleteDrawable;
    private GradientDrawable mErrorDrawable;

    private CharSequence mLoadingText;
    private CharSequence mCompleteText;
    private CharSequence mErrorText;

    public ProcessButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public ProcessButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ProcessButton(Context context) {
        super(context);
        init(context, null);
    }

    private void init(Context context, AttributeSet attrs) {
        mMinProgress = 0;
        mMaxProgress = 100;

        mProgressDrawable = (GradientDrawable) getDrawable(R.drawable.rect_progress).mutate();
        mProgressDrawable.setCornerRadius(getCornerRadius());

        mCompleteDrawable = (GradientDrawable) getDrawable(R.drawable.rect_complete).mutate();
        mCompleteDrawable.setCornerRadius(getCornerRadius());

        mErrorDrawable = (GradientDrawable) getDrawable(R.drawable.rect_error).mutate();
        mErrorDrawable.setCornerRadius(getCornerRadius());

        if (attrs != null) {
            initAttributes(context, attrs);
        }
    }

    private void initAttributes(Context context, AttributeSet attributeSet) {
        TypedArray attr = getTypedArray(context, attributeSet, R.styleable.ProcessButton);

        if (attr == null) {
            return;
        }

        try {
            mLoadingText = attr.getString(R.styleable.ProcessButton_pb_textProgress);
            mCompleteText = attr.getString(R.styleable.ProcessButton_pb_textComplete);
            mErrorText = attr.getString(R.styleable.ProcessButton_pb_textError);

            int purple = getColor(R.color.button_compelete);
            int colorProgress = attr.getColor(R.styleable.ProcessButton_pb_colorProgress, purple);
            mProgressDrawable.setColor(colorProgress);

            int green = getColor(R.color.button_progress);
            int colorComplete = attr.getColor(R.styleable.ProcessButton_pb_colorComplete, green);
            mCompleteDrawable.setColor(colorComplete);

            int red = getColor(R.color.button_error);
            int colorError = attr.getColor(R.styleable.ProcessButton_pb_colorError, red);
            mErrorDrawable.setColor(colorError);

        } finally {
            attr.recycle();
        }
    }

    public void setProgress(int progress) {
        mProgress = progress;

        if (mProgress == mMinProgress) {
            onNormalState();
        } else if (mProgress == mMaxProgress) {
            onCompleteState();
        } else if (mProgress < mMinProgress) {
            onErrorState();
        } else {
            onProgress();
        }

        invalidate();
    }

    protected void onErrorState() {
        if (getErrorText() != null) {
            setText(getErrorText());
        }
        setBackgroundCompat(getErrorDrawable());
    }

    protected void onProgress() {
//        if (getLoadingText() != null) {
        setText("正在下载...");
//        }
        setBackgroundCompat(getNormalDrawable());
    }

    protected void onCompleteState() {
//        if(getCompleteText() != null) {
        setText("打开");
//        }
        setBackgroundCompat(getCompleteDrawable());
    }

    protected void onNormalState() {
//        if (getNormalText() != null) {
        setText(getNormalText());
//        }
        setBackgroundCompat(getNormalDrawable());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // progress
        if (mProgress > mMinProgress && mProgress < mMaxProgress) {
            drawProgress(canvas);
        }

        super.onDraw(canvas);
    }

    public abstract void drawProgress(Canvas canvas);

    public int getProgress() {
        return mProgress;
    }

    public int getMaxProgress() {
        return mMaxProgress;
    }

    public int getMinProgress() {
        return mMinProgress;
    }

    public GradientDrawable getProgressDrawable() {
        return mProgressDrawable;
    }

    public GradientDrawable getCompleteDrawable() {
        return mCompleteDrawable;
    }

    public CharSequence getLoadingText() {
        return mLoadingText;
    }

    public CharSequence getCompleteText() {
        return mCompleteText;
    }

    public void setProgressDrawable(GradientDrawable progressDrawable) {
        mProgressDrawable = progressDrawable;
    }

    public void setCompleteDrawable(GradientDrawable completeDrawable) {
        mCompleteDrawable = completeDrawable;
    }

    public void setLoadingText(CharSequence loadingText) {
        mLoadingText = loadingText;
    }

    public void setCompleteText(CharSequence completeText) {
        mCompleteText = completeText;
    }

    public GradientDrawable getErrorDrawable() {
        return mErrorDrawable;
    }

    public void setErrorDrawable(GradientDrawable errorDrawable) {
        mErrorDrawable = errorDrawable;
    }

    public CharSequence getErrorText() {
        return mErrorText;
    }

    public void setErrorText(CharSequence errorText) {
        mErrorText = errorText;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.mProgress = mProgress;

        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState savedState = (SavedState) state;
            mProgress = savedState.mProgress;
            super.onRestoreInstanceState(savedState.getSuperState());
            setProgress(mProgress);
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    public static class SavedState extends BaseSavedState {

        private int mProgress;

        public SavedState(Parcelable parcel) {
            super(parcel);
        }

        private SavedState(Parcel in) {
            super(in);
            mProgress = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mProgress);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
