package me.shouheng.sample

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import com.bumptech.glide.Glide
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.shouheng.compress.Compress
import me.shouheng.compress.RequestBuilder
import me.shouheng.compress.listener.CompressListener
import me.shouheng.compress.naming.CacheNameFactory
import me.shouheng.compress.strategy.Strategies
import me.shouheng.compress.strategy.config.ScaleMode
import me.shouheng.compress.utils.CImageUtils
import me.shouheng.mvvm.base.CommonActivity
import me.shouheng.mvvm.base.anno.ActivityConfiguration
import me.shouheng.mvvm.comn.EmptyViewModel
import me.shouheng.sample.databinding.ActivityMainBinding
import me.shouheng.utils.app.IntentUtils
import me.shouheng.utils.permission.Permission
import me.shouheng.utils.permission.PermissionUtils
import me.shouheng.utils.permission.callback.OnGetPermissionCallback
import me.shouheng.utils.stability.L
import me.shouheng.utils.store.FileUtils
import me.shouheng.utils.store.IOUtils
import me.shouheng.utils.store.PathUtils
import java.io.File

@ActivityConfiguration(layoutResId = R.layout.activity_main)
class MainActivity : CommonActivity<ActivityMainBinding, EmptyViewModel>() {

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

    private var configArray = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        arrayOf(
            Bitmap.Config.ARGB_8888,
            Bitmap.Config.ALPHA_8,
            Bitmap.Config.RGB_565,
            Bitmap.Config.ARGB_4444,
            Bitmap.Config.RGBA_F16,
            Bitmap.Config.HARDWARE)
    } else {
        arrayOf(
            Bitmap.Config.ARGB_8888,
            Bitmap.Config.ALPHA_8,
            Bitmap.Config.RGB_565,
            Bitmap.Config.ARGB_4444)
    }
    private var scaleArray = arrayOf(
        ScaleMode.SCALE_LARGER,
        ScaleMode.SCALE_SMALLER,
        ScaleMode.SCALE_WIDTH,
        ScaleMode.SCALE_HEIGHT)
    private var sourceTypeArray = arrayOf(SourceType.FILE, SourceType.BYTE_ARRAY, SourceType.BITMAP)
    private var resultTypeArray = arrayOf(ResultType.FILE, ResultType.BITMAP)
    private var launchTypeArray = arrayOf(LaunchType.LAUNCH, LaunchType.AS_FLOWABLE, LaunchType.GET, LaunchType.COROUTINES)

    override fun doCreateView(savedInstanceState: Bundle?) {
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
                    toast("Can't create file!")
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
            startActivity(SampleActivity::class.java)
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
            L.d("Current configuration: \nConfig: $config\nScaleMode: $scaleMode")

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
                        L.d(Thread.currentThread().toString())
                        toast("Start [Compressor,File]")
                    }

                    override fun onSuccess(result: File) {
                        L.d(Thread.currentThread().toString())
                        displayResult(result.absolutePath)
                        toast("Success [Compressor,File] : $result")
                    }

                    override fun onError(throwable: Throwable) {
                        L.d(Thread.currentThread().toString())
                        toast("Error [Compressor,File] : $throwable")
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
                                    toast("Success [Compressor,File,Flowable] $it")
                                    displayResult(it.absolutePath)
                                }, {
                                    toast("Error [Compressor,File,Flowable] : $it")
                                })
                        }
                        LaunchType.GET -> {
                            val resultFile = compressor.get()
                            toast("Success [Compressor,File,Get] $resultFile")
                            displayResult(resultFile.absolutePath)
                        }
                        LaunchType.COROUTINES -> {
                            GlobalScope.launch {
                                val resultFile = compressor.get(Dispatchers.IO)
                                toast("Success [Luban,File,Kt,Get] $resultFile")
                                withContext(Dispatchers.Main) {
                                    displayResult(resultFile.absolutePath)
                                }
                            }
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
                                        L.d(Thread.currentThread().toString())
                                        toast("Start [Compressor,Bitmap,Launch]")
                                    }

                                    override fun onSuccess(result: Bitmap) {
                                        L.d(Thread.currentThread().toString())
                                        displayResult(result)
                                        toast("Success [Compressor,Bitmap,Launch] : $result")
                                    }

                                    override fun onError(throwable: Throwable) {
                                        L.d(Thread.currentThread().toString())
                                        toast("Error [Compressor,Bitmap,Launch] : $throwable")
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
                                    toast("Success [Compressor,Bitmap,Flowable] $it")
                                    displayResult(it)
                                }, {
                                    toast("Error [Compressor,Bitmap,Flowable] : $it")
                                })
                        }
                        LaunchType.GET -> {
                            val bitmap = bitmapBuilder.get()
                            toast("Success [Compressor,Bitmap,Get] $bitmap")
                            displayResult(bitmap)
                            // this will cause a crash when it's automatically recycling the source bitmap
                            binding.ivOriginal.setImageBitmap(srcBitmap)
                        }
                        LaunchType.COROUTINES -> {
                            GlobalScope.launch {
                                val bitmap = bitmapBuilder.get(Dispatchers.IO)
                                toast("Success [Luban,Bitmap,Kt,Get] $bitmap")
                                withContext(Dispatchers.Main) {
                                    displayResult(bitmap)
                                }
                            }
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
                        L.d(Thread.currentThread().toString())
                        toast("Start [Luban,File]")
                    }

                    override fun onSuccess(result: File) {
                        L.d(Thread.currentThread().toString())
                        displayResult(result?.absolutePath)
                        toast("Success [Luban,File] : $result")
                    }

                    override fun onError(throwable: Throwable) {
                        L.d(Thread.currentThread().toString())
                        toast("Error [Luban,File] : $throwable")
                    }
                })
                .setCacheNameFactory(object : CacheNameFactory {
                    override fun getFileName(format: Bitmap.CompressFormat): String {
                        return System.currentTimeMillis().toString() + ".jpg"
                    }
                })
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
                                    toast("Start [Luban,File,Flowable]")
                                    displayResult(it.absolutePath)
                                }, {
                                    toast("Error [Luban,File,Flowable] : $it")
                                })
                        }
                        LaunchType.GET -> {
                            val resultFile = luban.get()
                            toast("Success [Luban,File,Get] $resultFile")
                            displayResult(resultFile.absolutePath)
                        }
                        LaunchType.COROUTINES -> {
                            GlobalScope.launch {
                                val resultFile = luban.get(Dispatchers.IO)
                                toast("Success [Luban,File,Kt,Get] $resultFile")
                                withContext(Dispatchers.Main) {
                                    displayResult(resultFile.absolutePath)
                                }
                            }
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
                                        L.d(Thread.currentThread().toString())
                                        toast("Start [Luban,Bitmap,Launch]")
                                    }

                                    override fun onSuccess(result: Bitmap) {
                                        L.d(Thread.currentThread().toString())
                                        displayResult(result)
                                        toast("Success [Luban,Bitmap,Launch] : $result")
                                    }

                                    override fun onError(throwable: Throwable) {
                                        L.d(Thread.currentThread().toString())
                                        toast("Error [Luban,Bitmap,Launch] : $throwable")
                                    }
                                })
                                .launch()
                        }
                        LaunchType.AS_FLOWABLE -> {
                            val d = bitmapBuilder.asFlowable()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({
                                    toast("Success [Luban,Bitmap,Flowable] : $it")
                                    displayResult(it)
                                }, {
                                    toast("Error [Luban,Bitmap,Flowable] : $it")
                                })
                        }
                        LaunchType.GET -> {
                            val bitmap = bitmapBuilder.get()
                            toast("Success [Luban,Bitmap,Get] $bitmap")
                            displayResult(bitmap)
                        }
                        LaunchType.COROUTINES -> {
                            GlobalScope.launch {
                                val bitmap = bitmapBuilder.get(Dispatchers.IO)
                                toast("Success [Luban,Bitmap,Kt,Get] $bitmap")
                                withContext(Dispatchers.Main) {
                                    displayResult(bitmap)
                                }
                            }
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
                        L.d(Thread.currentThread().toString())
                        toast("Compress Start")
                    }

                    override fun onSuccess(result: File) {
                        L.d(Thread.currentThread().toString())
                        displayResult(result?.absolutePath)
                        toast("Compress Success : $result")
                    }

                    override fun onError(throwable: Throwable) {
                        L.d(Thread.currentThread().toString())
                        toast("Compress Error ：$throwable")
                    }
                })
                .setQuality(80)
                .strategy(MySimpleStrategy())
                .launch()
        }
    }

    private fun displayOriginal(filePath: String?) {
        if (TextUtils.isEmpty(filePath)) {
            toast("Error when displaying image info!")
            return
        }
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = false
        var bitmap = BitmapFactory.decodeFile(filePath, options)
        val angle = CImageUtils.getImageAngle(File(filePath))
        if (angle != 0) bitmap = CImageUtils.rotateBitmap(bitmap, angle)
        binding.ivOriginal.setImageBitmap(bitmap)
        val size = bitmap.byteCount
        binding.tvOriginal.text = "Original:\nwidth: ${bitmap.width}\nheight:${bitmap.height}\nsize:$size"
        binding.ivOriginal.tag = filePath
    }

    private fun displayResult(filePath: String?) {
        if (TextUtils.isEmpty(filePath)) {
            toast("Error when displaying image info!")
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
        GET,
        COROUTINES
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
