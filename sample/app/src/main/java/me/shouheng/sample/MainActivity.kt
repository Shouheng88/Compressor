package me.shouheng.sample

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import com.bumptech.glide.Glide
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.shouheng.compress.Compress
import me.shouheng.compress.listener.CompressListener
import me.shouheng.compress.strategy.ScaleMode
import me.shouheng.compress.strategy.Strategies
import me.shouheng.compress.utils.LogLog
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
    private var config = Bitmap.Config.ALPHA_8
    private var scaleMode = ScaleMode.SCALE_LARGER

    @RequiresApi(Build.VERSION_CODES.O)
    private var configArray = arrayOf(
        Bitmap.Config.ARGB_8888,
        Bitmap.Config.ALPHA_8,
        Bitmap.Config.RGB_565,
        Bitmap.Config.ARGB_4444,
        Bitmap.Config.RGBA_F16,
        Bitmap.Config.HARDWARE)

    private var scaleArray = arrayOf(
        ScaleMode.SCALE_LARGER,
        ScaleMode.SCALE_SMALLER,
        ScaleMode.SCALE_WIDTH,
        ScaleMode.SCALE_HEIGHT
    )

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_main, null, false)
        setContentView(binding.root)

        binding.ivOriginal.setOnLongClickListener {
            val tag = binding.ivOriginal.tag
            if (tag != null) {
                val filePath = tag as String
                val file = File(filePath)
                Glide.with(this@MainActivity).load(file).into(binding.ivResult)
            }
            true
        }
        binding.aspColor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    config = configArray[position]
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // nothing
            }
        }
        binding.aspScale.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // nothing
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                scaleMode = scaleArray[position]
            }
        }

        LogLog.setDebug(true)
    }

    fun compressor(v: View) {
        val tag = binding.ivOriginal.tag
        if (tag != null) {
            val filePath = tag as String
            val file = File(filePath)

            // connect compressor object
            LogLog.d("Current configuration: \nConfig: $config\nScaleMode: $scaleMode")

            // add scale mode
            val compressor = Compress.with(this, file)
                .setQuality(60)
                .setTargetDir("")
                .setCompressListener(object : CompressListener {
                    override fun onStart() {
                        LogLog.d(Thread.currentThread().toString())
                        Toast.makeText(this@MainActivity, "Compress Start", Toast.LENGTH_SHORT).show()
                    }

                    override fun onSuccess(result: File?) {
                        LogLog.d(Thread.currentThread().toString())
                        displayResult(result?.absolutePath)
                        Toast.makeText(this@MainActivity, "Compress Success : $result", Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(throwable: Throwable?) {
                        LogLog.d(Thread.currentThread().toString())
                        Toast.makeText(this@MainActivity, "Compress Error ：$throwable", Toast.LENGTH_SHORT).show()
                    }
                })
                .strategy(Strategies.compressor())
                .setConfig(config)
                .setMaxHeight(100f)
                .setMaxWidth(100f)
                .setScaleMode(scaleMode)

            // launch as flowable or luanch
            if (binding.rbCFlowable.isChecked) {
                val d = compressor
                        .asFlowable()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            displayResult(it.absolutePath)
                        }, {
                            Toast.makeText(this@MainActivity, "Compress Error ：$it", Toast.LENGTH_SHORT).show()
                        })
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
                        LogLog.d(Thread.currentThread().toString())
                        Toast.makeText(this@MainActivity, "Compress Start", Toast.LENGTH_SHORT).show()
                    }

                    override fun onSuccess(result: File?) {
                        LogLog.d(Thread.currentThread().toString())
                        displayResult(result?.absolutePath)
                        Toast.makeText(this@MainActivity, "Compress Success : $result", Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(throwable: Throwable?) {
                        LogLog.d(Thread.currentThread().toString())
                        Toast.makeText(this@MainActivity, "Compress Error ：$throwable", Toast.LENGTH_SHORT).show()
                    }
                })
                .setCacheNameFactory { System.currentTimeMillis().toString() + ".jpg" }
                .setQuality(80)
                .strategy(Strategies.luban())
                .setIgnoreSize(100, copy)

            // use as flowable or launch
            if (binding.rbFlowable.isChecked) {
                val d = luban.asFlowable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        displayResult(it.absolutePath)
                    }, {
                        Toast.makeText(this@MainActivity, "Compress Error ：$it", Toast.LENGTH_SHORT).show()
                    })
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
        PermissionUtils.checkStoragePermission(this, object : PermissionUtils.OnGetPermissionCallback {
            override fun onGetPermission() {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, REQUEST_SELECT_IMAGE)
            }
        })
    }

    fun custom(v: View) {
        val tag = binding.ivOriginal.tag
        if (tag != null) {
            val filePath = tag as String
            val file = File(filePath)

            Compress.with(this@MainActivity, file)
                .setCompressListener(object : CompressListener{
                    override fun onStart() {
                        LogLog.d(Thread.currentThread().toString())
                        Toast.makeText(this@MainActivity, "Compress Start", Toast.LENGTH_SHORT).show()
                    }

                    override fun onSuccess(result: File?) {
                        LogLog.d(Thread.currentThread().toString())
                        displayResult(result?.absolutePath)
                        Toast.makeText(this@MainActivity, "Compress Success : $result", Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(throwable: Throwable?) {
                        LogLog.d(Thread.currentThread().toString())
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
        val size = bitmap?.byteCount?:0
        binding.tvResult.text = "Result:\nwidth: $actualWidth\nheight:$actualHeight\nsize:$size"
        binding.ivResult.tag = filePath
    }

}
