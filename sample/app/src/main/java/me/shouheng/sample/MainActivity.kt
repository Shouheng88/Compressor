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
import me.shouheng.sample.databinding.ActivityMainBinding
import me.shouheng.utils.app.IntentUtils
import me.shouheng.utils.permission.Permission
import me.shouheng.utils.permission.PermissionUtils
import me.shouheng.utils.permission.callback.OnGetPermissionCallback
import me.shouheng.utils.stability.LogUtils
import me.shouheng.utils.store.FileUtils
import me.shouheng.utils.store.PathUtils
import me.shouheng.utils.ui.ToastUtils
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
    }

    fun compressor(v: View) {
        val tag = binding.ivOriginal.tag
        if (tag != null) {
            val filePath = tag as String
            val file = File(filePath)

            // connect compressor object
            LogUtils.d("Current configuration: \nConfig: $config\nScaleMode: $scaleMode")

            // add scale mode
            val compressor = Compress.with(this, file)
                .setQuality(60)
                .setTargetDir("")
                .setCompressListener(object : CompressListener {
                    override fun onStart() {
                        LogUtils.d(Thread.currentThread().toString())
                        ToastUtils.showShort("Compress Start")
                    }

                    override fun onSuccess(result: File?) {
                        LogUtils.d(Thread.currentThread().toString())
                        displayResult(result?.absolutePath)
                        ToastUtils.showShort("Compress Success : $result")
                    }

                    override fun onError(throwable: Throwable?) {
                        LogUtils.d(Thread.currentThread().toString())
                        ToastUtils.showShort("Compress Error ：$throwable")
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
                            ToastUtils.showShort("Compress Error ：$it")
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
                        LogUtils.d(Thread.currentThread().toString())
                        ToastUtils.showShort("Compress Start")
                    }

                    override fun onSuccess(result: File?) {
                        LogUtils.d(Thread.currentThread().toString())
                        displayResult(result?.absolutePath)
                        ToastUtils.showShort("Compress Success : $result")
                    }

                    override fun onError(throwable: Throwable?) {
                        LogUtils.d(Thread.currentThread().toString())
                        ToastUtils.showShort("Compress Error ：$throwable")
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
                        ToastUtils.showShort("Compress Error ：$it", Toast.LENGTH_SHORT)
                    })
            } else {
                luban.launch()
            }
        }
    }

    fun capture(v: View) {
        PermissionUtils.checkPermissions(this, OnGetPermissionCallback {
            val file = File(PathUtils.getExternalPicturesPath(), "${System.currentTimeMillis()}.jpeg")
            val succeed = FileUtils.createOrExistsFile(file)
            if (succeed) {
                originalFile = file
                val uri = Utils.getUriFromFile(this@MainActivity, file)
                startActivityForResult(IntentUtils.getCaptureIntent(uri), REQUEST_IMAGE_CAPTURE)
            } else{
                ToastUtils.showShort("Can't create file!", Toast.LENGTH_SHORT)
            }
        }, Permission.CAMERA, Permission.STORAGE)
    }

    fun choose(v: View) {
        PermissionUtils.checkStoragePermission(this) {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_SELECT_IMAGE)
        }
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
                        ToastUtils.showShort("Compress Start")
                    }

                    override fun onSuccess(result: File?) {
                        LogUtils.d(Thread.currentThread().toString())
                        displayResult(result?.absolutePath)
                        ToastUtils.showShort("Compress Success : $result")
                    }

                    override fun onError(throwable: Throwable?) {
                        LogUtils.d(Thread.currentThread().toString())
                        ToastUtils.showShort("Compress Error ：$throwable")
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
            ToastUtils.showShort("Error when displaying image info!")
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
            ToastUtils.showShort("Error when displaying image info!")
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
