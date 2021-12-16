package me.shouheng.sample.view

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import com.bumptech.glide.Glide
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.shouheng.compress.*
import me.shouheng.compress.listener.CompressListener
import me.shouheng.compress.naming.CacheNameFactory
import me.shouheng.compress.strategy.config.ScaleMode
import me.shouheng.compress.utils.CImageUtils
import me.shouheng.sample.R
import me.shouheng.sample.data.*
import me.shouheng.sample.databinding.ActivityMainBinding
import me.shouheng.sample.utils.*
import me.shouheng.utils.app.IntentUtils
import me.shouheng.utils.constant.ActivityDirection
import me.shouheng.utils.ktx.checkCameraPermission
import me.shouheng.utils.ktx.onDebouncedClick
import me.shouheng.utils.ktx.start
import me.shouheng.utils.stability.L
import me.shouheng.utils.store.FileUtils
import me.shouheng.utils.store.IOUtils
import me.shouheng.utils.store.PathUtils
import me.shouheng.vmlib.base.CommonActivity
import me.shouheng.vmlib.comn.EmptyViewModel
import java.io.File

/** Main page. */
class MainActivity : CommonActivity<EmptyViewModel, ActivityMainBinding>() {

    private lateinit var originalFile: File

    /* configuration for compressor */
    private var colorConfig = Bitmap.Config.ALPHA_8
    private var imageScaleMode = ScaleMode.SCALE_LARGER
    private var compressorSourceType = SourceType.FILE
    private var compressorLaunchType = LaunchType.LAUNCH
    private var compressorResultType = ResultType.FILE

    /* configuration for luban */
    private var lubanSourceType = SourceType.FILE
    private var lubanLaunchType = LaunchType.LAUNCH
    private var lubanResultType = ResultType.FILE

    override fun getLayoutResId(): Int = R.layout.activity_main

    override fun doCreateView(savedInstanceState: Bundle?) {
        configOptionsViews()
        configButtons()
        configSourceViews()
        observes()
    }

    private fun configSourceViews() {
        binding.ivOriginal.setOnLongClickListener {
            val tag = binding.ivOriginal.tag
            if (tag != null) {
                val filePath = tag as String
                val file = File(filePath)
                Glide.with(context).load(file).into(binding.ivResult)
            }
            true
        }
        binding.btnCapture.onDebouncedClick {
            checkCameraPermission {
                val file = File(PathUtils.getExternalPicturesPath(),
                    "${System.currentTimeMillis()}.jpeg")
                val succeed = FileUtils.createOrExistsFile(file)
                if (succeed) {
                    originalFile = file
                    startActivityForResult(
                        IntentUtils.getCaptureIntent(file.uri(context)),
                        REQUEST_IMAGE_CAPTURE
                    )
                } else{
                    toast("Can't create file!")
                }
            }
        }
        binding.btnChoose.onDebouncedClick { chooseFromAlbum() }
    }

    private fun configButtons() {
        binding.btnLuban.onDebouncedClick { compressByAutomatic() }
        binding.btnCompressor.onDebouncedClick{ compressByConcrete() }
        binding.btnCustom.onDebouncedClick { compressByCustom() }
        binding.btnSample.onDebouncedClick {
            start(SampleActivity::class.java, ActivityDirection.ANIMATE_SLIDE_TOP_FROM_BOTTOM)
        }
    }

    private fun configOptionsViews() {
        /* configuration for automatic. */
        binding.aspLubanSourceType.onItemSelected { lubanSourceType = sourceTypes[it] }
        binding.aspLubanResult.onItemSelected { lubanResultType = resultTypes[it] }
        binding.aspLubanLaunch.onItemSelected { lubanLaunchType = launchTypes[it] }

        /* configuration for concrete. */
        binding.aspSourceType.onItemSelected { compressorSourceType = sourceTypes[it] }
        binding.aspColor.onItemSelected {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                colorConfig = colorConfigs[it]
            }
        }

