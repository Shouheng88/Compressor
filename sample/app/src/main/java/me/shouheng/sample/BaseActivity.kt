package me.shouheng.sample

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import me.shouheng.sample.utils.PermissionUtils

/**
 * Created by WngShhng on 2018/5/24. */
abstract class BaseActivity : AppCompatActivity() {

    private var onGetPermissionCallback: PermissionUtils.OnGetPermissionCallback? = null

    val context: BaseActivity
        get() = this

    fun setOnGetPermissionCallback(onGetPermissionCallback: PermissionUtils.OnGetPermissionCallback) {
        this.onGetPermissionCallback = onGetPermissionCallback
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (onGetPermissionCallback != null) {
                onGetPermissionCallback!!.onGetPermission()
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Add array length check logic to avoid ArrayIndexOutOfBoundsException
                if (permissions.isNotEmpty() && !shouldShowRequestPermissionRationale(permissions[0])) {
                    showPermissionSettingDialog(requestCode)
                } else {
                    Toast.makeText(this, getToastMessage(requestCode), Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, getToastMessage(requestCode), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPermissionSettingDialog(requestCode: Int) {
        val permissionName = PermissionUtils.getPermissionName(this, requestCode)
        val msg = String.format(getString(R.string.permission_set_permission_tips), permissionName)
        AlertDialog.Builder(this)
            .setTitle(R.string.permission_set_permission)
            .setMessage(msg)
            .setPositiveButton(R.string.permission_to_set_permission) { dialog, which -> toSetPermission() }
            .setNegativeButton(R.string.text_cancel, null)
            .create()
            .show()
    }

    private fun toSetPermission() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    override fun getPackageName(): String {
        return applicationContext.packageName
    }

    /**
     * Get the permission toast message according to request code. If the permission name can be found,
     * we will show the permission name in the message, otherwise show the default message.
     *
     * @param requestCode the request code
     * @return the message to toast
     */
    private fun getToastMessage(requestCode: Int): String {
        val permissionName = PermissionUtils.getPermissionName(this, requestCode)
        val defName = getString(R.string.permission_default_permission_name)
        return if (defName == permissionName) {
            getString(R.string.permission_denied_try_again_after_set)
        } else {
            String.format(getString(R.string.permission_denied_try_again_after_set_given_permission), permissionName)
        }
    }
}
