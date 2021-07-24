package me.shouheng.compress.strategy.config

import android.graphics.Bitmap
import me.shouheng.compress.strategy.config.ScaleMode.Companion.SCALE_LARGER

/**
 * The basic configuration for compress strategy.
 *
 * @author Shouheng Wang
 * @version 2019/3/11 20:34
 */
object Config {

    val DEFAULT_COMPRESS_FORMAT: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG

    const val DEFAULT_COMPRESS_QUALITY = 75 // [0, 100]

    const val DEFAULT_CACHE_DIRECTORY_NAME = "compressor"

    /** Whether should the bitmap recycled automatically. */
    const val DEFAULT_BITMAP_RECYCLE = true

    const val LUBAN_DEFAULT_IGNORE_SIZE = 100 // KB

    const val LUBAN_COPY_WHEN_IGNORE = true

    const val COMPRESSOR_DEFAULT_MAX_WIDTH = 612.0f

    const val COMPRESSOR_DEFAULT_MAX_HEIGHT = 816.0f

    @ScaleMode
    const val COMPRESSOR_DEFAULT_SCALE_MODE: Int = SCALE_LARGER
}