        binding.aspScale.onItemSelected { imageScaleMode = scaleModes[it] }
        binding.aspCompressorResult.onItemSelected { compressorResultType = resultTypes[it] }
        binding.aspCompressorLaunch.onItemSelected { compressorLaunchType = launchTypes[it] }
    }

    private fun observes() {
        onResult(REQUEST_IMAGE_CAPTURE) { ret, _ ->
            if (ret == Activity.RESULT_OK) {
                displayOriginal(originalFile.absolutePath)
            }
        }
        onResult(REQUEST_SELECT_IMAGE) { ret, data ->
            if (ret == Activity.RESULT_OK && data != null) {
                displayOriginal(data.data.getPath(context))
            }
        }
    }

    private fun compressByConcrete() {
        val tag = binding.ivOriginal.tag
        tag ?: return
        val path = tag as String
        L.d("Current configuration: \nConfig: $colorConfig\nScaleMode: $imageScaleMode")

        // Step 1: get a compress object.
        val compress: Compress = getCompress(path)
        // Step 2: fill in basic options.
        val compressor = compress
            .setQuality(60)
            .setTargetDir(PathUtils.getExternalPicturesPath())
            .setCompressListener(getCompressListener("Concrete", compressorResultType))
            .concrete {
                withBitmapConfig(colorConfig)
                withMaxWidth(100f)
                withMaxHeight(100f)
                withScaleMode(imageScaleMode)
            }

        // launch according to given launch type
        when(compressorResultType) {
            ResultType.FILE -> {
                startForResultTypeFile(compressor, "Concrete")
            }
            ResultType.BITMAP -> {
                startForResultTypeBitmap(
                    compressor.asBitmap().setCompressListener(
                        getBitmapCompressListener("Concrete", compressorResultType)
                    ),
                    path, "Concrete"
                )
            }
        }
    }

    private fun compressByAutomatic() {
        val tag = binding.ivOriginal.tag
        tag ?: return
        val path = tag as String
        val copy = binding.scCopy.isChecked

        // Step 1: get a compress object.
        val compress: Compress = getCompress(path)
        // Step 2: fill in basic options.
        val automatic = compress
            .setCompressListener(getCompressListener("Automatic", lubanResultType))
            .setCacheNameFactory(getCacheNameFactory())
            .setQuality(80)
            .automatic {
                withIgnoreSize(100)
                withCopyWhenIgnore(copy)
            }

        when(lubanResultType) {
            ResultType.FILE -> {
                startForResultTypeFile(automatic, "Automatic")
            }
            ResultType.BITMAP -> {
                startForResultTypeBitmap(
                    automatic.asBitmap().setCompressListener(
                        getBitmapCompressListener("Automatic", compressorResultType)
                    ),
                    path,
                    "Automatic"
                )
            }
        }
    }

    private fun compressByCustom() {
        binding.ivOriginal.tag?.let {
            val file = File(it as String)
            Compress.with(context, file)
                .setCompressListener(getCompressListener("Half", ResultType.FILE))
                .setQuality(80)
                .algorithm(AlwaysHalfAlgorithm())
                .launch()
        }
    }

    /** Get [Compress] according to desired source type. */
    private fun getCompress(path: String): Compress {
        return when(compressorSourceType) {
            SourceType.FILE -> {
                Compress.with(this, File(path))
            }
            SourceType.BYTE_ARRAY -> {
                val byteArray = IOUtils.readFile2BytesByStream(path)
                Compress.with(this, byteArray)
            }
            SourceType.BITMAP -> {
                val bitmap = BitmapFactory.decodeFile(path)
                Compress.with(this, bitmap)
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun startForResultTypeBitmap(
        algorithm: Algorithm<Bitmap>,
        path: String,
        algorithmName: String
    ) {
        when(compressorLaunchType) {
            LaunchType.LAUNCH -> {
                algorithm.launch()
            }
            LaunchType.AS_FLOWABLE -> {
                algorithm.asFlowable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        toast("Success [$algorithmName][Bitmap][Flowable] $it")
                        displayResult(it)
                    }, {
                        toast("Error [$algorithmName][Bitmap][Flowable]: $it")
                    })
            }
            LaunchType.GET -> {
                val bitmap = algorithm.get()
                toast("Success [$algorithmName][Bitmap][Get] $bitmap")
                displayResult(bitmap)
                // this will cause a crash when it's automatically recycling the source bitmap
                val srcBitmap = BitmapFactory.decodeFile(path)
                binding.ivOriginal.setImageBitmap(srcBitmap)
            }
            LaunchType.COROUTINES -> {
                GlobalScope.launch {
                    val bitmap = algorithm.get(Dispatchers.IO)
                    toast("Success [$algorithmName][Bitmap][Coroutines] $bitmap")
                    withContext(Dispatchers.Main) {
                        displayResult(bitmap)
                    }
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun startForResultTypeFile(
        algorithm: Algorithm<File>,
        algorithmName: String
    ) {
        when(compressorLaunchType) {
            LaunchType.LAUNCH -> {
                algorithm.launch()
            }
            LaunchType.AS_FLOWABLE -> {
                algorithm.asFlowable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        toast("Success [$algorithmName][File][Flowable] $it")
                        displayResult(it.absolutePath)
                    }, {
                        toast("Error [$algorithmName][File][Flowable]: $it")
                    })
            }
            LaunchType.GET -> {
                val resultFile = algorithm.get()
                toast("Success [$algorithmName][File][Get] $resultFile")
                displayResult(resultFile?.absolutePath)
            }
            LaunchType.COROUTINES -> {
                GlobalScope.launch {
                    val resultFile = algorithm.get(Dispatchers.IO)
                    toast("Success [$algorithmName][File][Coroutines] $resultFile")
                    withContext(Dispatchers.Main) {
                        displayResult(resultFile?.absolutePath)
                    }
                }
            }
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

    private fun getBitmapCompressListener(
        algorithm: String,
        resultType: ResultType
    ): RequestBuilder.Callback<Bitmap> {
        return object : RequestBuilder.Callback<Bitmap> {
            override fun onStart() {
                toast("[onStart][$algorithm][$resultType][${Thread.currentThread()}]")
            }

            override fun onSuccess(result: Bitmap) {
                displayResult(result)
                toast("[onSuccess][$algorithm][$resultType][${Thread.currentThread()}]: $result")
            }

            override fun onError(throwable: Throwable) {
                toast("[onError][$algorithm][$resultType][${Thread.currentThread()}]: $throwable")
            }
        }
    }

    private fun getCacheNameFactory(): CacheNameFactory {
        return object : CacheNameFactory {
            override fun getFileName(format: Bitmap.CompressFormat): String {
                return System.currentTimeMillis().toString() + ".jpg"
            }
        }
    }

    private fun getCompressListener(
        algorithm: String,
        resultType: ResultType
    ): CompressListener {
        return object : CompressListener {
            override fun onStart() {
                toast("[onStart][$algorithm][$resultType][${Thread.currentThread()}]")
            }

            override fun onSuccess(result: File) {
                displayResult(result.absolutePath)
                toast("[onSuccess][$algorithm][$resultType][${Thread.currentThread()}]: $result")
            }

            override fun onError(throwable: Throwable) {
                toast("[onError][$algorithm][$resultType][${Thread.currentThread()}]: $throwable")
            }
        }
    }
}
