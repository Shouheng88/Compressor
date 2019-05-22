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
import me.shouheng.compress.RequestBuilder
import me.shouheng.compress.listener.CompressListener
import me.shouheng.compress.strategy.Strategies
import me.shouheng.compress.strategy.config.ScaleMode
import me.shouheng.sample.databinding.ActivityMainBinding
import me.shouheng.utils.app.ActivityHelper
import me.shouheng.utils.app.IntentUtils
import me.shouheng.utils.permission.Permission
import me.shouheng.utils.permission.PermissionUtils
import me.shouheng.utils.permission.callback.OnGetPermissionCallback
import me.shouheng.utils.stability.LogUtils
import me.shouheng.utils.store.FileUtils
import me.shouheng.utils.store.IOUtils
import me.shouheng.utils.store.PathUtils
import me.shouheng.utils.ui.ToastUtils
import java.io.File

class MainActivity : BaseActivity() {

    companion object {
        const val REQUEST_IMAGE_CAPTURE     = 0x0100
        const val REQUEST_SELECT_IMAGE      = 0x0101
    }

    private lateinit var originalFile: File

    /* configuration for compressor */
    private var config = Bitmap.Config.ALPHA_8
    private var scaleMode = ScaleMode.SCALE_LARGER
    private var compressorSourceType = SourceType.FILE
    private var compressorLaunchType = LaunchType.LAUNCH
    private var compressorResultType = ResultType.FILE

