package me.shouheng.compress;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import me.shouheng.compress.request.BitmapBuilder;
import me.shouheng.compress.strategy.config.Config;

import java.io.File;

/**
 * The abstract compress strategy.
 * Extend it and implement the abstract methods to add your own compress strategy.
 *
 * @author WngShhng
 */
public abstract class AbstractStrategy extends RequestBuilder<File> implements Handler.Callback {

    protected File srcFile;
    protected Bitmap srcBitmap;
    protected byte[] srcData;
    protected File outFile;
    protected Bitmap.CompressFormat format = Config.DEFAULT_COMPRESS_FORMAT;
    protected int quality = Config.DEFAULT_COMPRESS_QUALITY;
    protected boolean autoRecycle = Config.DEFAULT_BITMAP_RECYCLE;

    protected int srcWidth;
    protected int srcHeight;

    public BitmapBuilder asBitmap() {
        BitmapBuilder builder = new BitmapBuilder();
        builder.setAbstractStrategy(this);
        return builder;
    }

    /*------------------------------------------- protected level -------------------------------------------*/

    /**
     * Prepare original image size info before calculate the image sample size.
     */
    protected void prepareImageSizeInfo() {
        if (srcBitmap != null) {
            this.srcWidth = srcBitmap.getWidth();
            this.srcHeight = srcBitmap.getHeight();
            return;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = 1;
        if (srcData != null) {
            BitmapFactory.decodeByteArray(srcData, 0, srcData.length, options);
        } else if (srcFile != null) {
            BitmapFactory.decodeFile(srcFile.getAbsolutePath(), options);
        }
        this.srcWidth = options.outWidth;
        this.srcHeight = options.outHeight;
    }

    @Override
    protected Bitmap getBitmap() {
        throw new IllegalStateException("This #getBitmap() method is not implemented by your strategy.");
    }

    /*------------------------------------------- package level -------------------------------------------*/

    /* package */ void setSrcFile(File srcFile) {
        this.srcFile = srcFile;
    }

    /* package */ void setFormat(Bitmap.CompressFormat format) {
        this.format = format;
    }

    /* package */ void setQuality(int quality) {
        this.quality = quality;
    }

    /* package */ void setOutFile(File outFile) {
        this.outFile = outFile;
    }

    /* package */ void setSrcBitmap(Bitmap srcBitmap) {
        this.srcBitmap = srcBitmap;
    }

    /* package */ void setSrcData(byte[] srcData) {
        this.srcData = srcData;
    }

    /**
     * Whether the source bitmap should be recycled. The source bitmap means the birmap you used in
     * {@link Compress#with(Context, Bitmap)}. Since you may need to use the bitmap latter, so we
     * added this method for you to custom this action,
     *
     * @param autoRecycle whether the source bitmap should be recycled automatically
     */
    /* package */ void setAutoRecycle(boolean autoRecycle) {
        this.autoRecycle = autoRecycle;
    }
}
