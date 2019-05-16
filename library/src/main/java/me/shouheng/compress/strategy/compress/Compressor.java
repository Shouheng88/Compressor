package me.shouheng.compress.strategy.compress;

import android.graphics.*;
import android.os.AsyncTask;
import io.reactivex.Flowable;
import me.shouheng.compress.AbstractStrategy;
import me.shouheng.compress.strategy.Config;
import me.shouheng.compress.strategy.ScaleMode;
import me.shouheng.compress.utils.ImageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * The compress algorithm by <a href="https://github.com/zetbaitsu/Compressor">Compressor</a>.
 *
 * @author WngShhng
 * @version 2019-05-17
 */
public class Compressor extends AbstractStrategy {

    private float maxWidth = Config.COMPRESSOR_DEFAULT_MAX_WIDTH;

    private float maxHeight = Config.COMPRESSOR_DEFAULT_MAX_HEIGHT;

    @ScaleMode.Mode
    private int scaleMode = Config.COMPRESSOR_DEFAULT_SCALE_MODE;

    /**
     * Set the max width of compressed image.
     *
     * @param maxWidth the max width in pixels.
     * @return compressor object.
     */
    public Compressor setMaxWidth(float maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    /**
     * Set the max height of compressed image.
     *
     * @param maxHeight the max height in pixels.
     * @return the compressor object.
     */
    public Compressor setMaxHeight(float maxHeight) {
        this.maxHeight = maxHeight;
        return this;
    }

    /**
     * Set the scale mode when the destination image ratio differ from the original original.
     * Might be one of {@link ScaleMode#SCALE_LARGER},
     * {@link ScaleMode#SCALE_SMALLER},
     * {@link ScaleMode#SCALE_WIDTH} or
     * {@link ScaleMode#SCALE_HEIGHT}.
     *
     * @param scaleMode the scale mode.
     * @return the compressor object.
     * @see ScaleMode
     */
    public Compressor setScaleMode(@ScaleMode.Mode int scaleMode) {
        this.scaleMode = scaleMode;
        return this;
    }

    private boolean doCompress() throws IOException {
        prepareImageSizeInfo();

        Bitmap bitmap = decodeBitmap();
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

    private Bitmap decodeBitmap() {
        float imgRatio = (float) srcWidth / (float) srcHeight;
        float reqRatio = maxWidth / maxHeight;

        int reqWidth = calculateRequiredWidth(imgRatio, reqRatio);
        int reqHeight = calculateRequiredHeight(imgRatio, reqRatio);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = 1;
        BitmapFactory.decodeFile(srcFile.getAbsolutePath(), options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        Bitmap scaledBitmap = null;
        Bitmap bmp = null;
        try {
            bmp = BitmapFactory.decodeFile(srcFile.getAbsolutePath(), options);
            scaledBitmap = Bitmap.createBitmap(reqWidth, reqHeight, config == null ? Bitmap.Config.ARGB_8888 : config);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }

        // Return null if OOM.
        if (bmp == null || scaledBitmap == null) {
            return null;
        }

        // Scale the bitmap.
        float ratioX = reqWidth / (float) options.outWidth;
        float ratioY = reqHeight / (float) options.outHeight;
        float middleX = reqWidth / 2.0f;
        float middleY = reqHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2,
                middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));
        bmp.recycle();

        int orientation = ImageUtils.getImageAngle(srcFile);
        if (orientation != 0) {
            scaledBitmap = ImageUtils.rotateBitmap(scaledBitmap, ImageUtils.getImageAngle(srcFile));
        }

        return scaledBitmap;
    }

    protected int calculateRequiredWidth(float imgRatio, float reqRatio) {
        switch (scaleMode) {
            case ScaleMode.SCALE_LARGER: {
                if (srcHeight > maxHeight || srcWidth > maxWidth) {
                    // If Height is greater
                    if (imgRatio < reqRatio) {
                        imgRatio = maxHeight / srcHeight;
                        return (int) (imgRatio * srcWidth);
                    }  // If Width is greater
                    else if (imgRatio > reqRatio) {
                        return (int) maxWidth;
                    }
                }
                break;
            }
            case ScaleMode.SCALE_SMALLER: {
                if (srcHeight > maxHeight || srcWidth > maxWidth) {
                    // If Height is greater
                    if (imgRatio < reqRatio) {
                        return (int) maxWidth;
                    }  // If Width is greater
                    else if (imgRatio > reqRatio) {
                        imgRatio = maxHeight / srcHeight;
                        return (int) (imgRatio * srcWidth);
                    }
                }
                break;
            }
            case ScaleMode.SCALE_HEIGHT: {
                return (int) (srcWidth * maxHeight / srcHeight);
            }
            case ScaleMode.SCALE_WIDTH: {
                return (int) maxWidth;
            }
            default:
                return (int) maxWidth;
        }
        return (int) maxWidth;
    }

    protected int calculateRequiredHeight(float imgRatio, float reqRatio) {
        switch (scaleMode) {
            case ScaleMode.SCALE_LARGER: {
                if (srcHeight > maxHeight || srcWidth > maxWidth) {
                    // If Height is greater
                    if (imgRatio < reqRatio) {
                        return (int) maxHeight;
                    }  // If Width is greater
                    else if (imgRatio > reqRatio) {
                        imgRatio = maxWidth / srcWidth;
                        return (int) (imgRatio * srcHeight);
                    }
                }
                break;
            }
            case ScaleMode.SCALE_SMALLER: {
                if (srcHeight > maxHeight || srcWidth > maxWidth) {
                    // If Height is greater
                    if (imgRatio < reqRatio) {
                        imgRatio = maxWidth / srcWidth;
                        return (int) (imgRatio * srcHeight);
                    }  // If Width is greater
                    else if (imgRatio > reqRatio) {
                        return (int) maxHeight;
                    }
                }
                break;
            }
            case ScaleMode.SCALE_HEIGHT: {
                return (int) maxHeight;
            }
            case ScaleMode.SCALE_WIDTH: {
                imgRatio = maxWidth / srcWidth;
                return (int) (srcHeight * imgRatio);
            }
            default:
                return (int) maxHeight;
        }
        return (int) maxHeight;
    }

    protected int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    @Override
    public Flowable<File> asFlowable() {
        return Flowable.defer(new Callable<Flowable<File>>() {
            @Override
            public Flowable<File> call() {
                try {
                    notifyCompressStart();
                    boolean succeed = doCompress();
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
                    boolean succeed = doCompress();
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

}
