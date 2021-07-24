package me.shouheng.compress.utils

import android.content.Context
import android.os.Environment
import android.util.Log

import java.io.*

object CFileUtils {

    private const val TAG = "CFileUtils"

    private val isStorageWritable: Boolean
        get() = Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()

    /**
     * Get the default cache directory.
     *
     * @param context the context to get cache directory.
     * @param cacheName cache directory name.
     * @return the cache directory file.
     */
    fun getDefaultCacheDir(context: Context, cacheName: String): File? {
        if (!isStorageWritable) {
            return null
        }
        val cacheDir = context.externalCacheDir
        if (cacheDir != null) {
            val result = File(cacheDir, cacheName)
            return if (!result.mkdirs() && (!result.exists() || !result.isDirectory)) {
                // File wasn't able to create a directory, or the result exists but not a directory
                null
            } else result
        }
        if (Log.isLoggable(TAG, Log.ERROR)) {
            CLog.e("default disk cache dir is null")
        }
        return null
    }

    /**
     * Copy file from source to destination.
     *
     * @param source the source file.
     * @param destination the destination to copy to.
     * @return is copy succeed.
     */
    fun copyFile(source: File, destination: File): Boolean {
        return try {
            copyFile(FileInputStream(source), FileOutputStream(destination))
        } catch (e: FileNotFoundException) {
            CLog.e("Error copying file : $e")
            false
        }
    }

    /**
     * Copy from the original to destination, based on the input and output stream.
     *
     * @param ins the input stream.
     * @param os the output stream.
     * @return is copy succeed.
     */
    fun copyFile(ins: InputStream, os: OutputStream): Boolean {
        var res = false
        val data = ByteArray(1024)
        try {
            var len: Int = ins.read(data)
            while (len > 0) {
                os.write(data, 0, len)
                len = ins.read(data)
            }
            ins.close()
            os.close()
            res = true
        } catch (e: IOException) {
            CLog.e("Error copying file : $e")
        }
        return res
    }
}
