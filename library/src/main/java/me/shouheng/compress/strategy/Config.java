package me.shouheng.compress.strategy;

import android.graphics.Bitmap;

import static me.shouheng.compress.strategy.ScaleMode.SCALE_LARGER;

/**
 * The basic configuration for compress strategy.
 *
 * @author WngShhng (shouheng2015@gmail.com)
 * @version 2019/3/11 20:34
 */
public final class Config {

    private Config() {
        throw new UnsupportedOperationException("u can't initialize me");
    }

    public static final Bitmap.CompressFormat DEFAULT_COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;

    public static final int DEFAULT_COMPRESS_QUALITY = 75; // [0, 100]

    public static final String DEFAULT_CACHE_DIRECTORY_NAME = "compressor";

    public static final int LUBAN_DEFAULT_IGNORE_SIZE = 100; // KB

    public static final boolean LUBAN_COPY_WHEN_IGNORE = true;

    public static final float COMPRESSOR_DEFAULT_MAX_WIDTH = 612.0f;

    public static final float COMPRESSOR_DEFAULT_MAX_HEIGHT = 816.0f;

    @ScaleMode.Mode
    public static final int COMPRESSOR_DEFAULT_SCALE_MODE = SCALE_LARGER;

}
