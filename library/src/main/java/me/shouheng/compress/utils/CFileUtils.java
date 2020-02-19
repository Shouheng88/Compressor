package me.shouheng.compress.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.*;

public final class FileUtils {

    private static final String TAG = "FileUtils";

    private FileUtils() {
        throw new UnsupportedOperationException("u can't initialize me");
    }

    private static boolean isStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * Get the default cache directory.
     *
     * @param context the context to get cache directory.
     * @param cacheName cache directory name.
     * @return the cache directory file.
     */
    public static File getDefaultCacheDir(Context context, String cacheName) {
        if (!isStorageWritable()) {
            return null;
        }

        File cacheDir = context.getExternalCacheDir();
        if (cacheDir != null) {
            File result = new File(cacheDir, cacheName);
            if (!result.mkdirs() && (!result.exists() || !result.isDirectory())) {
                // File wasn't able to create a directory, or the result exists but not a directory
                return null;
            }
            return result;
        }
        if (Log.isLoggable(TAG, Log.ERROR)) {
            LogLog.e("default disk cache dir is null");
        }
        return null;
    }

    /**
     * Copy file from source to destination.
     *
     * @param source the source file.
     * @param destination the destination to copy to.
     * @return is copy succeed.
     */
    public static boolean copyFile(File source, File destination) {
        try {
            return copyFile(new FileInputStream(source), new FileOutputStream(destination));
        } catch (FileNotFoundException e) {
            LogLog.e("Error copying file : " + e);
            return false;
        }
    }

    /**
     * Copy from the original to destination, based on the input and output stream.
     *
     * @param is the input stream.
     * @param os the output stream.
     * @return is copy succeed.
     */
    public static boolean copyFile(InputStream is, OutputStream os) {
        boolean res = false;
        byte[] data = new byte[1024];
        int len;
        try {
            while ((len = is.read(data)) > 0) {
                os.write(data, 0, len);
            }
            is.close();
            os.close();
            res = true;
        } catch (IOException e) {
            LogLog.e("Error copying file : " + e);
        }
        return res;
    }

}
