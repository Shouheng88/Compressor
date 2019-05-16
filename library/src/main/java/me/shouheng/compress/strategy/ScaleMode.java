package me.shouheng.compress.strategy;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author WngShhng (shouheng2015@gmail.com)
 * @version 2019/5/17 0:03
 */
public final class ScaleMode {

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

    @IntDef({SCALE_LARGER, SCALE_SMALLER, SCALE_WIDTH, SCALE_HEIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
    }
}
