package me.shouheng.compress.strategy.compress

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.AsyncTask
import io.reactivex.Flowable
import kotlinx.coroutines.withContext
import me.shouheng.compress.AbstractStrategy
import me.shouheng.compress.strategy.config.Config
import me.shouheng.compress.strategy.config.ScaleMode
import me.shouheng.compress.utils.CImageUtils
import me.shouheng.compress.utils.CLog
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Callable
import kotlin.coroutines.CoroutineContext

/**
 * The concrete compress strategy.
 *
 * @author Shouheng Wang
 * @version 2019-05-17
 */
open class Compressor : AbstractStrategy() {

    private var maxWidth: Float             = Config.COMPRESSOR_DEFAULT_MAX_WIDTH
    private var maxHeight: Float            = Config.COMPRESSOR_DEFAULT_MAX_HEIGHT
    @ScaleMode private var scaleMode: Int   = Config.COMPRESSOR_DEFAULT_SCALE_MODE
    private var config: Bitmap.Config?      = null
    private var ignoreIfSmaller: Boolean    = true

    override fun getBitmap(): Bitmap? = compressByQuality()

    /*--------------------------------------- public methods ------------------------------------------*/

    /**
     * Set the max width of compressed image.
     *
     * @param maxWidth the max width in pixels.
     * @return the compressor instance.
     */
    fun setMaxWidth(maxWidth: Float): Compressor {
        this.maxWidth = maxWidth
        return this
    }

    /**
     * Set the max height of compressed image.
     *
     * @param maxHeight the max height in pixels.
     * @return the compressor instance.
     */
    fun setMaxHeight(maxHeight: Float): Compressor {
        this.maxHeight = maxHeight
        return this
    }

    /**
     * Set the scale mode when the destination image ratio differ from the original original.
     *
     * @param scaleMode the scale mode.
     * @return the compressor instance.
     * @see ScaleMode for details ab out this field
     */
    fun setScaleMode(@ScaleMode scaleMode: Int): Compressor {
        this.scaleMode = scaleMode
        return this
    }

    /**
     * Set the image configuration for bitmap: [android.graphics.Bitmap.Config].
     *
     * @param config the config
     * @return the compress instance
     * @see android.graphics.Bitmap.Config
     */
    fun setConfig(config: Bitmap.Config): Compressor {
        this.config = config
        return this
    }

    /**
     * Action when the current size is smaller than required size.
     * If the [ignoreIfSmaller] is true, it will ignore the request and return the origin bitmap.
     */
    fun setIgnoreIfSmaller(ignoreIfSmaller: Boolean): Compressor {
        this.ignoreIfSmaller = ignoreIfSmaller
        return this
    }

    override fun get(): File {
        try {
            notifyCompressStart()
            compressAndWrite()
            notifyCompressSuccess(outFile!!)
        } catch (e: Exception) {
            CLog.e(e.message)
            notifyCompressError(e)
        }
        return outFile!!
    }

    override suspend fun get(coroutineContext: CoroutineContext): File =
        withContext(coroutineContext) {
            return@withContext get()
        }

    override fun asFlowable(): Flowable<File> {
        return Flowable.defer(Callable {
            try {
                notifyCompressStart()
                val succeed = compressAndWrite()
                if (succeed) {
                    notifyCompressSuccess(outFile!!)
                } else {
                    notifyCompressError(Exception("Failed to compress image, either caused by OOM or other problems."))
                }
                return@Callable Flowable.just(outFile)
            } catch (e: Exception) {
                notifyCompressError(e)
                CLog.e(e.message)
                return@Callable Flowable.error<File>(e)
            }
        })
    }

    override fun launch() {
        AsyncTask.SERIAL_EXECUTOR.execute {
            try {
                notifyCompressStart()
                val succeed = compressAndWrite()
                if (succeed) {
                    notifyCompressSuccess(outFile!!)
                } else {
                    notifyCompressError(Exception("Failed to compress image, either caused by OOM or other problems."))
                }
            } catch (e: Exception) {
                notifyCompressError(e)
                CLog.e(e.message)
            }
        }
    }

    /*--------------------------------------- protected methods ------------------------------------------*/

