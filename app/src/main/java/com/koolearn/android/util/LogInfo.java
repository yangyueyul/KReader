package com.koolearn.android.util;

/**
 * ******************************************
 * 作    者 ：  杨越
 * 版    本 ：  1.0
 * 创建日期 ：  2016/1/27
 * 描    述 ：
 * 修订历史 ：
 * ******************************************
 */
public class LogInfo {
    static boolean flag = false;

    public static void I(String info) {
        if (flag) {
            final StackTraceElement[] stack = new Throwable().getStackTrace();
            final int i = 1;
//            for (int id = 0; id < stack.length; id++) {
            final StackTraceElement ste = stack[i];
            android.util.Log.i("yul1_log_", String.format("[%s][%s]%s[%s]",
                    ste.getFileName(), ste.getMethodName(), ste.getLineNumber(), info));
//            }
        }
    }

    public static void I1(String info) {
        if (flag) {
            final StackTraceElement[] stack = new Throwable().getStackTrace();
            final int i = 1;
//            for (int id = 0; id < stack.length; id++) {
            final StackTraceElement ste = stack[i];
            android.util.Log.i("yul2_log_", String.format("[%s][%s]%s[%s]",
                    ste.getFileName(), ste.getMethodName(), ste.getLineNumber(), info));
//            }
        }
    }

    public static void I3(String info) {
        if (flag) {
            final StackTraceElement[] stack = new Throwable().getStackTrace();
            final int i = 1;
//            for (int id = 0; id < stack.length; id++) {
            final StackTraceElement ste = stack[i];
            android.util.Log.i("yul2_log_", String.format("[%s][%s]%s[%s]",
                    ste.getFileName(), ste.getMethodName(), ste.getLineNumber(), info));
//            }
        }
    }

    public static void i(String info) {
        if (flag) {
            final StackTraceElement[] stack = new Throwable().getStackTrace();
            final int i = 1;
//            for (int id = 0; id < stack.length; id++) {
            final StackTraceElement ste = stack[i];
            android.util.Log.i("yul2_log_", String.format("[%s][%s]%s[%s]",
                    ste.getFileName(), ste.getMethodName(), ste.getLineNumber(), info));
//            }
        }
    }
}
