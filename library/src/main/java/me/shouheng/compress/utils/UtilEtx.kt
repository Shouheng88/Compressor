package me.shouheng.compress.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import java.io.*

/** Get image orientation. */
fun File.orientation(): Int {
    try {
        val exif = ExifInterface(absolutePath)
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

/** Copy file to destination. */
fun File.copyTo(file: File): Boolean {
    return try {
        val ins = FileInputStream(this)
        val ous = FileOutputStream(file)
        copy(ins, ous)
    } catch (e: IOException) {
        e.printStackTrace()
        false
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        false
    }
}

fun Uri.orientation(context: Context): Int {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        try {
            val exif = ExifInterface(context.contentResolver.openInputStream(this))
            return when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)) {
                6 -> 90
                3 -> 180
                8 -> 270
                else -> 0
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    return 0
}

/** Copy file of [Uri] to destination [file].  */
fun Uri.copyTo(context: Context, file: File): Boolean {
    return try {
        val ous = FileOutputStream(file)
        val ins = context.contentResolver.openInputStream(this) ?: return false
        copy(ins, ous)
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        false
    }
}

/** Decode bitmap from uri. */
fun Uri.decodeBitmap(context: Context, options: BitmapFactory.Options): Bitmap? {
    return try {
        val ins = context.contentResolver.openInputStream(this)
        BitmapFactory.decodeStream(ins, null, options)
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        null
    }
}

/** Return whether given bitmap is empty: null or no pixel. */
fun Bitmap?.isEmpty(): Boolean {
    return this == null || this.width == 0 || this.height == 0
}

fun Bitmap?.isNotEmpty(): Boolean {
    return !this.isEmpty()
}

/** Get bitmap size by bytes. */
fun Bitmap?.size(): Int {
    if (this == null) return 0
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        return allocationByteCount
    }
    return byteCount
}

/** Rotate bitmap by given angle. */
fun Bitmap.rotate(angle: Int): Bitmap {
    if (angle == 0) return this
    val matrix = Matrix()
    matrix.postRotate(angle.toFloat())
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

/** Save bitmap to [file]. */
fun Bitmap?.saveTo(file: File, format: Bitmap.CompressFormat, quality: Int): Boolean {
    return if (this.isNotEmpty()) {
        var ous: FileOutputStream? = null
        try {
            ous = FileOutputStream(file)
            this?.compress(format, quality, ous)
            ous.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } finally {
            ous?.close()
        }
        true
    } else {
        false
    }
}

/** Get the size of bitmap color config. */
fun Bitmap.Config?.size(): Int {
    return when(this) {
        Bitmap.Config.ALPHA_8   -> 1
        Bitmap.Config.ARGB_4444 -> 2
        Bitmap.Config.RGB_565   -> 2
        Bitmap.Config.ARGB_8888 -> 4
        else -> {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && this == Bitmap.Config.RGBA_F16) 8 else 4
        }
    }
}

/** Get pixel size of bitmap. */
fun BitmapFactory.Options.pixelSize(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) this.outConfig.size() else 4
}

/** Save byte array to file. */
fun ByteArray.saveTo(file: File): Boolean {
    var ous: FileOutputStream? = null
    return try {
        ous = FileOutputStream(file)
        ous.write(this)
        ous.flush()
        true
    } catch (e: IOException) {
        e.printStackTrace()
        false
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        false
    } finally {
        ous?.close()
    }
}

/** Copy from input stream to output stream. */
fun copy(ins: InputStream, os: OutputStream): Boolean {
    val data = ByteArray(1024)
    return try {
        var len: Int = ins.read(data)
        while (len > 0) {
            os.write(data, 0, len)
            len = ins.read(data)
        }
        true
    } catch (e: IOException) {
        CLog.e("Error copying file : $e")
        e.printStackTrace()
        false
    } finally {
        ins.close()
        os.close()
    }
}

/**
 * Get the default cache directory.
 *
 * @param context the context to get cache directory.
 * @param cacheName cache directory name.
 * @return the cache directory file.
 */
fun getDefaultCacheDir(context: Context, cacheName: String): File? {
    if (Environment.MEDIA_MOUNTED != Environment.getExternalStorageState()) {
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
    CLog.e("Default disk cache dir is null")
    return null
}
