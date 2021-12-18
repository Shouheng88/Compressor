package me.shouheng.sample.custom

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import me.shouheng.compress.strategy.IImageSource
import me.shouheng.compress.strategy.Size
import me.shouheng.compress.utils.copy
import me.shouheng.compress.utils.pixelSize
import me.shouheng.utils.app.ResUtils
import java.io.File
import java.io.FileOutputStream

data class AssetsResource(val name: String)

/**
 * Image source for [AssetsResource].
 *
 * @Author wangshouheng
 * @Time 2021/12/18
 */
class AssetsResourceSource(private val res: AssetsResource): IImageSource<AssetsResource> {

    override fun getSize(): Size {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        options.inSampleSize = 1
        val ins = ResUtils.getAssets().open(res.name)
        BitmapFactory.decodeStream(ins, null, options)
        return Size(options.outWidth, options.outHeight)
    }

    override fun getOriginBitmapByOptions(
        options: BitmapFactory.Options
    ): Bitmap? {
        val ins = ResUtils.getAssets().open(res.name)
        return BitmapFactory.decodeStream(ins, null, options)
    }

    override fun shouldIgnoreForSize(ignoreSize: Int): Boolean {
        val ins = ResUtils.getAssets().open(res.name)
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        options.inSampleSize = 1
        BitmapFactory.decodeStream(ins, null, options)
        val size = options.outWidth*options.outHeight*options.pixelSize()
        return size < ignoreSize shl 10
    }

    override fun getRotation(): Int = 0

    override fun copyTo(
        dest: File,
        format: Bitmap.CompressFormat,
        quality: Int
    ): Boolean {
        val ins = ResUtils.getAssets().open(res.name)
        return copy(ins, FileOutputStream(dest))
    }

    class Adapter: IImageSource.Adapter<AssetsResource> {
        override fun get(
            context: Context, source: AssetsResource
        ): IImageSource<AssetsResource> = AssetsResourceSource(source)
    }
}