package me.shouheng.compress.naming

import android.graphics.Bitmap

/**
 * The factory class used to get the name of compressed image file.
 */
interface CacheNameFactory {

    /**
     * Get the file name of compressed image
     *
     * @param format the desired image format, use this to get the file name suffix
     * @return       the file name with suffix
     */
    fun getFileName(format: Bitmap.CompressFormat): String

}
