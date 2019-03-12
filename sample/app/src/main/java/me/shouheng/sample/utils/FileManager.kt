package me.shouheng.sample.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.support.v4.content.FileProvider
import me.shouheng.sample.BuildConfig
import java.io.File

class FileManager {

    companion object {
        fun getUriFromFile(ctx: Context, file: File): Uri {
            return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                FileProvider.getUriForFile(ctx, BuildConfig.APPLICATION_ID + ".provider", file)
            } else {
                Uri.fromFile(file)
            }
        }

        fun createAttachment(ctx: Context, extension: String): File? {
            if (isStorageWritable()) {
                return File(ctx.getExternalFilesDir(null), System.currentTimeMillis().toString() + extension)
            }
            return null
        }

        private fun isStorageWritable(): Boolean {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state
        }
    }

}