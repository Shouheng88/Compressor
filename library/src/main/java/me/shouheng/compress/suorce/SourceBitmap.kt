package me.shouheng.compress.suorce

import android.graphics.Bitmap
import java.io.File

/** The source data. */
interface SourceData<T> {

    /** Real data. */
    fun data(): T

    /** Is source data of bitmap. */
    fun isBitmap(): Boolean
}

/** Abs source data. */
abstract class AbsSourceData<T>(private val data: T) : SourceData<T> {

    override fun data(): T = data

    override fun isBitmap(): Boolean = data is Bitmap
}

class FileSourceData(file: File): AbsSourceData<File>(file)

class ByteArraySourceData(bytes: ByteArray): AbsSourceData<ByteArray>(bytes)

class BitmapSourceData(bitmap: Bitmap): AbsSourceData<Bitmap>(bitmap)

/** The origin bitmap. */
data class SourceBitmap(

    /** The origin bitmap. */
    val bitmap: Bitmap,

    /**
     * Does the origin bitmap need be scaled. For example, bitmap decoded by sample
     * from file system or byte array don't need to scale again.
     */
    val needScale: Boolean
)
