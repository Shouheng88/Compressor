package me.shouheng.compress;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import io.reactivex.Flowable;

/**
 * The request builder object. Used to build the compress request. It contains many useful
 * methods like {@link #notifyCompressSuccess(Object)}.
 * This class has two children, {@link AbstractStrategy} and {@link me.shouheng.compress.request.BitmapBuilder}
 *
 * @param <T> the required result type
 */
public abstract class RequestBuilder<T> implements Handler.Callback {

    private static final int MSG_COMPRESS_SUCCESS       = 0;
    private static final int MSG_COMPRESS_START         = 1;
    private static final int MSG_COMPRESS_ERROR         = 2;

    private Callback<T> compressListener;
    private AbstractStrategy abstractStrategy;

    private Handler handler = new Handler(Looper.getMainLooper(), this);

    public RequestBuilder<T> setCompressListener(Callback<T> compressListener) {
        this.compressListener = compressListener;
        return this;
    }

    public abstract T get();

    public abstract Flowable<T> asFlowable();

    public abstract void launch();

    @Override
    public boolean handleMessage(Message msg) {
        if (compressListener == null) return false;
        switch (msg.what) {
            case MSG_COMPRESS_START:
                compressListener.onStart();
                break;
            case MSG_COMPRESS_SUCCESS:
                compressListener.onSuccess((T) msg.obj);
                break;
            case MSG_COMPRESS_ERROR:
                compressListener.onError((Throwable) msg.obj);
                break;
            default:
                break;
        }
        return false;
    }

    protected void setAbstractStrategy(AbstractStrategy abstractStrategy) {
        this.abstractStrategy = abstractStrategy;
    }

    /**
     * Get bitmap from given strategy. The strategy must implement this method.
     * Mainly this method is used to get bitmap in {@link RequestBuilder}
     * like {@link me.shouheng.compress.request.BitmapBuilder} to get bitmap from real compressor
     * like luban and compressor and transform the bitmap to required type.
     *
     * @return the bitmap
     */
    protected Bitmap getBitmap() {
        if (abstractStrategy == null) {
            throw new IllegalStateException("The real compress strategy is null.");
        }
        Bitmap bitmap = abstractStrategy.getBitmap();
        if (bitmap == null) {
            throw new IllegalStateException("The compress strategy must implement #getBitmap() method.");
        }
        return bitmap;
    }

    protected void notifyCompressStart() {
        handler.sendMessage(handler.obtainMessage(MSG_COMPRESS_START));
    }

    protected void notifyCompressSuccess(T result) {
        handler.sendMessage(handler.obtainMessage(MSG_COMPRESS_SUCCESS, result));
    }

    protected void notifyCompressError(Throwable throwable) {
        handler.sendMessage(handler.obtainMessage(MSG_COMPRESS_ERROR, throwable));
    }

    public interface Callback<T> {
        /**
         * Will be called when start to compress.
         */
        void onStart();

        /**
         * Will be called when finish compress.
         *
         * @param result the compressed image
         */
        void onSuccess(T result);

        /**
         * Will be called when error occurred.
         *
         * @param throwable the throwable exception
         */
        void onError(Throwable throwable);
    }
}
