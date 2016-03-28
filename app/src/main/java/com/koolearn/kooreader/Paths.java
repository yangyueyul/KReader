package com.koolearn.kooreader;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Environment;

import com.koolearn.android.util.LogUtil;
import com.koolearn.klibrary.core.options.ZLStringListOption;
import com.koolearn.klibrary.core.options.ZLStringOption;
import com.koolearn.klibrary.core.util.SystemInfo;
import com.koolearn.klibrary.ui.android.library.ZLAndroidLibrary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class Paths {
    public static ZLStringListOption BookPathOption =
            pathOption("BooksDirectory", ZLAndroidLibrary.Instance().getExternalCacheDir()); // 会写到数据库中");

    public static ZLStringListOption FontPathOption =
            pathOption("FontPathOption", "/data/data/com.koolearn.klibrary.ui.android/files");

    public static String internalTempDirectoryValue(Context context) {
        String value = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            value = getExternalCacheDirPath(context);  // 外部缓存
        }
        return value != null ? value : (mainBookDirectory() + "/.KooReader");
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    private static String getExternalCacheDirPath(Context context) {
        final File d = context != null ? context.getExternalCacheDir() : null;
        if (d != null) {
            d.mkdirs();
            if (d.exists() && d.isDirectory()) {
                return d.getPath();
            }
        }
        return null;
    }

    public static ZLStringOption DownloadsDirectoryOption =
            new ZLStringOption("Files", "DownloadsDirectory", "");

    static {
        if ("".equals(DownloadsDirectoryOption.getValue())) {
            DownloadsDirectoryOption.setValue(mainBookDirectory());
        }
    }

    private static void addDirToList(List<String> list, String candidate) {
        if (candidate == null || !candidate.startsWith("/")) {
            return;
        }
        for (int count = 0; count < 5; ++count) {
            while (candidate.endsWith("/")) {
                candidate = candidate.substring(0, candidate.length() - 1);
            }
            final File f = new File(candidate);
            try {
                final String canonical = f.getCanonicalPath();
                if (canonical.equals(candidate)) {
                    break;
                }
                candidate = canonical;
            } catch (Throwable t) {
                return;
            }
        }
        while (candidate.endsWith("/")) {
            candidate = candidate.substring(0, candidate.length() - 1);
        }
        if (!"".equals(candidate) && !list.contains(candidate) && new File(candidate).canRead()) {
            list.add(candidate);
        }
    }

    public static List<String> allCardDirectories() {
        final List<String> dirs = new LinkedList<String>();
        dirs.add(cardDirectory());
        addDirToList(dirs, System.getenv("SECONDARY_STORAGE"));
        return dirs;
    }

    /**
     * 外部缓存路径
     *
     * @return
     */
    public static String cardDirectory() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return Environment.getExternalStorageDirectory().getPath();
        }

        final List<String> dirNames = new LinkedList<String>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/self/mounts"));
            String line;
            while ((line = reader.readLine()) != null) {
                final String[] parts = line.split("\\s+");
                if (parts.length >= 4 &&
                        parts[2].toLowerCase().indexOf("fat") >= 0 &&
                        parts[3].indexOf("rw") >= 0) {
                    final File fsDir = new File(parts[1]);
                    if (fsDir.isDirectory() && fsDir.canWrite()) {
                        dirNames.add(fsDir.getPath());
                    }
                }
            }
        } catch (Throwable e) {
        } finally {
            try {
                reader.close();
            } catch (Throwable t) {
            }
        }

        for (String dir : dirNames) {
            if (dir.toLowerCase().indexOf("media") > 0) {
                return dir;
            }
        }
        if (dirNames.size() > 0) {
            return dirNames.get(0);
        }

        return Environment.getExternalStorageDirectory().getPath();
    }

    private static String defaultBookDirectory() {
        return cardDirectory() + "/Books";
    }

    private static ZLStringListOption pathOption(String key, String defaultDirectory) {
        final ZLStringListOption option = new ZLStringListOption(
                "Files", key, Collections.<String>emptyList(), "\n"
        );
        LogUtil.i6("defaultDirectory:" + defaultDirectory);
        if (option.getValue().isEmpty()) {
            option.setValue(Collections.singletonList(defaultDirectory));
        }
        return option;
    }

    public static List<String> bookPath() {
        final List<String> path = new ArrayList<String>(BookPathOption.getValue());
        final String downloadsDirectory = DownloadsDirectoryOption.getValue();
        if (!"".equals(downloadsDirectory) && !path.contains(downloadsDirectory)) {
            path.add(downloadsDirectory);
        }
        return path;
    }

    public static String mainBookDirectory() {
        final List<String> bookPath = BookPathOption.getValue();
        return bookPath.isEmpty() ? defaultBookDirectory() : bookPath.get(0);
    }

    public static SystemInfo systemInfo(Context context) {

        final Context appContext = context.getApplicationContext();
        return new SystemInfo() {
            public String tempDirectory() {
                return internalTempDirectoryValue(appContext);
            }
        };
    }

    public static String systemShareDirectory() {
        return "/system/usr/share/KooReader";
    }
}