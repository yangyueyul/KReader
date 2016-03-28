package com.koolearn.klibrary.core.library;

import com.koolearn.klibrary.core.filesystem.ZLResourceFile;
import com.koolearn.klibrary.core.options.ZLIntegerOption;
import com.koolearn.klibrary.core.options.ZLStringOption;

import java.util.List;

// 工具类 版本号、屏幕大小、dp等
public abstract class ZLibrary {
    public static ZLibrary Instance() {
        return ourImplementation;
    }

    private static ZLibrary ourImplementation;

    public static final String SCREEN_ORIENTATION_SYSTEM = "system";
    public static final String SCREEN_ORIENTATION_SENSOR = "sensor";
    public static final String SCREEN_ORIENTATION_PORTRAIT = "portrait";
    public static final String SCREEN_ORIENTATION_LANDSCAPE = "landscape";
    public static final String SCREEN_ORIENTATION_REVERSE_PORTRAIT = "reversePortrait";
    public static final String SCREEN_ORIENTATION_REVERSE_LANDSCAPE = "reverseLandscape";

    public final ZLIntegerOption ScreenHintStageOption =
            new ZLIntegerOption("LookNFeel", "ScreenHintStage", 0);

    public final ZLStringOption getOrientationOption() {
        return new ZLStringOption("LookNFeel", "Orientation", "system");
    }

    protected ZLibrary() {
        ourImplementation = this;
    }

    abstract public ZLResourceFile createResourceFile(String path);

    abstract public ZLResourceFile createResourceFile(ZLResourceFile parent, String name);

    abstract public String getVersionName();

    abstract public String getFullVersionName();

    abstract public String getCurrentTimeString();

    abstract public int getDisplayDPI();

    abstract public float getDPI();

    abstract public float getSP();

    abstract public int getWidthInPixels();

    abstract public int getScreenWidth();

    abstract public int getScreenHeight();

    abstract public int getHeightInPixels();

    abstract public String getExternalCacheDir();

    abstract public List<String> defaultLanguageCodes();

    abstract public boolean supportsAllOrientations();

    public String[] allOrientations() {
        return supportsAllOrientations()
                ? new String[]{
                SCREEN_ORIENTATION_SYSTEM,
                SCREEN_ORIENTATION_SENSOR,
                SCREEN_ORIENTATION_PORTRAIT,
                SCREEN_ORIENTATION_LANDSCAPE,
                SCREEN_ORIENTATION_REVERSE_PORTRAIT,
                SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        }
                : new String[]{
                SCREEN_ORIENTATION_SYSTEM,
                SCREEN_ORIENTATION_SENSOR,
                SCREEN_ORIENTATION_PORTRAIT,
                SCREEN_ORIENTATION_LANDSCAPE
        };
    }
}
