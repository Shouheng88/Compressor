package me.shouheng.compress;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import io.reactivex.Flowable;

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

    protected Bitmap getBitmap() {
        return abstractStrategy.getBitmap();
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
