package me.shouheng.compress

import android.content.Context
import android.graphics.Bitmap
import android.support.annotation.IntRange
import android.text.TextUtils
import me.shouheng.compress.listener.CompressListener
import me.shouheng.compress.naming.CacheNameFactory
import me.shouheng.compress.naming.DefaultNameFactory
import me.shouheng.compress.strategy.config.Config
import me.shouheng.compress.suorce.BitmapImageSource
import me.shouheng.compress.suorce.ByteArrayImageSource
import me.shouheng.compress.suorce.FileImageSource
import me.shouheng.compress.suorce.ImageSource
import me.shouheng.compress.utils.CFileUtils
import me.shouheng.compress.utils.CLog

import java.io.File

/**
 * The Compress connector.
 *
 * @author Shouheng Wang
 * @version 2019-5-22
 */
class Compress private constructor(
    private val context: Context,
    private val imageSource: ImageSource<*>
) {
    private var format: Bitmap.CompressFormat = Config.DEFAULT_COMPRESS_FORMAT
    private var quality: Int = Config.DEFAULT_COMPRESS_QUALITY
    private var targetDir: String? = null
    private var autoRecycle: Boolean = Config.DEFAULT_BITMAP_RECYCLE
    private var cacheNameFactory: CacheNameFactory? = null
    private var compressListener: CompressListener? = null

    private val outFile: File
        get() {
            if (TextUtils.isEmpty(targetDir)) {
                val cacheDir = CFileUtils.getDefaultCacheDir(context, Config.DEFAULT_CACHE_DIRECTORY_NAME)
                    ?: throw IllegalStateException("Cache directory is null, check your storage permission and try again.")
                targetDir = cacheDir.absolutePath
            }
            val pathname = targetDir + File.separator + (cacheNameFactory?:DefaultNameFactory).getFileName(format)
            CLog.d("The output file name was $pathname.")
            return File(pathname)
        }

    /** Set the format of compressed image. */
    fun setFormat(format: Bitmap.CompressFormat): Compress {
        this.format = format
        return this
    }

    /** Set the quality of compressed image. */
    fun setQuality(@IntRange(from = 0, to = 100) quality: Int): Compress {
        this.quality = quality
        return this
    }

    /**
     * Whether the source bitmap should be recycled. The source bitmap means the bitmap you used in
     * [Compress.with]. Since you may need to use the bitmap latter, so we
     * added this method for you to custom this action,
     * Default values is [Config.DEFAULT_BITMAP_RECYCLE].
     *
     * @param autoRecycle whether the source bitmap should be recycled automatically
     * @return the compress instance
     */
    fun setAutoRecycle(autoRecycle: Boolean): Compress {
        this.autoRecycle = autoRecycle
        return this
    }

    /** Directory the compressed image will be saved to. */
    fun setTargetDir(targetDir: String): Compress {
        this.targetDir = targetDir
        return this
    }

    /** The factory witch will used to provide the name of compressed file. */
    fun setCacheNameFactory(cacheNameFactory: CacheNameFactory): Compress {
        this.cacheNameFactory = cacheNameFactory
        return this
    }

    /** Set the compress listener, you can get the compressed image and the progress. */
    fun setCompressListener(compressListener: CompressListener): Compress {
        this.compressListener = compressListener
        return this
    }

    /**
     * Set the strategy used to compress the image. This method is often the last one for basic
     * options, other options are provided by each strategy.
     *
     * @param strategy the strategy instance, use [me.shouheng.compress.strategy.Strategies].
     * @see AbstractStrategy
     */
    fun <T : AbstractStrategy> strategy(strategy: T): T {
        return strategy.apply {
            setImageSource(imageSource)
            setFormat(format)
            setQuality(quality)
            setAutoRecycle(autoRecycle)
            setOutFile(outFile)
            setCompressListener(compressListener)
        }
    }

    companion object {

        /** Get a compress instance with image source type [File]. */
        fun with(context: Context, file: File): Compress {
            return Compress(context, FileImageSource(file))
        }

        /** Get a compress instance with image source type [Bitmap]. */
        fun with(context: Context, srcBitmap: Bitmap): Compress {
            return Compress(context, BitmapImageSource(srcBitmap))
        }

        /** Get a compress instance with image source type of [ByteArray]. */
        fun with(context: Context, srcData: ByteArray): Compress {
            return Compress(context, ByteArrayImageSource(srcData))
        }
    }
}
