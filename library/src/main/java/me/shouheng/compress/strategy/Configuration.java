package me.shouheng.compress.strategy;

import android.graphics.Bitmap;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The basic configuration for compress strategy.
 *
 * @author WngShhng (shouheng2015@gmail.com)
 * @version 2019/3/11 20:34
 */
public final class Configuration {

    /**
     * Scale according to larger side, another will change according to original image width/height ratio.
     * For example, if the original image (W:1000, H:500), destination (W:100, H:100), then the result
     * size will be (W:100, H:50).
     */
    public static final int SCALE_LARGER    = 0;

    /**
     * Scale according to smaller, another side will change according to original image width/height ratio.
     * For example, if the original image wa (W:1000, H:500), destination (W:100, H:100), then the result
     * size will be (W:200, H:100).
     */
    public static final int SCALE_SMALLER   = 1;

    /**
     * Scale the width, and the height will change according to the image ratio.
     * For example, if the original image (W:1000, H:500), destination (W:100, H:100). then the result
     * size will be (W:100, H:50).
     */
    public static final int SCALE_WIDTH     = 2;

    /**
     * Scale the width, and the height will change according to the image ratio.
     * For example, if the original image (W:1000, H:500), destination (W:100, H:100). then the result
     * size will be (W:200, H:100).
     */
    public static final int SCALE_HEIGHT    = 3;

    public static final Bitmap.CompressFormat DEFAULT_COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;

    public static final int DEFAULT_COMPRESS_QUALITY = 75; // [0, 100]

    public static final String DEFAULT_CACHE_DIRECTORY_NAME = "compressor";

    public static final int LUBAN_DEFAULT_IGNORE_SIZE = 100; // KB

    public static final boolean LUBAN_COPY_WHEN_IGNORE = true;

    public static final float COMPRESSOR_DEFAULT_MAX_WIDTH = 612.0f;

    public static final float COMPRESSOR_DEFAULT_MAX_HEIGHT = 816.0f;

    public static final int COMPRESSOR_DEFAULT_SCALE_MODE = SCALE_LARGER;

    @IntDef({SCALE_LARGER, SCALE_SMALLER, SCALE_WIDTH, SCALE_HEIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ScaleMode {
    }

}