    /* configuration for luban */
    private var lubanSourceType = SourceType.FILE
    private var lubanLaunchType = LaunchType.LAUNCH
    private var lubanResultType = ResultType.FILE

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
        ScaleMode.SCALE_HEIGHT)
    private var sourceTypeArray = arrayOf(SourceType.FILE, SourceType.BYTE_ARRAY, SourceType.BITMAP)
    private var resultTypeArray = arrayOf(ResultType.FILE, ResultType.BITMAP)
    private var launchTypeArray = arrayOf(LaunchType.LAUNCH, LaunchType.AS_FLOWABLE, LaunchType.GET)

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_main, null, false)
        setContentView(binding.root)
        configViews()
    }

    private fun configViews() {
        binding.ivOriginal.setOnLongClickListener {
            val tag = binding.ivOriginal.tag
            if (tag != null) {
                val filePath = tag as String
                val file = File(filePath)
                Glide.with(this@MainActivity).load(file).into(binding.ivResult)
            }
            true
        }
        binding.btnCapture.setOnClickListener {
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
        binding.btnChoose.setOnClickListener {
            PermissionUtils.checkStoragePermission(this) {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, REQUEST_SELECT_IMAGE)
            }
        }
        binding.btnSample.setOnClickListener {
            ActivityHelper.start(this@MainActivity, SampleActivity::class.java)
        }

        /* configuration for luban */
        binding.aspLubanSourceType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // nothing
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                lubanSourceType = sourceTypeArray[position]
            }
        }
        binding.aspLubanResult.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // nothing
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                lubanResultType = resultTypeArray[position]
            }
        }
        binding.aspLubanLaunch.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // nothing
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                lubanLaunchType = launchTypeArray[position]
            }
        }
        binding.btnLuban.setOnClickListener {
            compressByLuban()
        }

        /* configuration for compressor */
        binding.aspSourceType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // nothing
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                compressorSourceType = sourceTypeArray[position]
            }
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
        binding.aspCompressorResult.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // empty
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                compressorResultType = resultTypeArray[position]
            }
        }
        binding.aspCompressorLaunch.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // do nothing
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                compressorLaunchType = launchTypeArray[position]
            }
        }
        binding.btnCompressor.setOnClickListener {
            compressByCompressor()
        }

        /* configuration for custom */
        binding.btnCustom.setOnClickListener {
            compressByCustom()
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

    private fun compressByCompressor() {
        val tag = binding.ivOriginal.tag
        if (tag != null) {
            val filePath = tag as String
            val file = File(filePath)
            // connect compressor object
            LogUtils.d("Current configuration: \nConfig: $config\nScaleMode: $scaleMode")

            var srcBitmap: Bitmap? = null
            val compress: Compress
            compress = when(compressorSourceType) {
                SourceType.FILE -> {
                    Compress.with(this, file)
                }
                SourceType.BYTE_ARRAY -> {
                    val byteArray = IOUtils.readFile2BytesByStream(file)
                    Compress.with(this, byteArray)
                }
                SourceType.BITMAP -> {
                    srcBitmap = BitmapFactory.decodeFile(filePath)
                    Compress.with(this, srcBitmap)
                }
            }

            // add scale mode
            val compressor = compress
                .setQuality(60)
                .setTargetDir(PathUtils.getExternalPicturesPath())
                .setCompressListener(object : CompressListener {
                    override fun onStart() {
                        LogUtils.d(Thread.currentThread().toString())
                        ToastUtils.showShort("Start [Compressor,File]")
                    }

                    override fun onSuccess(result: File?) {
                        LogUtils.d(Thread.currentThread().toString())
                        displayResult(result?.absolutePath)
                        ToastUtils.showShort("Success [Compressor,File] : $result")
                    }

                    override fun onError(throwable: Throwable?) {
                        LogUtils.d(Thread.currentThread().toString())
                        ToastUtils.showShort("Error [Compressor,File] : $throwable")
                    }
                })
                .strategy(Strategies.compressor())
                .setConfig(config)
                .setMaxHeight(100f)
                .setMaxWidth(100f)
                .setScaleMode(scaleMode)

            // launch according to given launch type
            when(compressorResultType) {
                ResultType.FILE -> {
                    when(compressorLaunchType) {
                        LaunchType.LAUNCH -> {
                            compressor.launch()
                        }
                        LaunchType.AS_FLOWABLE -> {
                            val d = compressor
                                .asFlowable()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({
                                    ToastUtils.showShort("Success [Compressor,File,Flowable] $it")
                                    displayResult(it.absolutePath)
                                }, {
                                    ToastUtils.showShort("Error [Compressor,File,Flowable] : $it")
                                })
                        }
                        LaunchType.GET -> {
                            val resultFile = compressor.get()
                            ToastUtils.showShort("Success [Compressor,File,Get] $resultFile")
                            displayResult(resultFile.absolutePath)
                        }
                    }
                }
                ResultType.BITMAP -> {
                    val bitmapBuilder = compressor.asBitmap()
                    when(compressorLaunchType) {
                        LaunchType.LAUNCH -> {
                            bitmapBuilder
                                .setCompressListener(object : RequestBuilder.Callback<Bitmap> {
                                    override fun onStart() {
                                        LogUtils.d(Thread.currentThread().toString())
                                        ToastUtils.showShort("Start [Compressor,Bitmap,Launch]")
                                    }

                                    override fun onSuccess(result: Bitmap?) {
                                        LogUtils.d(Thread.currentThread().toString())
                                        displayResult(result)
                                        ToastUtils.showShort("Success [Compressor,Bitmap,Launch] : $result")
                                    }

                                    override fun onError(throwable: Throwable?) {
                                        LogUtils.d(Thread.currentThread().toString())
                                        ToastUtils.showShort("Error [Compressor,Bitmap,Launch] : $throwable")
                                    }
                                })
                                .launch()
                        }
                        LaunchType.AS_FLOWABLE -> {
                            val d = bitmapBuilder
                                .asFlowable()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({
                                    ToastUtils.showShort("Success [Compressor,Bitmap,Flowable] $it")
                                    displayResult(it)
                                }, {
                                    ToastUtils.showShort("Error [Compressor,Bitmap,Flowable] : $it")
                                })
                        }
                        LaunchType.GET -> {
                            val bitmap = bitmapBuilder.get()
                            ToastUtils.showShort("Success [Compressor,Bitmap,Get] $bitmap")
                            displayResult(bitmap)
                            // this will cause a crash when it's automatically recycling the source bitmap
                            binding.ivOriginal.setImageBitmap(srcBitmap)
                        }
                    }
                }
            }
        }
    }

    private fun compressByLuban() {
        val tag = binding.ivOriginal.tag
        if (tag != null) {
            val filePath = tag as String
            val file = File(filePath)
            val copy = binding.scCopy.isChecked

            val compress: Compress
            compress = when(lubanSourceType) {
                SourceType.FILE -> {
                    Compress.with(this, file)
                }
                SourceType.BYTE_ARRAY -> {
                    val byteArray = IOUtils.readFile2BytesByStream(file)
                    Compress.with(this, byteArray)
                }
                SourceType.BITMAP -> {
                    val bitmap = BitmapFactory.decodeFile(filePath)
                    Compress.with(this, bitmap)
                }
            }

            // connect luban object
            val luban = compress
                .setCompressListener(object : CompressListener{
                    override fun onStart() {
                        LogUtils.d(Thread.currentThread().toString())
                        ToastUtils.showShort("Start [Luban,File]")
                    }

                    override fun onSuccess(result: File?) {
                        LogUtils.d(Thread.currentThread().toString())
                        displayResult(result?.absolutePath)
                        ToastUtils.showShort("Success [Luban,File] : $result")
                    }

                    override fun onError(throwable: Throwable?) {
                        LogUtils.d(Thread.currentThread().toString())
                        ToastUtils.showShort("Error [Luban,File] : $throwable")
                    }
                })
                .setCacheNameFactory { System.currentTimeMillis().toString() + ".jpg" }
                .setQuality(80)
                .strategy(Strategies.luban())
                .setIgnoreSize(100, copy)

            when(lubanResultType) {
                ResultType.FILE -> {
                    // launch according to given launch type
                    when(lubanLaunchType) {
                        LaunchType.LAUNCH -> {
                            luban.launch()
                        }
                        LaunchType.AS_FLOWABLE -> {
                            val d = luban.asFlowable()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({
                                    ToastUtils.showShort("Start [Luban,File,Flowable]")
                                    displayResult(it.absolutePath)
                                }, {
                                    ToastUtils.showShort("Error [Luban,File,Flowable] : $it")
                                })
                        }
                        LaunchType.GET -> {
                            val resultFile = luban.get()
                            ToastUtils.showShort("Success [Luban,File,Get] $resultFile")
                            displayResult(resultFile.absolutePath)
                        }
                    }
                }
                ResultType.BITMAP -> {
                    val bitmapBuilder = luban.asBitmap()
                    // launch according to given launch type
                    when(lubanLaunchType) {
                        LaunchType.LAUNCH -> {
                            bitmapBuilder
                                .setCompressListener(object : RequestBuilder.Callback<Bitmap> {
                                    override fun onStart() {
                                        LogUtils.d(Thread.currentThread().toString())
                                        ToastUtils.showShort("Start [Luban,Bitmap,Launch]")
                                    }

                                    override fun onSuccess(result: Bitmap?) {
                                        LogUtils.d(Thread.currentThread().toString())
                                        displayResult(result)
                                        ToastUtils.showShort("Success [Luban,Bitmap,Launch] : $result")
                                    }

                                    override fun onError(throwable: Throwable?) {
                                        LogUtils.d(Thread.currentThread().toString())
                                        ToastUtils.showShort("Error [Luban,Bitmap,Launch] : $throwable")
                                    }
                                })
                                .launch()
                        }
                        LaunchType.AS_FLOWABLE -> {
                            val d = bitmapBuilder.asFlowable()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({
                                    ToastUtils.showShort("Success [Luban,Bitmap,Flowable] : $it")
                                    displayResult(it)
                                }, {
                                    ToastUtils.showShort("Error [Luban,Bitmap,Flowable] : $it", Toast.LENGTH_SHORT)
                                })
                        }
                        LaunchType.GET -> {
                            val bitmap = bitmapBuilder.get()
                            ToastUtils.showShort("Success [Luban,Bitmap,Get] $bitmap")
                            displayResult(bitmap)
                        }
                    }
                }
            }
        }
    }

    private fun compressByCustom() {
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
        options.inJustDecodeBounds = false
        val bitmap = BitmapFactory.decodeFile(filePath, options)
        displayResult(bitmap)
        binding.ivResult.tag = filePath
    }

    private fun displayResult(bitmap: Bitmap?) {
        if (bitmap == null) {
            return
        }
        val actualWidth = bitmap.width
        val actualHeight = bitmap.height
        binding.ivResult.setImageBitmap(bitmap)
        val size = bitmap.byteCount
        binding.tvResult.text = "Result:\nwidth: $actualWidth\nheight:$actualHeight\nsize:$size"
    }

    /**
     * Compress task launch type
     */
    enum class LaunchType {
        LAUNCH,
        AS_FLOWABLE,
        GET
    }

    /**
     * Compress data source type
     */
    enum class SourceType {
        FILE,
        BYTE_ARRAY,
        BITMAP
    }

    /**
     * Result type
     */
    enum class ResultType {
        BITMAP,
        FILE
    }
}
