package me.shouheng.compress.strategy.luban;

import android.os.AsyncTask;
import io.reactivex.Flowable;
import me.shouheng.compress.strategy.config.Config;
import me.shouheng.compress.strategy.SimpleStrategy;
import me.shouheng.compress.utils.CFileUtils;
import me.shouheng.compress.utils.CImageUtils;
import org.reactivestreams.Publisher;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * The compress algorithm by <a href="https://github.com/Curzibn/Luban">Luban</a>.
 *
 * @author WngShhng
 * @version 2019-05-17
 */
public class Luban extends SimpleStrategy {

    /**
     * The image won't be compressed if the image size smaller than this value (KB).
     */
    private int ignoreSize = Config.LUBAN_DEFAULT_IGNORE_SIZE; // KB

    /**
     * Should copy the image if the size of original image is less than {@link #ignoreSize}.
     */
    private boolean copyWhenIgnore = Config.LUBAN_COPY_WHEN_IGNORE;

    /**
     * The file won't be compressed if the image size is less than this value.
     * Note that the original file will be returned from {@link me.shouheng.compress.listener.CompressListener}
     * and {@link #asFlowable()}, if copyWhenIgnore was false. Otherwise the original file will be copied to the destination.
     *
     * @param ignoreSize  the size to ignore.
     * @return luban object
     */
    public Luban setIgnoreSize(int ignoreSize, boolean copyWhenIgnore) {
        this.ignoreSize = ignoreSize;
        this.copyWhenIgnore = copyWhenIgnore;
        return this;
    }

    /**
     * The {@link android.graphics.BitmapFactory.Options#inSampleSize} calculation for Luban.
     *
     * @return the in sample size for Luban.
     */
    @Override
    protected int calInSampleSize() {
        srcWidth = srcWidth % 2 == 1 ? srcWidth + 1 : srcWidth;
        srcHeight = srcHeight % 2 == 1 ? srcHeight + 1 : srcHeight;

        int longSide = Math.max(srcWidth, srcHeight);
        int shortSide = Math.min(srcWidth, srcHeight);

        float scale = ((float) shortSide / longSide);
        if (scale <= 1 && scale > 0.5625) {
            if (longSide < 1664) {
                return 1;
            } else if (longSide < 4990) {
                return 2;
            } else if (longSide > 4990 && longSide < 10240) {
                return 4;
            } else {
                return longSide / 1280 == 0 ? 1 : longSide / 1280;
            }
        } else if (scale <= 0.5625 && scale > 0.5) {
            return longSide / 1280 == 0 ? 1 : longSide / 1280;
        } else {
            return (int) Math.ceil(longSide / (1280.0 / scale));
        }
    }

    @Override
    public Flowable<File> asFlowable() {
        if (srcFile == null || CImageUtils.needCompress(srcFile.getAbsolutePath(), ignoreSize)) {
            return super.asFlowable();
        } else {
            // don't need to compress.
            if (copyWhenIgnore) {
                return Flowable.defer(new Callable<Publisher<? extends File>>() {
                    @Override
                    public Publisher<? extends File> call() {
                        notifyCompressStart();
                        boolean succeed = CFileUtils.copyFile(srcFile, outFile);
                        if (succeed) {
                            notifyCompressSuccess(outFile);
                            return Flowable.just(outFile);
                        } else {
                            IOException e = new IOException("Failed when copying file...");
                            notifyCompressError(e);
                            return Flowable.error(e);
                        }
                    }
                });
            } else {
                return Flowable.defer(new Callable<Flowable<File>>() {
                    @Override
                    public Flowable<File> call() {
                        notifyCompressStart();
                        notifyCompressSuccess(srcFile);
                        return Flowable.just(srcFile);
                    }
                });
            }
        }
    }

    @Override
    public void launch() {
        if (srcFile == null || CImageUtils.needCompress(srcFile.getAbsolutePath(), ignoreSize)) {
            super.launch();
        } else {
            if (copyWhenIgnore) {
                AsyncTask.SERIAL_EXECUTOR.execute(new Runnable() {
                    @Override
                    public void run() {
                        notifyCompressStart();
                        boolean succeed = CFileUtils.copyFile(srcFile, outFile);
                        if (succeed) {
                            notifyCompressSuccess(outFile);
                        } else {
                            IOException e = new IOException("Failed when copying file...");
                            notifyCompressError(e);
                        }
                    }
                });
            } else {
                notifyCompressStart();
                notifyCompressSuccess(srcFile);
            }
        }
    }

}
