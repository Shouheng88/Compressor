package me.shouheng.sample

import android.content.Context
import android.net.Uri
import android.os.Build
import android.support.v4.content.FileProvider
import java.io.File

class Utils {

    companion object {
        fun getUriFromFile(ctx: Context, file: File): Uri {
            return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                FileProvider.getUriForFile(ctx, BuildConfig.APPLICATION_ID + ".provider", file)
            } else {
                Uri.fromFile(file)
            }
        }
    }
}