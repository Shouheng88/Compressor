package me.shouheng.sample

import android.app.Application
import me.shouheng.compress.Compress
import me.shouheng.sample.custom.AssetsResource
import me.shouheng.sample.custom.AssetsResourceSource
import me.shouheng.vmlib.VMLib
import java.io.InputStream

/**
 * The custom application
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        VMLib.onCreate(this)
        Compress.setDebug(true)
        // Register custom image source type and its adapter.
        Compress.registerTypeAdapter(AssetsResource::class.java, AssetsResourceSource.Adapter())
    }
}