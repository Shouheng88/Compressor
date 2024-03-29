package me.shouheng.compress

import android.graphics.Bitmap
import android.os.Handler
import androidx.annotation.IntRange
import me.shouheng.compress.request.BitmapBuilder
import me.shouheng.compress.strategy.config.Config
import java.io.File

/** The abstract compress strategy. */
abstract class AbstractStrategy : RequestBuilder<File>(), Handler.Callback {

    protected var outFile: File?                = null
    protected var format: Bitmap.CompressFormat = Config.DEFAULT_COMPRESS_FORMAT
    protected var quality: Int                  = Config.DEFAULT_COMPRESS_QUALITY
    protected var autoRecycle: Boolean          = Config.DEFAULT_BITMAP_RECYCLE

    protected var srcWidth: Int                 = 0
    protected var srcHeight: Int                = 0

    /** Call this when you want to get the result as bitmap. */
    fun asBitmap(): BitmapBuilder {
        val builder = BitmapBuilder()
        builder.setAbstractStrategy(this)
        return builder
    }

    /** Prepare original image size info before calculate the image sample size.*/
    protected fun prepareImageSizeInfo() {
        imageSource?.getSize()?.let {
            this.srcWidth = it.width
            this.srcHeight = it.height
        }
    }

    override fun getBitmap(): Bitmap? {
        throw IllegalStateException("The #getBitmap() method is not implemented by your strategy.")
    }

    internal fun setFormat(format: Bitmap.CompressFormat) {
        this.format = format
    }

    internal fun setQuality(@IntRange(from = 0, to = 100) quality: Int) {
        this.quality = quality
    }

    internal fun setOutFile(outFile: File) {
        this.outFile = outFile
    }

    @Deprecated("The auto-recycle is not necessary any more.",
        ReplaceWith("this.autoRecycle = autoRecycle")
    )
    internal fun setAutoRecycle(autoRecycle: Boolean) {
        this.autoRecycle = autoRecycle
    }
}
