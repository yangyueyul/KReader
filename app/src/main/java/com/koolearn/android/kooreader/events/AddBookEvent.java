package com.koolearn.android.kooreader.events;

/**
 * Created by yangyue on 2016/4/11.
 */
public class AddBookEvent {
    public final String bookPath;

    public AddBookEvent(String bookPath) {
        this.bookPath = bookPath;
    }
}