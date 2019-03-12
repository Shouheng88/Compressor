package me.shouheng.sample.utils

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import me.shouheng.sample.BaseActivity
import me.shouheng.sample.R

/**
 * Created by WngShhng on 2017/12/5. */
object PermissionUtils {

    private const val REQUEST_STORAGE = 0x0001
    private const val REQUEST_PHONE_STATE = 0x0002
    private const val REQUEST_LOCATION = 0x0003
    private const val REQUEST_MICROPHONE = 0x0004
    private const val REQUEST_SMS = 0x0005
    private const val REQUEST_SENSORS = 0x0006
    private const val REQUEST_CONTACTS = 0x0007
    private const val REQUEST_CAMERA = 0x0008
    private const val REQUEST_CALENDAR = 0x0009

    fun <T : BaseActivity> checkStoragePermission(activity: T, callback: OnGetPermissionCallback) {
        checkPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_STORAGE, callback)
    }

    fun <T : BaseActivity> checkPhonePermission(activity: T, callback: OnGetPermissionCallback) {
        checkPermission(activity, Manifest.permission.READ_PHONE_STATE, REQUEST_PHONE_STATE, callback)
    }

    fun <T : BaseActivity> checkLocationPermission(activity: T, callback: OnGetPermissionCallback) {
        checkPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_LOCATION, callback)
    }

    fun <T : BaseActivity> checkRecordPermission(activity: T, callback: OnGetPermissionCallback) {
        checkPermission(activity, Manifest.permission.RECORD_AUDIO, REQUEST_MICROPHONE, callback)
    }

    fun <T : BaseActivity> checkSmsPermission(activity: T, callback: OnGetPermissionCallback) {
        checkPermission(activity, Manifest.permission.SEND_SMS, REQUEST_SMS, callback)
    }

    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    fun <T : BaseActivity> checkSensorsPermission(activity: T, callback: OnGetPermissionCallback) {
        checkPermission(activity, Manifest.permission.BODY_SENSORS, REQUEST_SENSORS, callback)
    }

    fun <T : BaseActivity> checkContactsPermission(activity: T, callback: OnGetPermissionCallback) {
        checkPermission(activity, Manifest.permission.READ_CONTACTS, REQUEST_CONTACTS, callback)
    }

    fun <T : BaseActivity> checkCameraPermission(activity: T, callback: OnGetPermissionCallback) {
        checkPermission(activity, Manifest.permission.CAMERA, REQUEST_CAMERA, callback)
    }

    fun <T : BaseActivity> checkCalendarPermission(activity: T, callback: OnGetPermissionCallback) {
        checkPermission(activity, Manifest.permission.READ_CALENDAR, REQUEST_CALENDAR, callback)
    }

    private fun <T : BaseActivity> checkPermission(
        activity: T,
        permission: String,
        requestCode: Int,
        callback: OnGetPermissionCallback?
    ) {
        activity.setOnGetPermissionCallback(callback!!)
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
        } else {
            callback?.onGetPermission()
        }
    }

    fun getPermissionName(context: Context, requestCode: Int): String {
        when (requestCode) {
            REQUEST_STORAGE -> return context.getString(R.string.permission_storage_permission)
            REQUEST_LOCATION -> return context.getString(R.string.permission_location_permission)
            REQUEST_MICROPHONE -> return context.getString(R.string.permission_microphone_permission)
            REQUEST_PHONE_STATE -> return context.getString(R.string.permission_phone_permission)
            REQUEST_SMS -> return context.getString(R.string.permission_sms_permission)
            REQUEST_SENSORS -> return context.getString(R.string.permission_sensor_permission)
            REQUEST_CONTACTS -> return context.getString(R.string.permission_contacts_permission)
            REQUEST_CAMERA -> return context.getString(R.string.permission_camera_permission)
            REQUEST_CALENDAR -> return context.getString(R.string.permission_calendar_permission)
        }
        return context.getString(R.string.permission_default_permission_name)
    }

    interface OnGetPermissionCallback {
        fun onGetPermission()
    }
}