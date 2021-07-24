package me.shouheng.sample.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import me.shouheng.sample.BuildConfig
import me.shouheng.sample.data.REQUEST_SELECT_IMAGE
import me.shouheng.utils.ktx.checkStoragePermission
import me.shouheng.vmlib.base.CommonActivity
import java.io.File

/** Get uri of file. */
fun File.uri(ctx: Context): Uri {
    return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
        FileProvider.getUriForFile(ctx, BuildConfig.APPLICATION_ID + ".provider", this)
    } else {
        Uri.fromFile(this)
    }
}

/** Get path from uri. */
fun Uri?.getPath(context: Context): String? {
    this ?: return null
    val cursor = context.contentResolver.query(this,
        arrayOf(MediaStore.Images.Media.DATA),
        null, null, null)
    cursor?.moveToFirst()
    val index = cursor?.getColumnIndex(MediaStore.Images.Media.DATA)
    val path = if (index == null) null else cursor.getString(index)
    cursor?.close()
    return path
}

/** Start a capture activity. */
fun CommonActivity<*, *>.chooseFromAlbum() {
    checkStoragePermission {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_SELECT_IMAGE)
    }
}
