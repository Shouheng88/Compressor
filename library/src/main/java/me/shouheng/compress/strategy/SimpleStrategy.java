package me.shouheng.compress.strategy;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import io.reactivex.Flowable;
import me.shouheng.compress.AbstractStrategy;
import me.shouheng.compress.utils.ImageUtils;

import java.io.ByteArrayOutputStream;
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

    /**
     * Calculate the {@link BitmapFactory.Options#inSampleSize} filed.
     *
     * @return the inSampleSize
     */
    protected abstract int calInSampleSize();

    private void doCompress() throws IOException {
        prepareImageSizeInfo();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calInSampleSize();

        Bitmap bitmap = BitmapFactory.decodeFile(srcFile.getAbsolutePath(), options);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        int orientation = ImageUtils.getImageAngle(srcFile);
        if (orientation != 0) {
            bitmap = ImageUtils.rotateBitmap(bitmap, orientation);
        }
        bitmap.compress(format, quality, bao);
        bitmap.recycle();

        FileOutputStream fos = new FileOutputStream(outFile);
        fos.write(bao.toByteArray());
        fos.flush();
        fos.close();
        bao.close();
    }

    @Override
    public Flowable<File> asFlowable() {
        return Flowable.defer(new Callable<Flowable<File>>() {
            @Override
            public Flowable<File> call() {
                try {
                    notifyCompressStart();
                    doCompress();
                    notifyCompressSuccess(outFile);
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
                    doCompress();
                    notifyCompressSuccess(outFile);
                } catch (IOException e) {
                    notifyCompressError(e);
                    e.printStackTrace();
                }
            }
        });
    }

}
