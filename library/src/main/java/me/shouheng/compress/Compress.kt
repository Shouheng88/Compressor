package me.shouheng.compress

import android.content.Context
import android.graphics.Bitmap
import android.support.annotation.IntRange
import android.text.TextUtils
import me.shouheng.compress.listener.CompressListener
import me.shouheng.compress.naming.CacheNameFactory
import me.shouheng.compress.naming.DefaultNameFactory
import me.shouheng.compress.strategy.config.Config
import me.shouheng.compress.utils.CFileUtils
import me.shouheng.compress.utils.CLog

import java.io.File

/**
 * The Compress connector.
 *
 * @author WngShhng (shouheng2015@gmail.com)
 * @version 2019-5-22
 */
class Compress private constructor(
    private val context: Context,
    private val srcFile: File?,
    private val srcBitmap: Bitmap?,
    private val srcData: ByteArray?
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
            val pathname = targetDir + File.separator + getCacheNameFactory().getFileName(format)
            CLog.d("The output file name was $pathname.")
            return File(pathname)
        }

    /**
     * Set the format of compressed image.
     *
     * @param format image format
     * @return       the compress instance
     */
    fun setFormat(format: Bitmap.CompressFormat): Compress {
        this.format = format
        return this
    }

    /**
     * Set the quality of compressed image, should be an integer between 0 and 100, aks [0, 100].
     *
     * @param quality the quality of compressed image
     * @return        the compress instance
     */
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
     * @return            the compress instance
     */
    fun setAutoRecycle(autoRecycle: Boolean): Compress {
        this.autoRecycle = autoRecycle
        return this
    }

    /**
     * The directory compressed image will be saved to.
     *
     * @param targetDir the target directory
     * @return          the compress instance
     */
    fun setTargetDir(targetDir: String): Compress {
        this.targetDir = targetDir
        return this
    }

    /**
     * The factory witch will used to provide the name of compressed file.
     *
     * @param cacheNameFactory the cache name factory
     * @return                 the compress instance
     */
    fun setCacheNameFactory(cacheNameFactory: CacheNameFactory): Compress {
        this.cacheNameFactory = cacheNameFactory
        return this
    }

    /**
     * Set the compress listener, you can get the compressed image and the progress.
     *
     * @param compressListener the listener
     * @return                 the compress instance
     */
    fun setCompressListener(compressListener: CompressListener): Compress {
        this.compressListener = compressListener
        return this
    }

    /**
     * Set the strategy used to compress the image. This method is often the last one for basic
     * options, other options are provided by each strategy.
     *
     * @param t   the strategy instance, use [me.shouheng.compress.strategy.Strategies]
     *            to get all provided strategies.
     * @param <T> the strategy type.
     * @return    the strategy instance.
     *
     * @see AbstractStrategy
     */
    fun <T : AbstractStrategy> strategy(t: T): T {
        t.setSrcFile(srcFile)
        t.setSrcBitmap(srcBitmap)
        t.setSrcData(srcData)
        t.setFormat(format)
        t.setQuality(quality)
        t.setAutoRecycle(autoRecycle)
        t.setOutFile(outFile)
        t.setCompressListener(compressListener)
        return t
    }

    private fun getCacheNameFactory(): CacheNameFactory {
        return cacheNameFactory?:DefaultNameFactory.get()
    }

    companion object {

        /**
         * Get a compress instance with image source type [File]
         *
         * @param context context
         * @param file    image source
         * @return        the compress instance
         */
        fun with(context: Context, file: File): Compress {
            return Compress(context, file, null, null)
        }

        /**
         * Get a compress instance with image source type [Bitmap]
         *
         * @param context   context
         * @param srcBitmap image source
         * @return          the compress instance
         */
        fun with(context: Context, srcBitmap: Bitmap): Compress {
            return Compress(context, null, srcBitmap, null)
        }

        /**
         * Get a compress instance with image source type of [ByteArray]
         *
         * @param context context
         * @param srcData image source
         * @return        the compress instance
         */
        fun with(context: Context, srcData: ByteArray): Compress {
            return Compress(context, null, null, srcData)
        }
    }
}
