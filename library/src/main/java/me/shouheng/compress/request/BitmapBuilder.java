package me.shouheng.compress.request;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import io.reactivex.Flowable;
import me.shouheng.compress.AbstractStrategy;
import me.shouheng.compress.RequestBuilder;
import me.shouheng.compress.utils.LogLog;

import java.util.concurrent.Callable;

public class BitmapBuilder extends RequestBuilder<Bitmap> {

    public BitmapBuilder(AbstractStrategy abstractStrategy) {
        super(abstractStrategy);
    }

    public Bitmap get() {
        Bitmap bitmap = null;
        try {
            notifyCompressStart();
            bitmap = getBitmap();
            if (bitmap != null) {
                notifyCompressSuccess(bitmap);
            } else {
                notifyCompressError(new Exception("Failed to compress image, either caused by OOM or other problems."));
            }
        } catch (Exception e) {
            LogLog.e(e.getMessage());
            notifyCompressError(e);
        }
        return bitmap;
    }

    public Flowable<Bitmap> asFlowable() {
        return Flowable.defer(new Callable<Flowable<Bitmap>>() {
            @Override
            public Flowable<Bitmap> call() {
                try {
                    notifyCompressStart();
                    Bitmap bitmap = getBitmap();
                    if (bitmap != null) {
                        notifyCompressSuccess(bitmap);
                        return Flowable.just(bitmap);
                    } else {
                        Exception e = new Exception("Failed to compress image, either caused by OOM or other problems.");
                        notifyCompressError(e);
                        return Flowable.error(e);
                    }
                } catch (Exception e) {
                    notifyCompressError(e);
                    LogLog.e(e.getMessage());
                    return Flowable.error(e);
                }
            }
        });
    }

    public void launch() {
        AsyncTask.SERIAL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    notifyCompressStart();
                    Bitmap bitmap = getBitmap();
                    if (bitmap == null) {
                        notifyCompressError(new Exception("Failed to compress image, either caused by OOM or other problems."));
                    } else {
                        notifyCompressSuccess(bitmap);
                    }
                } catch (Exception e) {
                    notifyCompressError(e);
                    LogLog.e(e.getMessage());
                }
            }
        });
    }
}