    open fun calculateRequiredWidth(imgRatio: Float, reqRatio: Float): Int {
        var ratio = imgRatio
        when (scaleMode) {
            ScaleMode.SCALE_LARGER -> if (srcHeight > maxHeight || srcWidth > maxWidth) {
                // If Height is greater
                if (ratio < reqRatio) {
                    ratio = maxHeight / srcHeight
                    return (ratio * srcWidth).toInt()
                }  // If Width is greater
                else if (ratio > reqRatio) {
                    return maxWidth.toInt()
                }
            }
            ScaleMode.SCALE_SMALLER -> if (srcHeight > maxHeight || srcWidth > maxWidth) {
                // If Height is greater
                if (ratio < reqRatio) {
                    return maxWidth.toInt()
                }  // If Width is greater
                else if (ratio > reqRatio) {
                    ratio = maxHeight / srcHeight
                    return (ratio * srcWidth).toInt()
                }
            }
            ScaleMode.SCALE_HEIGHT -> return (srcWidth * maxHeight / srcHeight).toInt()
            ScaleMode.SCALE_WIDTH -> return maxWidth.toInt()
            else -> return maxWidth.toInt()
        }
        return maxWidth.toInt()
    }

    open fun calculateRequiredHeight(imgRatio: Float, reqRatio: Float): Int {
        var ratio = imgRatio
        when (scaleMode) {
            ScaleMode.SCALE_LARGER -> if (srcHeight > maxHeight || srcWidth > maxWidth) {
                // If Height is greater
                if (ratio < reqRatio) {
                    return maxHeight.toInt()
                }  // If Width is greater
                else if (ratio > reqRatio) {
                    ratio = maxWidth / srcWidth
                    return (ratio * srcHeight).toInt()
                }
            }
            ScaleMode.SCALE_SMALLER -> if (srcHeight > maxHeight || srcWidth > maxWidth) {
                // If Height is greater
                if (ratio < reqRatio) {
                    ratio = maxWidth / srcWidth
                    return (ratio * srcHeight).toInt()
                }  // If Width is greater
                else if (ratio > reqRatio) {
                    return maxHeight.toInt()
                }
            }
            ScaleMode.SCALE_HEIGHT -> return maxHeight.toInt()
            ScaleMode.SCALE_WIDTH -> {
                ratio = maxWidth / srcWidth
                return (srcHeight * ratio).toInt()
            }
            else -> return maxHeight.toInt()
        }
        return maxHeight.toInt()
    }

    open fun calculateInSampleSize(reqWidth: Int, reqHeight: Int): Int {
        var inSampleSize = 1
        if (srcHeight > reqHeight || srcWidth > reqWidth) {
            val halfHeight = srcHeight / 2
            val halfWidth = srcWidth / 2
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /*--------------------------------------- inner methods ------------------------------------------*/

    /** Compress bitmap and save it to target file. */
    private fun compressAndWrite(): Boolean {
        val bitmap = compressByScale()
        if (!CImageUtils.isEmptyBitmap(bitmap)) {
            val fos = FileOutputStream(outFile)
            bitmap!!.compress(format, quality, fos)
            fos.flush()
            fos.close()
        } else {
            return false
        }
        return true
    }

    /** Compress by quality. The bitmap will be compressed by scale first. */
    private fun compressByQuality(): Bitmap? {
        val bitmap = compressByScale()
        if (CImageUtils.isEmptyBitmap(bitmap)) return null
        val baos = ByteArrayOutputStream()
        bitmap!!.compress(format, quality, baos)
        val bytes = baos.toByteArray()
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    /** Compress the source bitmap by scale */
    private fun compressByScale(): Bitmap? {
        prepareImageSizeInfo()

        val imgRatio = srcWidth.toFloat() / srcHeight.toFloat()
        val reqRatio = maxWidth / maxHeight

        val reqWidth = calculateRequiredWidth(imgRatio, reqRatio)
        val reqHeight = calculateRequiredHeight(imgRatio, reqRatio)

        val origin = getOriginBitmap(reqWidth, reqHeight)

        // ignore if the origin size is smaller then required size
        if (ignoreIfSmaller && (reqWidth > srcWidth || reqWidth > srcHeight)) {
            return origin?.let {
                imageSource?.rotation(it)
            }
        }

        // failed to create scaled bitmap or get the origin bitmap, return null directly.
        if (origin == null) return null

        // scale the bitmap
        val ratioX = reqWidth / origin.width.toFloat()
        val ratioY = reqHeight / origin.height.toFloat()
        val matrix = Matrix().apply { setScale(ratioX, ratioY) }
        val result = Bitmap.createBitmap(origin, 0, 0, origin.width, origin.height, matrix, true)

        return imageSource?.rotation(result)
    }

    /** Get the origin bitmap based on the source bitmap type. */
    private fun getOriginBitmap(reqWidth: Int, reqHeight: Int): Bitmap? {
        val sourceData = imageSource!!.source()
        if (sourceData.isBitmap())
            return sourceData.data() as Bitmap
        val options = BitmapFactory.Options()
        options.inSampleSize = calculateInSampleSize(reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        options.inDither = false
        options.inPurgeable = true
        options.inInputShareable = true
        options.inTempStorage = ByteArray(16 * 1024)
        return imageSource!!.bitmap(options).bitmap
    }
}
