package me.shouheng.compress

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.IntRange
import android.text.TextUtils
import me.shouheng.compress.listener.CompressListener
import me.shouheng.compress.naming.CacheNameFactory
import me.shouheng.compress.naming.DefaultNameFactory
import me.shouheng.compress.strategy.*
import me.shouheng.compress.strategy.config.Config
import me.shouheng.compress.utils.CLog
import me.shouheng.compress.utils.getDefaultCacheDir

import java.io.File

/**
 * The Compress connector.
 *
 * @author Shouheng Wang
 * @version 2019-5-22
 */
class Compress private constructor(
    private val context: Context,
    private val imageSource: IImageSource<*>
) {
    private var format: Bitmap.CompressFormat       = Config.DEFAULT_COMPRESS_FORMAT
    private var quality: Int                        = Config.DEFAULT_COMPRESS_QUALITY
    private var targetDir: String?                  = null
    private var autoRecycle: Boolean                = Config.DEFAULT_BITMAP_RECYCLE
    private var cacheNameFactory: CacheNameFactory? = null
    private var compressListener: CompressListener? = null

    private val outFile: File
        get() {
            if (TextUtils.isEmpty(targetDir)) {
                val cacheDir = getDefaultCacheDir(context, Config.DEFAULT_CACHE_DIRECTORY_NAME)
                    ?: throw IllegalStateException("Cache directory is null, " +
                            "check your storage permission and try again.")
                targetDir = cacheDir.absolutePath
            }
            val pathname = targetDir + File.separator + (cacheNameFactory?:DefaultNameFactory).getFileName(format)
            CLog.d("The output file name was [$pathname].")
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

    @Deprecated("The auto-recycle is not necessary any more.",
        ReplaceWith("this.autoRecycle = autoRecycle")
    )
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

        private val typeAdapters = mutableMapOf<Class<*>, IImageSource.Adapter<*>>()

        init {
            registerTypeAdapter(Bitmap::class.java,     BitmapImageSource.Adapter())
            registerTypeAdapter(ByteArray::class.java,  ByteArrayImageSource.Adapter())
            registerTypeAdapter(Uri::class.java,        UriImageSource.Adapter())
            registerTypeAdapter(File::class.java,       FileImageSource.Adapter())
        }

        /** Set debug mode or not. */
        fun setDebug(debug: Boolean) {
            CLog.isDebug = debug
        }

        /** Get a compress instance with image source type of [ByteArray]. */
        fun with(context: Context, srcData: Any): Compress {
            val source = getImageSource(context, srcData.javaClass, srcData) ?: InvalidImageSource()
            return Compress(context, source)
        }

        /** Register type adapter. */
        fun <T> registerTypeAdapter(type: Class<T>, adapter: IImageSource.Adapter<T>) {
            typeAdapters[type] = adapter
        }

        private fun <T> getImageSource(context: Context, type: Class<T>, data: T): IImageSource<T>? {
            // Find by type itself.
            var adapter = typeAdapters[type] as? IImageSource.Adapter<T>
            if (adapter == null) {
                // Find by inheritance relationship.
                typeAdapters.keys.find {
                    it.isAssignableFrom(type)
                }?.let {
                    adapter = typeAdapters[it] as? IImageSource.Adapter<T>?
                }
            }
            if (adapter == null) {
                CLog.i("Failed to get adapter for source type: [$type].")
            } else {
                CLog.i("Hit adapter [${adapter!!.javaClass}] for type [$type].")
            }
            return adapter?.get(context, data)
        }
    }
}
