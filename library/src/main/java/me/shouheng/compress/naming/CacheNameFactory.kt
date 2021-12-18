package me.shouheng.compress.naming

import android.graphics.Bitmap

/** The factory class used to get the name of compressed image file. */
interface CacheNameFactory {

    /** Get the file name of compressed image. */
    fun getFileName(format: Bitmap.CompressFormat): String

}
