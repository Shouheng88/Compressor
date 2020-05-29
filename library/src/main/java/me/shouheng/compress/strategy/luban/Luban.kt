package me.shouheng.compress.strategy.luban

import android.os.AsyncTask
import io.reactivex.Flowable
import me.shouheng.compress.strategy.SimpleStrategy
import me.shouheng.compress.strategy.config.Config
import me.shouheng.compress.utils.CFileUtils
import me.shouheng.compress.utils.CImageUtils
import java.io.File
import java.io.IOException

/**
 * The compress algorithm by [Luban](https://github.com/Curzibn/Luban).
 *
 * @author WngShhng
 * @version 2019-05-17
 */
class Luban : SimpleStrategy() {

    /**
     * The image won't be compressed if the image size smaller than this value (KB).
     */
    private var ignoreSize: Int = Config.LUBAN_DEFAULT_IGNORE_SIZE // KB

    /**
     * Should copy the image if the size of original image is less than [ignoreSize].
     */
    private var copyWhenIgnore: Boolean = Config.LUBAN_COPY_WHEN_IGNORE

    /**
     * The file won't be compressed if the image size is less than this value.
     * Note that the original file will be returned from [me.shouheng.compress.listener.CompressListener]
     * and [asFlowable], if [copyWhenIgnore] was false.
     * Otherwise the original file will be copied to the destination.
     *
     * @param ignoreSize the size to ignore.
     * @return           luban instance
     */
    fun setIgnoreSize(ignoreSize: Int, copyWhenIgnore: Boolean): Luban {
        this.ignoreSize = ignoreSize
        this.copyWhenIgnore = copyWhenIgnore
        return this
    }

    /**
     * The [android.graphics.BitmapFactory.Options.inSampleSize] calculation for Luban.
     *
     * @return the in sample size for Luban.
     */
    override fun calInSampleSize(): Int {
        srcWidth = if (srcWidth % 2 == 1) srcWidth + 1 else srcWidth
        srcHeight = if (srcHeight % 2 == 1) srcHeight + 1 else srcHeight

        val longSide = Math.max(srcWidth, srcHeight)
        val shortSide = Math.min(srcWidth, srcHeight)

        val scale = shortSide.toFloat() / longSide
        return if (scale <= 1 && scale > 0.5625) {
            if (longSide < 1664) {
                1
            } else if (longSide < 4990) {
                2
            } else if (longSide > 4990 && longSide < 10240) {
                4
            } else {
                if (longSide / 1280 == 0) 1 else longSide / 1280
            }
        } else if (scale <= 0.5625 && scale > 0.5) {
            if (longSide / 1280 == 0) 1 else longSide / 1280
        } else {
            Math.ceil(longSide / (1280.0 / scale)).toInt()
        }
    }

    override fun asFlowable(): Flowable<File> {
        return if (srcFile == null || CImageUtils.needCompress(srcFile!!.absolutePath, ignoreSize)) {
            super.asFlowable()
        } else {
            // don't need to compress.
            if (copyWhenIgnore) {
                Flowable.defer<File> {
                    notifyCompressStart()
                    val succeed = CFileUtils.copyFile(srcFile!!, outFile!!)
                    if (succeed) {
                        notifyCompressSuccess(outFile!!)
                        Flowable.just(outFile)
                    } else {
                        val e = IOException("Failed when copying file...")
                        notifyCompressError(e)
                        Flowable.error(e)
                    }
                }
            } else {
                Flowable.defer {
                    notifyCompressStart()
                    notifyCompressSuccess(srcFile!!)
                    Flowable.just(srcFile!!)
                }
            }
        }
    }

    override fun launch() {
        if (srcFile == null || CImageUtils.needCompress(srcFile!!.absolutePath, ignoreSize)) {
            super.launch()
        } else {
            if (copyWhenIgnore) {
                AsyncTask.SERIAL_EXECUTOR.execute {
                    notifyCompressStart()
                    val succeed = CFileUtils.copyFile(srcFile!!, outFile!!)
                    if (succeed) {
                        notifyCompressSuccess(outFile!!)
                    } else {
                        val e = IOException("Failed when copying file...")
                        notifyCompressError(e)
                    }
                }
            } else {
                notifyCompressStart()
                notifyCompressSuccess(srcFile!!)
            }
        }
    }
}
