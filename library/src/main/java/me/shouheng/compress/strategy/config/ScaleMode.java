package me.shouheng.compress.strategy.config;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static me.shouheng.compress.strategy.config.ScaleMode.*;

/**
 * Image scale mode enums.
 *
 * @author WngShhng (shouheng2015@gmail.com)
 * @version 2019/5/17 0:03
 */
@IntDef({SCALE_LARGER, SCALE_SMALLER, SCALE_WIDTH, SCALE_HEIGHT})
@Retention(RetentionPolicy.SOURCE)
public @interface ScaleMode {

    /**
     * Scale according to larger side, another will change according to original image width/height ratio.
     *
     * For example:
     *
     * 1. If the original image is (W:1000, H:500), destination is (W:100, H:100), then the result
     * size will be (W:100, H:50).
     * 2. If the original image is (W:500, H:1000), destination is (W:100, H:100), then the result
     * size will be (W:50, H:100).
     */
    int SCALE_LARGER    = 0;

    /**
     * Scale according to smaller, another side will change according to original image width/height ratio.
     *
     * For example:
     *
     * 1. If the original image is (W:1000, H:500), destination is (W:100, H:100), then the result
     * size will be (W:200, H:100).
     * 2. If the original image is (W:500, H:1000), destination is (W:100, H:100), then the result
     * size will be (W:100, H:200).
     */
    int SCALE_SMALLER   = 1;

    /**
     * Scale the width, and the height will change according to the image ratio.
     *
     * For example:
     *
     * 1. If the original image is (W:1000, H:500), destination is (W:100, H:100). then the result
     * size will be (W:100, H:50).
     * 2. If the original image is (W:500, H:1000), destination is (W:100, H:100), then the result
     * size will be (W:100, H:200).
     */
    int SCALE_WIDTH     = 2;

    /**
     * Scale the width, and the height will change according to the image ratio.
     *
     * For example:
     *
     * 1. If the original image is (W:1000, H:500), destination is (W:100, H:100). then the result
     * size will be (W:200, H:100).
     * 2. If the original image is (W:500, H:1000), destination is (W:100, H:100), then the result
     * size will be (W:50, H:100).
     */
    int SCALE_HEIGHT    = 3;

}
