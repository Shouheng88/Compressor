package me.shouheng.compress.strategy

import android.graphics.*
import android.os.AsyncTask
import io.reactivex.Flowable
import kotlinx.coroutines.withContext
import me.shouheng.compress.AbstractStrategy
import me.shouheng.compress.utils.CImageUtils
import me.shouheng.compress.utils.CLog
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.Callable
import kotlin.coroutines.CoroutineContext

/**
 * Simple image compress logic, change the sample size only. Implement the
 * [calInSampleSize] method to add your own sample logic.
 *
 * @author WngShnng
 */
abstract class SimpleStrategy : AbstractStrategy() {

    override fun getBitmap(): Bitmap? = compressByQuality()

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
            } catch (e: IOException) {
                notifyCompressError(e)
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
            } catch (e: IOException) {
                notifyCompressError(e)
                e.printStackTrace()
            }
        }
    }

    /*------------------------------------------- protected level -------------------------------------------*/

    /**
     * Calculate the [BitmapFactory.Options.inSampleSize] filed.
     *
     * @return the inSampleSize
     */
    protected abstract fun calInSampleSize(): Int

    /*------------------------------------------- inner level -------------------------------------------*/

    /**
     * Compress the bitmap and write it to file system (the target file).
     *
     * @return whether the compress logic is succeed.
     * @throws IOException the io exception
     */
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

    /**
     * Compress the source bitmap by the required quality
     *
     * @return the compressed bitmap.
     */
    private fun compressByQuality(): Bitmap? {
        val bitmap = compressByScale()
        if (CImageUtils.isEmptyBitmap(bitmap)) return null
        val baos = ByteArrayOutputStream()
        bitmap!!.compress(format, quality, baos)
        val bytes = baos.toByteArray()
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    /** Compress the source bitmap by scaling it. */
    private fun compressByScale(): Bitmap? {
        imageSource?:return null

        prepareImageSizeInfo()

        val inSampleSize = calInSampleSize()
        val options = BitmapFactory.Options()
        options.inSampleSize = inSampleSize
        val origin = imageSource!!.bitmap(options)
        var result = origin.bitmap

        if (origin.needScale) {
            val bitmap = origin.bitmap
            // Scale bitmap according to inSampleSize
            val reqWidth = (srcWidth.toFloat() / inSampleSize).toInt()
            val reqHeight = (srcHeight.toFloat() / inSampleSize).toInt()
            result = Bitmap.createBitmap(reqWidth, reqHeight, Bitmap.Config.ARGB_8888)
            val ratioX = reqWidth / bitmap.width.toFloat()
            val ratioY = reqHeight / bitmap.height.toFloat()
            val middleX = reqWidth / 2.0f
            val middleY = reqHeight / 2.0f
            val scaleMatrix = Matrix()
            scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)
            val canvas = Canvas(result)
            canvas.matrix = scaleMatrix
            canvas.drawBitmap(
                bitmap, middleX - srcWidth / 2,
                middleY - srcHeight / 2, Paint(Paint.FILTER_BITMAP_FLAG)
            )
        }

        // Rotate bitmap if necessary.
        return imageSource?.rotation(result)
    }
}
