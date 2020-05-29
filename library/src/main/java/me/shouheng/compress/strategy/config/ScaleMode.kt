package me.shouheng.compress.strategy.config

import android.support.annotation.IntDef
import me.shouheng.compress.strategy.config.ScaleMode.Companion.SCALE_HEIGHT
import me.shouheng.compress.strategy.config.ScaleMode.Companion.SCALE_LARGER
import me.shouheng.compress.strategy.config.ScaleMode.Companion.SCALE_SMALLER
import me.shouheng.compress.strategy.config.ScaleMode.Companion.SCALE_WIDTH

/**
 * Image scale mode enums.
 *
 * @author WngShhng (shouheng2015@gmail.com)
 * @version 2019/5/17 0:03
 */
@IntDef(SCALE_LARGER, SCALE_SMALLER, SCALE_WIDTH, SCALE_HEIGHT)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class ScaleMode {
    companion object {

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
        const val SCALE_LARGER = 0

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
        const val SCALE_SMALLER = 1

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
        const val SCALE_WIDTH = 2

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
        const val SCALE_HEIGHT = 3
    }

}
