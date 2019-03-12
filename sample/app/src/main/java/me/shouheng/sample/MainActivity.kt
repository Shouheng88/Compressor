package me.shouheng.sample

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.shouheng.compress.Compress
import me.shouheng.compress.listener.CompressListener
import me.shouheng.compress.strategy.Configuration
import me.shouheng.compress.strategy.Strategies
import me.shouheng.compress.utils.LogUtils
import me.shouheng.sample.databinding.ActivityMainBinding
import me.shouheng.sample.utils.FileManager
import me.shouheng.sample.utils.MySimpleStrategy
import me.shouheng.sample.utils.PermissionUtils
import java.io.File

class MainActivity : BaseActivity() {

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 0x0100
        const val REQUEST_SELECT_IMAGE = 0x0101
    }

    private lateinit var originalFile: File

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_main, null, false)
        setContentView(binding.root)

        LogUtils.setDebug(true)
    }

    fun compressor(v: View) {
        val tag = binding.ivOriginal.tag
        if (tag != null) {
            val filePath = tag as String
            val file = File(filePath)

            // connect compressor object
            val compressor = Compress.with(this, file)
                .setQuality(60)
                .setTargetDir("")
                .setCompressListener(object : CompressListener {
                    override fun onStart() {
                        LogUtils.d(Thread.currentThread().toString())
                        Toast.makeText(this@MainActivity, "Compress Start", Toast.LENGTH_SHORT).show()
                    }

                    override fun onSuccess(result: File?) {
                        LogUtils.d(Thread.currentThread().toString())
                        displayResult(result?.absolutePath)
                        Toast.makeText(this@MainActivity, "Compress Success : $result", Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(throwable: Throwable?) {
                        LogUtils.d(Thread.currentThread().toString())
                        Toast.makeText(this@MainActivity, "Compress Error ：$throwable", Toast.LENGTH_SHORT).show()
                    }
                })
                .strategy(Strategies.compressor())
                .setMaxHeight(100f)
                .setMaxWidth(100f)

            // add scale mode
            if (binding.rbScaleWidth.isChecked) {
                compressor.setScaleMode(Configuration.SCALE_WIDTH)
            } else if (binding.rbScaleHeight.isChecked) {
                compressor.setScaleMode(Configuration.SCALE_HEIGHT)
            } else if (binding.rbScaleSmaller.isChecked) {
                compressor.setScaleMode(Configuration.SCALE_SMALLER)
            }

            // launch as flowable or luanch
            if (binding.rbCFlowable.isChecked) {
                val d = compressor
                        .asFlowable()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { displayResult(it.absolutePath) }
            } else {
                compressor.launch()
            }
        }
    }

    fun luban(v: View) {
        val tag = binding.ivOriginal.tag
        if (tag != null) {
            val filePath = tag as String
            val file = File(filePath)
            val copy = binding.scCopy.isChecked

            // connect luban object
            val luban = Compress.with(this, file)
                .setCompressListener(object : CompressListener{
                    override fun onStart() {
                        LogUtils.d(Thread.currentThread().toString())
                        Toast.makeText(this@MainActivity, "Compress Start", Toast.LENGTH_SHORT).show()
                    }

                    override fun onSuccess(result: File?) {
                        LogUtils.d(Thread.currentThread().toString())
                        displayResult(result?.absolutePath)
                        Toast.makeText(this@MainActivity, "Compress Success : $result", Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(throwable: Throwable?) {
                        LogUtils.d(Thread.currentThread().toString())
                        Toast.makeText(this@MainActivity, "Compress Error ：$throwable", Toast.LENGTH_SHORT).show()
                    }
                })
                .setCacheNameFactory { System.currentTimeMillis().toString() }
                .setQuality(80)
                .strategy(Strategies.luban())
                .setIgnoreSize(100, copy)

            // use as flowable or launch
            if (binding.rbFlowable.isChecked) {
                val d = luban.asFlowable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { displayResult(it.absolutePath) }
            } else {
                luban.launch()
            }
        }
    }

    fun capture(v: View) {
        val file = FileManager.createAttachment(this, ".png")
        if (file != null) {
            originalFile = file
            PermissionUtils.checkCameraPermission(this, object : PermissionUtils.OnGetPermissionCallback {
                override fun onGetPermission() {
                    val uri = FileManager.getUriFromFile(this@MainActivity, file)
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
                }
            })
        } else{
            Toast.makeText(this, "Can't create file!", Toast.LENGTH_SHORT).show()
        }
    }

    fun choose(v: View) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_SELECT_IMAGE)
    }

    fun custom(v: View) {
        val tag = binding.ivOriginal.tag
        if (tag != null) {
            val filePath = tag as String
            val file = File(filePath)

            Compress.with(this@MainActivity, file)
                .setCompressListener(object : CompressListener{
                    override fun onStart() {
                        LogUtils.d(Thread.currentThread().toString())
                        Toast.makeText(this@MainActivity, "Compress Start", Toast.LENGTH_SHORT).show()
                    }

                    override fun onSuccess(result: File?) {
                        LogUtils.d(Thread.currentThread().toString())
                        displayResult(result?.absolutePath)
                        Toast.makeText(this@MainActivity, "Compress Success : $result", Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(throwable: Throwable?) {
                        LogUtils.d(Thread.currentThread().toString())
                        Toast.makeText(this@MainActivity, "Compress Error ：$throwable", Toast.LENGTH_SHORT).show()
                    }
                })
                .setQuality(80)
                .strategy(MySimpleStrategy())
                .launch()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                if (resultCode == Activity.RESULT_OK) {
                    displayOriginal(originalFile.absolutePath)
                }
            }
            REQUEST_SELECT_IMAGE -> {
                if (data != null) {
                    val uri = data.data
                    val cursor = contentResolver.query(uri!!, arrayOf(MediaStore.Images.Media.DATA), null, null, null)
                    cursor?.moveToFirst()
                    val index = cursor?.getColumnIndex(MediaStore.Images.Media.DATA)
                    val path = if (index == null) { null } else cursor.getString(index)
                    cursor?.close()
                    displayOriginal(path)
                }
            }
        }
    }

    private fun displayOriginal(filePath: String?) {
        if (TextUtils.isEmpty(filePath)) {
            Toast.makeText(this, "Error when displaying image info!", Toast.LENGTH_SHORT).show()
            return
        }
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath, options)
        val actualWidth = options.outWidth
        val actualHeight = options.outHeight
        options.inJustDecodeBounds = false
        val bitmap = BitmapFactory.decodeFile(filePath, options)
        binding.ivOriginal.setImageBitmap(bitmap)
        val size = bitmap.byteCount
        binding.tvOriginal.text = "Original:\nwidth: $actualWidth\nheight:$actualHeight\nsize:$size"
        binding.ivOriginal.tag = filePath
    }

    private fun displayResult(filePath: String?) {
        if (TextUtils.isEmpty(filePath)) {
            Toast.makeText(this, "Error when displaying image info!", Toast.LENGTH_SHORT).show()
            return
        }
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath, options)
        val actualWidth = options.outWidth
        val actualHeight = options.outHeight
        options.inJustDecodeBounds = false
        val bitmap = BitmapFactory.decodeFile(filePath, options)
        binding.ivResult.setImageBitmap(bitmap)
        val size = bitmap.byteCount
        binding.tvResult.text = "Result:\nwidth: $actualWidth\nheight:$actualHeight\nsize:$size"
        binding.ivResult.tag = filePath
    }

}
