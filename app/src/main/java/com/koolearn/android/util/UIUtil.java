package com.koolearn.android.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.koolearn.klibrary.core.application.ZLApplication;
import com.koolearn.klibrary.core.resources.ZLResource;
import com.kooreader.util.Pair;

import java.util.LinkedList;
import java.util.Queue;

public abstract class UIUtil {
    private static final Object ourMonitor = new Object();
    private static ProgressDialog ourProgress;
    private static final Queue<Pair<Runnable, String>> ourTaskQueue = new LinkedList<Pair<Runnable, String>>();
    private static volatile Handler ourProgressHandler;

    private static boolean init() {
        if (ourProgressHandler != null) {
            return true;
        }
        try {
            ourProgressHandler = new Handler() {
                public void handleMessage(Message message) {
                    try {
                        synchronized (ourMonitor) {
                            if (ourTaskQueue.isEmpty()) {
                                ourProgress.dismiss();
                                ourProgress = null;
                            } else {
                                ourProgress.setMessage(ourTaskQueue.peek().Second);
                            }
                            ourMonitor.notify();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        ourProgress = null;
                    }
                }
            };
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    public static void wait(String key, String param, Runnable action, Context context) {
        waitInternal(getWaitMessage(key).replace("%s", param), action, context);
    }

    public static void wait(String key, Runnable action, Context context) {
        waitInternal(getWaitMessage(key), action, context);
    }

    private static String getWaitMessage(String key) {
        return ZLResource.resource("dialog").getResource("waitMessage").getResource(key).getValue();
    }

    private static void waitInternal(String message, Runnable action, Context context) {
        if (!init()) {
            action.run();
            return;
        }

        synchronized (ourMonitor) {
            ourTaskQueue.offer(new Pair(action, message));
            if (ourProgress == null) {
                ourProgress = ProgressDialog.show(context, null, message, true, false);
            } else {
                return;
            }
        }
        final ProgressDialog currentProgress = ourProgress;
        new Thread(new Runnable() {
            public void run() {
                while (ourProgress == currentProgress && !ourTaskQueue.isEmpty()) {
                    final Pair<Runnable, String> p = ourTaskQueue.poll();
                    p.First.run();
                    synchronized (ourMonitor) {
                        ourProgressHandler.sendEmptyMessage(0);
                        try {
                            ourMonitor.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
        }).start();
    }

    public static ZLApplication.SynchronousExecutor createExecutor(final Activity activity, final String key) {
        return new ZLApplication.SynchronousExecutor() {
            private final ZLResource myResource = ZLResource.resource("dialog").getResource("waitMessage");
            private final String myMessage = myResource.getResource(key).getValue();
            private volatile ProgressDialog myProgress; // 图书加载中

            public void execute(final Runnable action, final Runnable uiPostAction) {
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        myProgress = ProgressDialog.show(activity, null, myMessage, true, false);
                        final Thread runner = new Thread() {
                            public void run() {
                                action.run();
                                activity.runOnUiThread(new Runnable() {
                                    public void run() {
                                        try {
                                            myProgress.dismiss();
                                            myProgress = null;
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        if (uiPostAction != null) {
                                            uiPostAction.run();
                                        }
                                    }
                                });
                            }
                        };
                        runner.setPriority(Thread.MAX_PRIORITY);
                        runner.start();
                    }
                });
            }

            private void setMessage(final ProgressDialog progress, final String message) {
                if (progress == null) {
                    return;
                }
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        progress.setMessage(message);
                    }
                });
            }

            public void executeAux(String key, Runnable runnable) {
                setMessage(myProgress, myResource.getResource(key).getValue());
                runnable.run();
                setMessage(myProgress, myMessage);
            }
        };
    }
}
