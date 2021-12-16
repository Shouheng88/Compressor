package me.shouheng.compress.suorce

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import me.shouheng.compress.utils.CFileUtils
import me.shouheng.compress.utils.CImageUtils
import me.shouheng.compress.utils.CLog
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream

/**
 * The image source.
 *
 * @Author Shouheng Wang
 * @Time 2021/7/24
 */
interface ImageSource<T> {

    /** Get the source image. */
    fun source(): SourceData<T>

    /** Get the source image size. */
    fun size(): Pair<Int, Int>

    /** Get origin image according to [options]. */
    fun bitmap(options: BitmapFactory.Options): SourceBitmap

    /** Rotate image if necessary. */
    fun rotation(src: Bitmap): Bitmap = src

    /** Should ignore the image according to ignore size [size]. */
    fun ignore(size: Int): Boolean = false

    /** Copy image to [dest] if ignored. */
    fun copyTo(dest: File): Boolean = true
}

/** Image source for [File]. */
class FileImageSource(private val file: File) : ImageSource<File> {

    override fun source(): SourceData<File> = FileSourceData(file)

    override fun size(): Pair<Int, Int> {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        options.inSampleSize = 1
        BitmapFactory.decodeFile(file.absolutePath, options)
        return Pair(options.outWidth, options.outHeight)
    }

    override fun bitmap(options: BitmapFactory.Options): SourceBitmap {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
        return SourceBitmap(bitmap, false)
    }

    override fun rotation(src: Bitmap): Bitmap {
        val orientation = CImageUtils.getImageAngle(file)
        if (orientation != 0) {
            return CImageUtils.rotateBitmap(src, orientation)
        }
        return src
    }

    override fun ignore(size: Int): Boolean {
        return size <= 0 || !file.exists() || file.length() < size shl 10
    }

    override fun copyTo(dest: File): Boolean {
        return try {
            CFileUtils.copyFile(FileInputStream(file), FileOutputStream(dest))
            true
        } catch (e: FileNotFoundException) {
            CLog.e("Error copying file : $e")
            false
        }
    }
}

/** Image source for [ByteArray]. */
class ByteArrayImageSource(private val bytes: ByteArray) : ImageSource<ByteArray> {

    override fun source(): SourceData<ByteArray> = ByteArraySourceData(bytes)

    override fun size(): Pair<Int, Int> {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        options.inSampleSize = 1
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        return Pair(options.outWidth, options.outHeight)
    }

    override fun bitmap(options: BitmapFactory.Options): SourceBitmap {
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        return SourceBitmap(bitmap, false)
    }
}

/** Image source for [Bitmap]. */
class BitmapImageSource(private val bitmap: Bitmap) : ImageSource<Bitmap> {

    override fun source(): SourceData<Bitmap> = BitmapSourceData(bitmap)

    override fun size(): Pair<Int, Int> = Pair(bitmap.width, bitmap.height)

    override fun bitmap(options: BitmapFactory.Options): SourceBitmap = SourceBitmap(bitmap, true)
}
