package me.shouheng.compress

import android.graphics.Bitmap
import android.os.Handler
import me.shouheng.compress.request.BitmapBuilder
import me.shouheng.compress.strategy.config.Config
import java.io.File

/**
 * The abstract compress strategy.
 *
 * @author Shouheng Wang
 */
abstract class AbstractStrategy : RequestBuilder<File>(), Handler.Callback {

    protected var outFile: File? = null
    protected var format: Bitmap.CompressFormat = Config.DEFAULT_COMPRESS_FORMAT
    protected var quality: Int = Config.DEFAULT_COMPRESS_QUALITY
    protected var autoRecycle: Boolean = Config.DEFAULT_BITMAP_RECYCLE

    protected var srcWidth: Int = 0
    protected var srcHeight: Int = 0

    fun asBitmap(): BitmapBuilder {
        val builder = BitmapBuilder()
        builder.setAbstractStrategy(this)
        return builder
    }

    /** Prepare original image size info before calculate the image sample size.*/
    protected fun prepareImageSizeInfo() {
        imageSource ?: return
        val size = imageSource!!.size()
        this.srcWidth = size.first
        this.srcHeight = size.second
    }

    override fun getBitmap(): Bitmap? {
        throw IllegalStateException("This #getBitmap() method is not implemented by your strategy.")
    }

    internal fun setFormat(format: Bitmap.CompressFormat) {
        this.format = format
    }

    internal fun setQuality(quality: Int) {
        this.quality = quality
    }

    internal fun setOutFile(outFile: File) {
        this.outFile = outFile
    }

    /**
     * Whether the source bitmap should be recycled. The source bitmap means the birmap you used in
     * [Compress.with]. Since you may need to use the bitmap latter, so we
     * added this method for you to custom this action,
     *
     * @param autoRecycle whether the source bitmap should be recycled automatically
     */
    internal fun setAutoRecycle(autoRecycle: Boolean) {
        this.autoRecycle = autoRecycle
    }
}
