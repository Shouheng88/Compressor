package me.shouheng.compress.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface

import java.io.File
import java.io.IOException

object CImageUtils {

    /**
     * Return whether given bitmap is empty: null or no pixel.
     *
     * @param src the source bitmap
     * @return whether the bitmap is empty
     */
    fun isEmptyBitmap(src: Bitmap?): Boolean {
        return src == null || src.width == 0 || src.height == 0
    }

    /**
     * Need the image compress according to the file size and the least compress size.
     *
     * @param filePath file path
     * @param leastCompressSize least compress size
     * @return true if need to compress
     */
    fun needCompress(filePath: String, leastCompressSize: Int): Boolean {
        if (leastCompressSize > 0) {
            val source = File(filePath)
            return source.exists() && source.length() > leastCompressSize shl 10
        }
        return true
    }

    /**
     * Get angle from image attribute.
     *
     * @param file the image file
     * @return the angle of image
     */
    fun getImageAngle(file: File): Int {
        val exif: ExifInterface
        try {
            exif = ExifInterface(file.absolutePath)
            return when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)) {
                6 -> 90
                3 -> 180
                8 -> 270
                else -> 0
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return 0
    }

    /**
     * Rotate given bitmap and return the result.
     *
     * @param srcBitmap the source bitmap
     * @param angle the angle to rotate
     * @return the rotated bitmap
     */
    fun rotateBitmap(srcBitmap: Bitmap, angle: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        return Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.width, srcBitmap.height, matrix, true)
    }
}
