package me.shouheng.compress.utils;

import android.util.Log;

public class LogUtils {

    private static final String TAG = "EasyCompressor";

    private static boolean debug;

    private LogUtils() {
        throw new IllegalStateException("LogUtils can't be constructed by this!");
    }

    public static boolean isDebug() {
        return debug;
    }

    public static void setDebug(boolean debug) {
        LogUtils.debug = debug;
    }

    public static void d(String message) {
        if (debug) {
            Log.d(TAG, message);
        }
    }

    public static void w(String message) {
        if (debug) {
            Log.w(TAG, message);
        }
    }

    public static void e(String message) {
        if (debug) {
            Log.e(TAG, message);
        }
    }

}
