package me.shouheng.sample

import android.app.Application
import me.shouheng.compress.utils.LogLog
import me.shouheng.utils.UtilsApp

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        UtilsApp.init(this)
        LogLog.setDebug(true)
    }
}