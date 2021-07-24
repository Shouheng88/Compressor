package me.shouheng.sample

import android.app.Application
import me.shouheng.compress.utils.CLog
import me.shouheng.vmlib.VMLib

/**
 * The custom application
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        CLog.isDebug = true
        VMLib.onCreate(this)
    }
}