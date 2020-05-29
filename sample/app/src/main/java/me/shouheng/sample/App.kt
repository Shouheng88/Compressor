package me.shouheng.sample

import android.app.Application
import android.content.Context
import me.shouheng.compress.utils.CLog
import me.shouheng.mvvm.MVVMs

/**
 * The custom application
 */
class App : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MVVMs.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        CLog.isDebug = true
        MVVMs.onCreate(this)
    }
}