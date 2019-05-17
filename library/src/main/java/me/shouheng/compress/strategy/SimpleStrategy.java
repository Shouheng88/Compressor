package me.shouheng.compress.strategy;

import android.graphics.*;
import android.os.AsyncTask;
import io.reactivex.Flowable;
import me.shouheng.compress.AbstractStrategy;
import me.shouheng.compress.utils.ImageUtils;
import me.shouheng.compress.utils.LogLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * Simple image compress logic, change the sample size only. Implement the
 * {@link #calInSampleSize()} method to add your own sample logic.
 *
 * @author WngShnng
 */
public abstract class SimpleStrategy extends AbstractStrategy {

    @Override
    public File get() {
        try {
            notifyCompressStart();
            compressAndWrite();
            notifyCompressSuccess(outFile);
        } catch (Exception e) {
            LogLog.e(e.getMessage());
            notifyCompressError(e);
        }
        return outFile;
    }

    @Override
    protected Bitmap getBitmap() {
        return doCompress();
    }

    @Override
    public Flowable<File> asFlowable() {
        return Flowable.defer(new Callable<Flowable<File>>() {
            @Override
            public Flowable<File> call() {
                try {
                    notifyCompressStart();
                    boolean succeed = compressAndWrite();
                    if (succeed) {
                        notifyCompressSuccess(outFile);
                    } else {
                        notifyCompressError(new Exception("Failed to compress image, either caused by OOM or other problems."));
                    }
                    return Flowable.just(outFile);
                } catch (IOException e) {
                    notifyCompressError(e);
                    return Flowable.error(e);
                }
            }
        });
    }

    @Override
    public void launch() {
        AsyncTask.SERIAL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    notifyCompressStart();
                    boolean succeed = compressAndWrite();
                    if (succeed) {
                        notifyCompressSuccess(outFile);
                    } else {
                        notifyCompressError(new Exception("Failed to compress image, either caused by OOM or other problems."));
                    }
                } catch (IOException e) {
                    notifyCompressError(e);
                    e.printStackTrace();
                }
            }
        });
    }

    /*------------------------------------------- protected level -------------------------------------------*/

    /**
     * Calculate the {@link BitmapFactory.Options#inSampleSize} filed.
     *
     * @return the inSampleSize
     */
    protected abstract int calInSampleSize();

    /*------------------------------------------- inner level -------------------------------------------*/

    private boolean compressAndWrite() throws IOException {
        Bitmap bitmap = doCompress();
        if (bitmap != null) {
            FileOutputStream fos = new FileOutputStream(outFile);
            bitmap.compress(format, quality, fos);
            fos.flush();
            fos.close();
        } else {
            return false;
        }
        return true;
    }

    private Bitmap doCompress() {
        prepareImageSizeInfo();

        int inSampleSize = calInSampleSize();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;
        Bitmap bitmap = null;

        if (srcBitmap != null) {
            // scale bitmap according to inSampleSize
            int reqWidth = (int) (srcWidth * 1.f / inSampleSize);
            int reqHeight = (int) (srcHeight * 1.f / inSampleSize);
            Bitmap scaledBitmap = Bitmap.createBitmap(reqWidth, reqHeight, Bitmap.Config.ARGB_8888);
            float ratioX = reqWidth / (float) srcBitmap.getWidth();
            float ratioY = reqHeight / (float) srcBitmap.getHeight();
            float middleX = reqWidth / 2.0f;
            float middleY = reqHeight / 2.0f;
            Matrix scaleMatrix = new Matrix();
            scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);
            Canvas canvas = new Canvas(scaledBitmap);
            canvas.setMatrix(scaleMatrix);
            canvas.drawBitmap(srcBitmap, middleX - srcWidth / 2,
                    middleY - srcHeight / 2, new Paint(Paint.FILTER_BITMAP_FLAG));
            srcBitmap.recycle();
            return scaledBitmap;
        } else if (srcData != null || srcFile != null) {
            // scale bitmap by bitmap decode options
            if (srcFile != null) {
                bitmap = BitmapFactory.decodeFile(srcFile.getAbsolutePath(), options);
            } else {
                bitmap = BitmapFactory.decodeByteArray(srcData, 0, srcData.length, options);
            }
        }

        if (srcFile != null) {
            int orientation = ImageUtils.getImageAngle(srcFile);
            if (orientation != 0) {
                bitmap = ImageUtils.rotateBitmap(bitmap, orientation);
            }
        }
        return bitmap;
    }

}
