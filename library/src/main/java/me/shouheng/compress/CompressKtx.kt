package me.shouheng.compress

import android.graphics.Bitmap
import androidx.annotation.IntRange
import androidx.annotation.RestrictTo
import me.shouheng.compress.strategy.Strategies
import me.shouheng.compress.strategy.compress.Compressor
import me.shouheng.compress.strategy.config.Config
import me.shouheng.compress.strategy.config.ScaleMode
import me.shouheng.compress.strategy.luban.Luban

@DslMarker annotation class CompressMaker

abstract class AlgorithmBuilder {
    protected var format: Bitmap.CompressFormat   = Config.DEFAULT_COMPRESS_FORMAT
    protected var quality: Int                    = Config.DEFAULT_COMPRESS_QUALITY
    protected var autoRecycle: Boolean            = Config.DEFAULT_BITMAP_RECYCLE

    fun withFormat(format: Bitmap.CompressFormat) {
        this.format = format
    }

    fun withQuality(@IntRange(from = 0, to = 100) quality: Int) {
        this.quality = quality
    }

    fun withAutoRecycle(autoRecycle: Boolean) {
        this.autoRecycle = autoRecycle
    }
}

@CompressMaker class ConcreteBuilder internal constructor(): AlgorithmBuilder() {
    private var maxWidth: Float                 = Config.COMPRESSOR_DEFAULT_MAX_WIDTH
    private var maxHeight: Float                = Config.COMPRESSOR_DEFAULT_MAX_HEIGHT
    @ScaleMode private var scaleMode: Int       = Config.COMPRESSOR_DEFAULT_SCALE_MODE
    private var config: Bitmap.Config?          = null
    private var ignoreIfSmaller: Boolean        = true

    fun withMaxWidth(maxWidth: Float ) {
        this.maxWidth = maxWidth
    }

    fun withMaxHeight(maxHeight: Float) {
        this.maxHeight = maxHeight
    }

    /** The scale mode. See [ScaleMode]. */
    fun withScaleMode(@ScaleMode scaleMode: Int) {
        this.scaleMode = scaleMode
    }

    fun withBitmapConfig(config: Bitmap.Config) {
        this.config = config
    }

    /** Don't compress if the size of source bitmap is smaller than desired size. */
    fun withIgnoreIfSmaller(ignoreIfSmaller: Boolean) {
        this.ignoreIfSmaller = ignoreIfSmaller
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY) fun build(): Compressor {
        val compressor = Strategies.compressor()
        compressor.setFormat(format)
        compressor.setQuality(quality)
        compressor.setAutoRecycle(autoRecycle)
        compressor.setMaxWidth(maxWidth)
        compressor.setMaxHeight(maxHeight)
        compressor.setScaleMode(scaleMode)
        compressor.setIgnoreIfSmaller(ignoreIfSmaller)
        return compressor
    }
}

@CompressMaker class AutomaticBuilder internal constructor(): AlgorithmBuilder() {
    private var ignoreSize: Int                 = Config.LUBAN_DEFAULT_IGNORE_SIZE // KB
    private var copyWhenIgnore: Boolean         = Config.LUBAN_COPY_WHEN_IGNORE

    fun withIgnoreSize(ignoreSize: Int) {
        this.ignoreSize = ignoreSize
    }

    /**
     * Copy the source bitmap to given destination if it's
     * smaller than [ignoreSize] and [copyWhenIgnore] is true.
     */
    fun withCopyWhenIgnore(copyWhenIgnore: Boolean) {
        this.copyWhenIgnore = copyWhenIgnore
    }

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
