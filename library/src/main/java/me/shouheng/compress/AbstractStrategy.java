package me.shouheng.compress;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import io.reactivex.Flowable;
import me.shouheng.compress.listener.CompressListener;
import me.shouheng.compress.strategy.Configuration;

import java.io.File;

/**
 * The abstract compress strategy. Extend it and implement the abstract methods to
 * add your own compress strategy.
 */
public abstract class AbstractStrategy implements Handler.Callback {

    private static final int MSG_COMPRESS_SUCCESS = 0;
    private static final int MSG_COMPRESS_START = 1;
    private static final int MSG_COMPRESS_ERROR = 2;

    protected File srcFile;
    protected File outFile;
    protected Bitmap.CompressFormat format = Configuration.DEFAULT_COMPRESS_FORMAT;
    protected int quality = Configuration.DEFAULT_COMPRESS_QUALITY;
    protected CompressListener compressListener;

    protected int srcWidth;
    protected int srcHeight;

    private Handler handler = new Handler(Looper.getMainLooper(), this);

    public abstract Flowable<File> asFlowable();

    public abstract void launch();

    /**
     * Prepare original image size info before calculate the image sample size.
     */
    protected void prepareImageSizeInfo() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = 1;
        BitmapFactory.decodeFile(srcFile.getAbsolutePath(), options);
        this.srcWidth = options.outWidth;
        this.srcHeight = options.outHeight;
    }

    /**
     * Rotate the original bitmap and return the result.
     *
     * @param srcBitmap original bitmap
     * @param angle angle to rotate
     * @return the rotated bitmap
     */
    protected Bitmap rotateBitmap(Bitmap srcBitmap, int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(), srcBitmap.getHeight(), matrix, true);
    }

    /**
     * Notify compress started.
     */
    protected void notifyCompressStart() {
        handler.sendMessage(handler.obtainMessage(MSG_COMPRESS_START));
    }

    /**
     * Notify compress succeed.
     *
     * @param result compressed result
     */
    protected void notifyCompressSuccess(File result) {
        handler.sendMessage(handler.obtainMessage(MSG_COMPRESS_SUCCESS, result));
    }

    /**
     * Notify compress error occurred.
     *
     * @param throwable the exception thrown
     */
    protected void notifyCompressError(Throwable throwable) {
        handler.sendMessage(handler.obtainMessage(MSG_COMPRESS_ERROR, throwable));
    }

    void setSrcFile(File srcFile) {
        this.srcFile = srcFile;
    }

    void setFormat(Bitmap.CompressFormat format) {
        this.format = format;
    }

    void setQuality(int quality) {
        this.quality = quality;
    }

    void setOutFile(File outFile) {
        this.outFile = outFile;
    }

    void setCompressListener(CompressListener compressListener) {
        this.compressListener = compressListener;
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (compressListener == null) return false;

        switch (msg.what) {
            case MSG_COMPRESS_START:
                compressListener.onStart();
                break;
            case MSG_COMPRESS_SUCCESS:
                compressListener.onSuccess((File) msg.obj);
                break;
            case MSG_COMPRESS_ERROR:
                compressListener.onError((Throwable) msg.obj);
                break;
            default:
                break;
        }
        return false;
    }

}
