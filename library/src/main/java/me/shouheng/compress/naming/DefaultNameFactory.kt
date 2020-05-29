package me.shouheng.compress.naming

import android.graphics.Bitmap
import java.util.Random

/**
 * Default name factory implementation
 */
class DefaultNameFactory private constructor() : CacheNameFactory {

    override fun getFileName(format: Bitmap.CompressFormat): String {
        return (System.currentTimeMillis() + Random().nextInt()).toString() + when(format) {
            Bitmap.CompressFormat.PNG -> ".png"
            Bitmap.CompressFormat.JPEG -> ".jpeg"
            Bitmap.CompressFormat.WEBP -> ".webp"
        }
    }

    companion object {
        fun get(): DefaultNameFactory = DefaultNameFactory()
    }
}
