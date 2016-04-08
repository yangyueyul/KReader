package com.koolearn.klibrary.ui.android.view.animation;

import com.koolearn.klibrary.core.view.ZLView;
import com.koolearn.klibrary.ui.android.curl.CurlPage;

/**
 * ******************************************
 * 作    者 ：  杨越
 * 版    本 ：  1.0
 * 创建日期 ：  2016/3/28 ${time}
 * 描    述 ：
 * 修订历史 ：
 * ******************************************
 */

public abstract interface PageProvider
{
    public abstract boolean hasNextPage();

    public abstract boolean hasPreviousPage();

    // CURL_LEFT = 1;
//     CURL_NONE = 0;
//     CURL_RIGHT = 2;
    public abstract void shift(int param);

    public abstract void updatePage(CurlPage paramhz, int paramInt1, int paramInt2, ZLView.PageIndex paramPageIndex);
}