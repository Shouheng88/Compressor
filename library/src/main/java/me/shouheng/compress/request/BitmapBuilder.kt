package me.shouheng.compress.request

import android.graphics.Bitmap
import android.os.AsyncTask
import io.reactivex.Flowable
import kotlinx.coroutines.withContext
import me.shouheng.compress.RequestBuilder
import me.shouheng.compress.utils.CLog

import java.util.concurrent.Callable
import kotlin.coroutines.CoroutineContext

class BitmapBuilder : RequestBuilder<Bitmap>() {

    override fun get(): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            notifyCompressStart()
            bitmap = getBitmap()
            if (bitmap != null) {
                notifyCompressSuccess(bitmap)
            } else {
                notifyCompressError(Exception("Failed to compress image, either caused by OOM or other problems."))
            }
        } catch (e: Exception) {
            CLog.e(e.message)
            notifyCompressError(e)
        }
        return bitmap
    }

    override suspend fun get(coroutineContext: CoroutineContext): Bitmap? = withContext(coroutineContext) {
        return@withContext get()
    }

    override fun asFlowable(): Flowable<Bitmap> {
        return Flowable.defer(Callable {
            try {
                notifyCompressStart()
                val bitmap = getBitmap()
                if (bitmap != null) {
                    notifyCompressSuccess(bitmap)
                    return@Callable Flowable.just(bitmap)
                } else {
                    val e = Exception("Failed to compress image, either caused by OOM or other problems.")
                    notifyCompressError(e)
                    return@Callable Flowable.error<Bitmap>(e)
                }
            } catch (e: Exception) {
                notifyCompressError(e)
                CLog.e(e.message)
                return@Callable Flowable.error<Bitmap>(e)
            }
        })
    }

    override fun launch() {
        AsyncTask.SERIAL_EXECUTOR.execute {
            try {
                notifyCompressStart()
                val bitmap = getBitmap()
                if (bitmap == null) {
                    notifyCompressError(Exception("Failed to compress image, either caused by OOM or other problems."))
                } else {
                    notifyCompressSuccess(bitmap)
                }
            } catch (e: Exception) {
                notifyCompressError(e)
                CLog.e(e.message)
            }
        }
    }
}
