package me.shouheng.compress

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import me.shouheng.compress.request.BitmapBuilder
import me.shouheng.compress.strategy.config.Config
import java.io.File

/**
 * The abstract compress strategy.
 * Extend it and implement the abstract methods to add your own compress strategy.
 *
 * @author WngShhng
 */
abstract class AbstractStrategy : RequestBuilder<File>(), Handler.Callback {

    protected var srcFile: File? = null
    protected var srcBitmap: Bitmap? = null
    protected var srcData: ByteArray? = null
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

    /*------------------------------------------- protected level -------------------------------------------*/

    /** Prepare original image size info before calculate the image sample size.*/
    protected fun prepareImageSizeInfo() {
        if (srcBitmap != null) {
            this.srcWidth = srcBitmap!!.width
            this.srcHeight = srcBitmap!!.height
            return
        }
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        options.inSampleSize = 1
        if (srcData != null) {
            BitmapFactory.decodeByteArray(srcData, 0, srcData!!.size, options)
        } else if (srcFile != null) {
            BitmapFactory.decodeFile(srcFile!!.absolutePath, options)
        }
        this.srcWidth = options.outWidth
        this.srcHeight = options.outHeight
    }

    override fun getBitmap(): Bitmap? {
        throw IllegalStateException("This #getBitmap() method is not implemented by your strategy.")
    }

    /*------------------------------------------- package level -------------------------------------------*/

    /* package */ internal fun setSrcFile(srcFile: File?) {
        this.srcFile = srcFile
    }

    /* package */ internal fun setFormat(format: Bitmap.CompressFormat) {
        this.format = format
    }

    /* package */ internal fun setQuality(quality: Int) {
        this.quality = quality
    }

    /* package */ internal fun setOutFile(outFile: File) {
        this.outFile = outFile
    }

    /* package */ internal fun setSrcBitmap(srcBitmap: Bitmap?) {
        this.srcBitmap = srcBitmap
    }

    /* package */ internal fun setSrcData(srcData: ByteArray?) {
        this.srcData = srcData
    }

    /**
     * Whether the source bitmap should be recycled. The source bitmap means the birmap you used in
     * [Compress.with]. Since you may need to use the bitmap latter, so we
     * added this method for you to custom this action,
     *
     * @param autoRecycle whether the source bitmap should be recycled automatically
     */
    /* package */ internal fun setAutoRecycle(autoRecycle: Boolean) {
        this.autoRecycle = autoRecycle
    }
}
