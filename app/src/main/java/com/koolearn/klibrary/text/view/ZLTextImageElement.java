package com.koolearn.klibrary.text.view;

import com.koolearn.android.util.LogUtil;
import com.koolearn.klibrary.core.image.ZLImageData;

public final class ZLTextImageElement extends ZLTextElement {
    public final String Id;
    public final ZLImageData ImageData;
    public final String URL;
    public final boolean IsCover; // 这里判断有问题

    ZLTextImageElement(String id, ZLImageData imageData, String url, boolean isCover) {
        Id = id;
        ImageData = imageData;
        URL = url;
        IsCover = isCover;
        LogUtil.i16("ZLTextImageElement:" + URL + ":" + IsCover);
    }
}
