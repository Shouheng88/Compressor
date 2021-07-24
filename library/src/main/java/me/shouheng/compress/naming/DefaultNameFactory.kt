package me.shouheng.compress.naming

import android.graphics.Bitmap
import java.util.Random

/** Default name factory implementation. */
object DefaultNameFactory : CacheNameFactory {

    override fun getFileName(format: Bitmap.CompressFormat): String {
        return (System.currentTimeMillis() + Random().nextInt()).toString() + when(format) {
            Bitmap.CompressFormat.PNG -> ".png"
            Bitmap.CompressFormat.JPEG -> ".jpeg"
            Bitmap.CompressFormat.WEBP -> ".webp"
        }
    }
}
