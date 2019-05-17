package me.shouheng.sample

import android.support.v7.app.AppCompatActivity
import me.shouheng.utils.permission.PermissionResultHandler
import me.shouheng.utils.permission.PermissionResultResolver
import me.shouheng.utils.permission.callback.OnGetPermissionCallback
import me.shouheng.utils.permission.callback.PermissionResultCallbackImpl

/**
 * Created by WngShhng on 2018/5/24. */
abstract class BaseActivity : AppCompatActivity(), PermissionResultResolver {

    private var permissionCallback : OnGetPermissionCallback? = null

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionResultHandler.handlePermissionsResult(this, requestCode, permissions, grantResults,
            PermissionResultCallbackImpl(this, permissionCallback))
    }

    override fun setOnGetPermissionCallback(onGetPermissionCallback: OnGetPermissionCallback?) {
        this.permissionCallback = onGetPermissionCallback
    }
}
