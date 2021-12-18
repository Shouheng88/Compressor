package me.shouheng.sample.data

import android.graphics.Bitmap
import android.os.Build
import me.shouheng.compress.strategy.config.ScaleMode

/** Compress task launch type */
enum class LaunchType {
    LAUNCH, AS_FLOWABLE, GET, COROUTINES
}

/** Compress data source type */
enum class SourceType {
    FILE, BYTE_ARRAY, BITMAP, URI
}

/** Result type */
enum class ResultType {
    BITMAP, FILE
}

/** Color config options. */
val colorConfigs =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        arrayOf(
            Bitmap.Config.ARGB_8888,
            Bitmap.Config.ALPHA_8,
            Bitmap.Config.RGB_565,
            Bitmap.Config.ARGB_4444,
            Bitmap.Config.RGBA_F16,
            Bitmap.Config.HARDWARE)
    } else {
        arrayOf(
            Bitmap.Config.ARGB_8888,
            Bitmap.Config.ALPHA_8,
            Bitmap.Config.RGB_565,
            Bitmap.Config.ARGB_4444)
    }

/** Scale mode options. */
val scaleModes = arrayOf(
    ScaleMode.SCALE_LARGER,
    ScaleMode.SCALE_SMALLER,
    ScaleMode.SCALE_WIDTH,
    ScaleMode.SCALE_HEIGHT
)

/** Source type options. */
val sourceTypes = arrayOf(
    SourceType.FILE,
    SourceType.BYTE_ARRAY,
    SourceType.BITMAP,
    SourceType.URI
)

/** Result type options. */
val resultTypes = arrayOf(
    ResultType.FILE,
    ResultType.BITMAP
)

/** Launch type options. */
val launchTypes = arrayOf(
    LaunchType.LAUNCH,
    LaunchType.AS_FLOWABLE,
    LaunchType.GET,
    LaunchType.COROUTINES
)

const val REQUEST_IMAGE_CAPTURE     = 0x0100
const val REQUEST_SELECT_IMAGE      = 0x0101