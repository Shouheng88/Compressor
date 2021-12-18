package me.shouheng.compress.strategy

import android.content.Context
import android.graphics.*
import android.net.Uri
import me.shouheng.compress.utils.*
import java.io.File
import me.shouheng.compress.Compress

/** Image size. */
class Size(val width: Int, val height: Int)

/** The image source wrapper. */
interface IImageSource<T> {

    /** Get the source image sizes. */
    fun getSize(): Size

    /** Get origin image bitmap by [options] provided. */
    fun getOriginBitmapByOptions(options: BitmapFactory.Options): Bitmap?

    /** Get the rotation ratios for the image source. */
    fun getRotation(): Int

    /** Should ignore the image according to ignore size [ignoreSize]. */
    fun shouldIgnoreForSize(ignoreSize: Int): Boolean

    /** Copy image to [dest] if ignored. */
    fun copyTo(dest: File, format: Bitmap.CompressFormat, quality: Int): Boolean

    /**
     * The image adapter from raw data to [IImageSource]. Register your adapter by
     * implement this interface and calling [Compress.registerTypeAdapter] method.
     */
    interface Adapter<T> {
        fun get(context: Context, source: T): IImageSource<T>
    }
}

abstract class AbsImageSource<T>(
    private val source: T
): IImageSource<T>

/** The invalid image source. */
class InvalidImageSource: IImageSource<Any> {

    override fun getSize(): Size = Size(0, 0)

    override fun getOriginBitmapByOptions(options: BitmapFactory.Options): Bitmap? = null

    override fun getRotation(): Int = 0

    override fun shouldIgnoreForSize(ignoreSize: Int): Boolean = true

    override fun copyTo(dest: File, format: Bitmap.CompressFormat, quality: Int): Boolean = false
}

/** Image source for uri data type. */
class UriImageSource(
    private val uri: Uri,
    private val context: Context
): AbsImageSource<Uri>(uri) {

    override fun getSize(): Size {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        options.inSampleSize = 1
        uri.decodeBitmap(context, options)
        return Size(options.outWidth, options.outHeight)
    }

    override fun getOriginBitmapByOptions(
        options: BitmapFactory.Options
    ): Bitmap? = uri.decodeBitmap(context, options)

    override fun shouldIgnoreForSize(ignoreSize: Int): Boolean {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        options.inSampleSize = 1
        uri.decodeBitmap(context, options)
        val size = options.outWidth*options.outHeight*options.pixelSize()
        return size < ignoreSize shl 10
    }

    override fun getRotation(): Int = uri.orientation(context)

    override fun copyTo(
        dest: File,
        format: Bitmap.CompressFormat,
        quality: Int
    ): Boolean = uri.copyTo(context, dest)

    class Adapter: IImageSource.Adapter<Uri> {
        override fun get(
            context: Context, source: Uri
        ): IImageSource<Uri> = UriImageSource(source, context)
    }
}

/** Image source for [File]. */
class FileImageSource(val file: File): AbsImageSource<File>(file) {

    override fun getSize(): Size {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        options.inSampleSize = 1
        BitmapFactory.decodeFile(file.absolutePath, options)
        return Size(options.outWidth, options.outHeight)
    }

    override fun getOriginBitmapByOptions(
        options: BitmapFactory.Options
    ): Bitmap = BitmapFactory.decodeFile(file.absolutePath, options)

    override fun getRotation() = file.orientation()

    override fun shouldIgnoreForSize(ignoreSize: Int): Boolean =
        ignoreSize <= 0 || !file.exists() || file.length() < ignoreSize shl 10

    override fun copyTo(
        dest: File,
        format: Bitmap.CompressFormat,
        quality: Int
    ): Boolean = file.copyTo(dest)

    class Adapter: IImageSource.Adapter<File> {
        override fun get(
            context: Context, source: File
        ): IImageSource<File> = FileImageSource(source)
    }
}

/** Image source for [ByteArray]. */
class ByteArrayImageSource(private val bytes: ByteArray): AbsImageSource<ByteArray>(bytes) {

    override fun getSize(): Size {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        options.inSampleSize = 1
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        return Size(options.outWidth, options.outHeight)
    }

    override fun getOriginBitmapByOptions(
        options: BitmapFactory.Options
    ): Bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

    override fun shouldIgnoreForSize(ignoreSize: Int): Boolean = bytes.size < ignoreSize shl 10

    override fun getRotation(): Int = 0

    override fun copyTo(
        dest: File,
        format: Bitmap.CompressFormat,
        quality: Int
    ): Boolean = bytes.saveTo(dest)

    class Adapter: IImageSource.Adapter<ByteArray> {
        override fun get(
            context: Context, source: ByteArray
        ): IImageSource<ByteArray> = ByteArrayImageSource(source)
    }
}

/** Image source for [Bitmap]. */
class BitmapImageSource(private val bitmap: Bitmap): AbsImageSource<Bitmap>(bitmap) {

    override fun getSize(): Size = Size(bitmap.width, bitmap.height)

    override fun getOriginBitmapByOptions(options: BitmapFactory.Options): Bitmap {
        // Scale bitmap according to inSampleSize
        val reqWidth = (bitmap.width.toFloat() / options.inSampleSize).toInt()
        val reqHeight = (bitmap.height.toFloat() / options.inSampleSize).toInt()
        val result = Bitmap.createBitmap(reqWidth, reqHeight, Bitmap.Config.ARGB_8888)
        val ratioX = reqWidth.toFloat() / bitmap.width
        val ratioY = reqHeight.toFloat() / bitmap.height
        val middleX = reqWidth / 2.0f
        val middleY = reqHeight / 2.0f
        val scaleMatrix = Matrix()
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)
        val canvas = Canvas(result)
        canvas.matrix = scaleMatrix
        canvas.drawBitmap(bitmap
            , middleX - bitmap.width / 2
            , middleY - bitmap.height / 2
            , Paint(Paint.FILTER_BITMAP_FLAG)
        )
        return result
    }

    override fun shouldIgnoreForSize(ignoreSize: Int): Boolean =
        ignoreSize <= 0 || bitmap.isEmpty() || bitmap.size() < ignoreSize shl 10

    override fun getRotation(): Int = 0

    override fun copyTo(
        dest: File,
        format: Bitmap.CompressFormat,
        quality: Int
    ): Boolean = bitmap.saveTo(dest, format, quality)

    class Adapter: IImageSource.Adapter<Bitmap> {
        override fun get(
            context: Context, source: Bitmap
        ): IImageSource<Bitmap> = BitmapImageSource(source)
    }
}
