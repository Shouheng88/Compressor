package me.shouheng.sample

import android.app.Application
import me.shouheng.compress.utils.LogLog
import me.shouheng.utils.UtilsApp

/**
 * The custom application
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        UtilsApp.init(this)
        LogLog.setDebug(true)
    }
}