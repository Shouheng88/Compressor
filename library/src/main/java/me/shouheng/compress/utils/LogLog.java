package me.shouheng.compress.utils;

import android.util.Log;

/**
 * Simple log utils
 *
 * @author WngShhng (shouheng2015@gmail.com)
 */
public class LogLog {

    private static String TAG = "EasyCompressor";

    private static boolean debug;

    private LogLog() {
        throw new UnsupportedOperationException("u can't initialize me");
    }

    /**
     * Is log in debug mode
     *
     * @return is debug mode
     */
    public static boolean isDebug() {
        return debug;
    }

    /**
     * Set the log debug mode
     *
     * @param debug debug mode
     */
    public static void setDebug(boolean debug) {
        LogLog.debug = debug;
    }

    /**
     * Get the tag of this utils
     *
     * @return the tag
     */
    public static String getTAG() {
        return TAG;
    }

    /**
     * Set tag of the utils
     *
     * @param TAG the tag
     */
    public static void setTAG(String TAG) {
        LogLog.TAG = TAG;
    }

    public static void d(String message) {
        if (debug) {
            Log.d(TAG, message);
        }
    }

    public static void i(String message) {
        if (debug) {
            Log.i(TAG, message);
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
