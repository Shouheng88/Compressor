package me.shouheng.compress

import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.os.Message
import io.reactivex.Flowable
import kotlinx.coroutines.Dispatchers
import me.shouheng.compress.strategy.IImageSource
import me.shouheng.compress.utils.CLog
import kotlin.coroutines.CoroutineContext

/** The compress algorithm. */
interface Algorithm<R> {

    /** Blocking method used to get the compressed result in current thread. */
    fun get(): R?

    /** Get the result using kotlin coroutines, for example [Dispatchers.IO]. */
    suspend fun get(coroutineContext: CoroutineContext): R?

    /** Use RxJava to get the result. */
    fun asFlowable(): Flowable<R>

    /** Launch the compressor task in [AsyncTask]. */
    fun launch()
}

/**
 * The request builder object. Used to build the compress request. It contains many useful methods
 * like [notifyCompressSuccess] etc. This class has two children [AbstractStrategy] and
 * [me.shouheng.compress.request.BitmapBuilder] by default.
 *
 * @param <R> the result type.
 */
abstract class RequestBuilder<R> : Handler.Callback, Algorithm<R> {

    private var compressListener: Callback<R>?      = null
    private var abstractStrategy: AbstractStrategy? = null
    protected var imageSource: IImageSource<*>?     = null

    private val handler = Handler(Looper.getMainLooper(), this)

    open fun getBitmap(): Bitmap? = abstractStrategy?.getBitmap()

    /** Set the image source. */
    internal fun setImageSource(imageSource: IImageSource<*>) {
        this.imageSource = imageSource
    }

    override fun handleMessage(msg: Message): Boolean {
        compressListener ?: return false
        when (msg.what) {
            MSG_COMPRESS_START   -> {
                CLog.i("Compress process started!")
                compressListener?.onStart()
            }
            MSG_COMPRESS_SUCCESS -> {
                CLog.i("Compress process succeed!")
                compressListener?.onSuccess(msg.obj as R)
            }
            MSG_COMPRESS_ERROR   -> {
                CLog.i("Compress process failed, due [${(msg.obj as Throwable).message}]!")
                compressListener?.onError(msg.obj as Throwable)
            }
            else -> { /* noop */ }
        }
        return false
    }

    fun setCompressListener(compressListener: Callback<R>?): RequestBuilder<R> {
        this.compressListener = compressListener
        return this
    }

    fun setAbstractStrategy(abstractStrategy: AbstractStrategy) {
        this.abstractStrategy = abstractStrategy
    }

    protected fun notifyCompressStart() {
        handler.sendMessage(handler.obtainMessage(MSG_COMPRESS_START))
    }

    protected fun notifyCompressSuccess(result: R) {
        handler.sendMessage(handler.obtainMessage(MSG_COMPRESS_SUCCESS, result))
    }

    protected fun notifyCompressError(throwable: Throwable) {
        handler.sendMessage(handler.obtainMessage(MSG_COMPRESS_ERROR, throwable))
    }

    interface Callback<T> {

        /** Will be called when start to compress. */
        fun onStart()

        /** Will be called when finish compress. */
        fun onSuccess(result: T)

        /** Will be called when error occurred. */
        fun onError(throwable: Throwable)
    }

    companion object {
        private const val MSG_COMPRESS_SUCCESS  = 0
        private const val MSG_COMPRESS_START    = 1
        private const val MSG_COMPRESS_ERROR    = 2
    }
}
