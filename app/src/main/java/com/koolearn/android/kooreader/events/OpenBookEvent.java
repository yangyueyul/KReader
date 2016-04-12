package com.koolearn.android.kooreader.events;

/**
 * Created by yangyue on 2016/4/11.
 */
public class OpenBookEvent {
    public final String bookPath;

    public OpenBookEvent(String bookPath) {
        this.bookPath = bookPath;
    }
}