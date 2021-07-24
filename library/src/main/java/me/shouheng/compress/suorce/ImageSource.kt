package me.shouheng.compress.suorce

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

/**
 * The image source.
 *
 * @Author wangshouheng
 * @Time 2021/7/24
 */
interface ImageSource<T> {

    fun image(): T

    fun size(): Pair<Int, Int>

    fun origin(options: BitmapFactory.Options): Bitmap

    fun needCompress(): Boolean

    fun rotation()
}

class FileImageSource(val file: File) : ImageSource<File> {

    override fun image(): File = file

    override fun size(): Pair<Int, Int> {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        options.inSampleSize = 1
        BitmapFactory.decodeFile(file.absolutePath, options)
        return Pair(options.outWidth, options.outHeight)
    }

    override fun origin(options: BitmapFactory.Options): Bitmap {
    }
}

class ByteArrayImageSource(val byteArray: ByteArray) : ImageSource<ByteArray> {

    override fun image(): ByteArray = byteArray

    override fun size(): Pair<Int, Int> {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        options.inSampleSize = 1
        BitmapFactory.decodeByteArray(byteArray, 0, srcData!!.size, options)
        return Pair(options.outWidth, options.outHeight)
    }

    override fun origin(options: BitmapFactory.Options): Bitmap {
    }
}

class BitmapImageSource(val bitmap: Bitmap) : ImageSource<Bitmap> {

    override fun image(): Bitmap = bitmap

    override fun size(): Pair<Int, Int> {
        return Pair(bitmap.width, bitmap.height)
    }

    override fun origin(options: BitmapFactory.Options): Bitmap {
    }
}
