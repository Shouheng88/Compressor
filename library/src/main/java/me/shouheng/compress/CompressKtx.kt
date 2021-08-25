import android.graphics.Bitmap
import androidx.annotation.RestrictTo
import me.shouheng.compress.AbstractStrategy
import me.shouheng.compress.Compress
import me.shouheng.compress.strategy.Strategies
import me.shouheng.compress.strategy.compress.Compressor
import me.shouheng.compress.strategy.config.Config
import me.shouheng.compress.strategy.config.ScaleMode
import me.shouheng.compress.strategy.luban.Luban

@DslMarker
annotation class CompressMaker

//@CompressMaker
//class CompressBuilder {
//    var context: Context? = null
//    var imageFile: File? = null
//    var imageBitmap: Bitmap? = null
//    var imageData: ByteArray? = null
//    var format: Bitmap.CompressFormat = Config.DEFAULT_COMPRESS_FORMAT
//    var quality: Int = Config.DEFAULT_COMPRESS_QUALITY
//    var targetDir: String? = null
//    var autoRecycle: Boolean = Config.DEFAULT_BITMAP_RECYCLE
//    var cacheNameFactory: CacheNameFactory? = null
//    var compressListener: CompressListener? = null
//    var algorithm: AbstractStrategy? = null
//
//    fun <T : AbstractStrategy> algorithm(algorithm: T) {
//        this.algorithm = algorithm
//    }
//
//    fun automatic(init: ConcreteBuilder.() -> Unit) {
//        val builder = ConcreteBuilder()
//        builder.apply(init)
//        algorithm = builder.build()
//    }
//
//    @RestrictTo(RestrictTo.Scope.LIBRARY) fun build(): Compress {
//        if (context == null) {
//            throw IllegalArgumentException("Context is required!")
//        }
//        if (imageFile == null && imageData == null && imageBitmap == null) {
//            throw IllegalArgumentException("Source image data (imageFile, imageData or imageBitmap) is required!")
//        }
//        imageData?.let {
//            return Compress.with(context!!, it)
//        }
//        imageBitmap?.let {
//            return Compress.Companion.with(context!!, it)
//        }
//        imageFile?.let {
//            return Compress.Companion.with(context!!, it)
//        }
//        throw IllegalStateException("no reachable")
//    }
//}
//
@CompressMaker
class ConcreteBuilder {
    var format: Bitmap.CompressFormat = Config.DEFAULT_COMPRESS_FORMAT
    var quality: Int = Config.DEFAULT_COMPRESS_QUALITY
    var autoRecycle: Boolean = Config.DEFAULT_BITMAP_RECYCLE
    var maxWidth: Float = Config.COMPRESSOR_DEFAULT_MAX_WIDTH
    var maxHeight: Float = Config.COMPRESSOR_DEFAULT_MAX_HEIGHT
    @ScaleMode var scaleMode: Int = Config.COMPRESSOR_DEFAULT_SCALE_MODE
    var config: Bitmap.Config? = null
    var ignoreIfSmaller: Boolean = true

    @RestrictTo(RestrictTo.Scope.LIBRARY) fun build(): Compressor {
        val compressor = Strategies.compressor()
        compressor.setFormat(format)
        compressor.setQuality(quality)
        compressor.setAutoRecycle(autoRecycle)
        compressor.setMaxHeight(maxWidth)
        compressor.setMaxHeight(maxHeight)
        compressor.setScaleMode(scaleMode)
        compressor.setIgnoreIfSmaller(ignoreIfSmaller)
        return compressor
    }
}

@CompressMaker
class AutomaticBuilder internal constructor() {
    var ignoreSize: Int = Config.LUBAN_DEFAULT_IGNORE_SIZE // KB
    var copyWhenIgnore: Boolean = Config.LUBAN_COPY_WHEN_IGNORE
    var format: Bitmap.CompressFormat = Config.DEFAULT_COMPRESS_FORMAT
    var quality: Int = Config.DEFAULT_COMPRESS_QUALITY
    var autoRecycle: Boolean = Config.DEFAULT_BITMAP_RECYCLE

    @RestrictTo(RestrictTo.Scope.LIBRARY) fun build(): Luban {
        val luban = Strategies.luban()
        luban.setIgnoreSize(ignoreSize, copyWhenIgnore)
        luban.setFormat(format)
        luban.setQuality(quality)
        luban.setAutoRecycle(autoRecycle)
        return luban
    }
}

/** Automatic compress algorithm based on image size. @see [automatic] */
fun Compress.automatic(): Luban {
    return algorithm(Strategies.luban())
}

fun Compress.automatic(init: AutomaticBuilder.() -> Unit): Luban {
    val builder = AutomaticBuilder()
    builder.apply(init)
    return algorithm(builder.build())
}

/** Concrete compress algorithm of size etc. */
fun Compress.concrete(): Compressor {
    return algorithm(Strategies.compressor())
}

fun Compress.concrete(init: ConcreteBuilder.() -> Unit): Compressor {
    val builder = ConcreteBuilder()
    builder.apply(init)
    return algorithm(builder.build())
}

/** Specify the compress algorithm. */
fun <T : AbstractStrategy> Compress.algorithm(algorithm: T): T {
    return this.strategy(algorithm)
}

///** Global compress function. */
//fun <T : AbstractStrategy> compress(init: CompressBuilder.() -> Unit): T {
//    val builder = CompressBuilder()
//    builder.apply(init)
//    builder.build()
//    return builder.algorithm!!
//}
