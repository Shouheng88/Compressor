package me.shouheng.sample.view

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import com.bumptech.glide.Glide
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.shouheng.compress.*
import me.shouheng.compress.listener.CompressListener
import me.shouheng.compress.strategy.config.ScaleMode
import me.shouheng.sample.R
import me.shouheng.sample.custom.AlwaysHalfAlgorithm
import me.shouheng.sample.custom.AssetsResource
import me.shouheng.sample.databinding.ActivitySampleBinding
import me.shouheng.sample.utils.uri
import me.shouheng.utils.constant.ActivityDirection
import me.shouheng.utils.ktx.dp
import me.shouheng.utils.ktx.save
import me.shouheng.utils.stability.L
import me.shouheng.utils.store.PathUtils
import me.shouheng.utils.ui.ImageUtils
import me.shouheng.vmlib.anno.ActivityConfiguration
import me.shouheng.vmlib.base.CommonActivity
import me.shouheng.vmlib.comn.EmptyViewModel
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Sample activity for different:
 *
 * - launch modes
 * - algorithms
 * - result types
 * - source types
 *
 * @author Shouheng Wang
 * @version 2019/5/17 22:04
 */
@ActivityConfiguration(exitDirection = ActivityDirection.ANIMATE_SLIDE_BOTTOM_FROM_TOP)
class SampleActivity : CommonActivity<EmptyViewModel, ActivitySampleBinding>() {

    override fun getLayoutResId(): Int = R.layout.activity_sample

    override fun doCreateView(savedInstanceState: Bundle?) {
        compressAndGetBitmapWithBlocking()
        autoCompressAndGetBitmapWithBlocking()
        compressAndGetFileAsync()
        compressUriAndGetFileWithKotlin()
        compressUriAndGetBitmapWithKotlin()
        compressByteArrayAndGetFileWithRxJava()
        compressFileAndGetBitmapWithRxJava()
        compressInputStreamAndGetBitmapWithRxJava()
        compressInputStreamAndGetBitmapWithAsync()
    }

    /**
     * Sample:
     * - Source:    [Bitmap]
     * - Algorithm: [concrete]
     * - Launch:    Blocking
     * - Result:    [Bitmap]
     */
    @SuppressLint("CheckResult")
    private fun compressAndGetBitmapWithBlocking() {
        Observable.create<Bitmap> {
            val srcBitmap = BitmapFactory.decodeResource(resources, R.drawable.img_lena)
            // Compress and try to get the result on current thread.
            val bitmap = Compress.with(context, srcBitmap)
                .concrete {
                    withMaxHeight(100f)
                    withMaxWidth(120f)
                    withScaleMode(ScaleMode.SCALE_LARGER)
                }
                .asBitmap()
                .get()
            // Add some color ... don't mind, none of business with compress ;)
            val result = ImageUtils.addCircleBorder(bitmap, 1f.dp(), Color.RED)
            it.onNext(result)
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            binding.iv2.setImageBitmap(it)
        }, {
            toast("error : $it")
        })
    }

    /**
     * Sample:
     * - Source:    [Bitmap]
     * - Algorithm: [automatic]
     * - Launch:    Blocking
     * - Result:    [Bitmap]
     */
    @SuppressLint("CheckResult")
    private fun autoCompressAndGetBitmapWithBlocking() {
        Observable.create<Bitmap> {
            var srcBitmap = BitmapFactory.decodeResource(resources, R.drawable.img_lena)
            val out = ByteArrayOutputStream()
            srcBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            val byteArray = out.toByteArray()
            srcBitmap = Compress.with(context, byteArray)
                .automatic {
                    // do nothing
                }
                .asBitmap()
                .get()
            val result = ImageUtils.addCornerBorder(srcBitmap, 1f.dp(), Color.GREEN, 20f.dp().toFloat())
            it.onNext(result)
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            binding.iv3.setImageBitmap(it)
        }, {
            toast("error : $it")
        })
    }

    /**
     * Sample:
     * - Source:    [File]
     * - Algorithm: [concrete]
     * - Launch:    [AsyncTask]
     * - Result:    [File]
     */
    private fun compressAndGetFileAsync() {
        val file = getOrSaveBitmap() ?: return
        Compress.with(context, file)
            .setQuality(80)
            .setCompressListener(object: CompressListener {
                override fun onStart() {
                    L.d("compressAndGetFileAsync onStart")
                }

                override fun onSuccess(result: File) {
                    L.d("compressAndGetFileAsync onSuccess")
                    Glide.with(context).load(file).into(binding.iv4)
                }

                override fun onError(throwable: Throwable) {
                    L.e("compressAndGetFileAsync onError")
                    throwable.printStackTrace()
                }
            })
            .concrete {
                withMaxWidth(100f)
                withMaxHeight(100f)
                withScaleMode(ScaleMode.SCALE_HEIGHT)
                withIgnoreIfSmaller(true)
            }
            .launch()
    }

    /**
     * Sample:
     * - Source:    [Uri]
     * - Algorithm: [concrete]
     * - Launch:    [Dispatchers]
     * - Result:    [Bitmap]
     */
    private fun compressUriAndGetBitmapWithKotlin() {
        val file = getOrSaveBitmap() ?: return
        GlobalScope.launch {
            val bitmap = Compress.with(context, file.uri(context))
                .setQuality(80)
                .concrete {
                    withMaxWidth(100f)
                    withMaxHeight(100f)
                    withScaleMode(ScaleMode.SCALE_HEIGHT)
                    withIgnoreIfSmaller(true)
                }
                .asBitmap()
                .get(Dispatchers.IO)
            withContext(Dispatchers.Main) {
                Glide.with(context).load(bitmap).into(binding.iv5)
            }
        }
    }

    /**
     * Sample:
     * - Source:    [Uri]
     * - Algorithm: [concrete]
     * - Launch:    [Dispatchers]
     * - Result:    [File]
     */
    private fun compressUriAndGetFileWithKotlin() {
        val file = getOrSaveBitmap() ?: return
        GlobalScope.launch {
            val compressedFile = Compress.with(context, file.uri(context))
                .setQuality(80)
                .concrete {
                    withMaxWidth(100f)
                    withMaxHeight(100f)
                    withScaleMode(ScaleMode.SCALE_HEIGHT)
                    withIgnoreIfSmaller(true)
                }
                .get(Dispatchers.IO)
            withContext(Dispatchers.Main) {
                Glide.with(context).load(compressedFile).into(binding.iv6)
            }
        }
    }

    /**
     * Sample:
     * - Source:    [ByteArray]
     * - Algorithm: [concrete]
     * - Launch:    [Dispatchers]
     * - Result:    [Bitmap]
     */
    private fun compressByteArrayAndGetFileWithRxJava() {
        val srcBitmap = BitmapFactory.decodeResource(resources, R.drawable.img_lena)
        val out = ByteArrayOutputStream()
        srcBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        val byteArray = out.toByteArray()
        GlobalScope.launch {
            val bitmap = Compress.with(context, byteArray)
                .setQuality(80)
                .concrete {
                    withMaxWidth(100f)
                    withMaxHeight(100f)
                    withScaleMode(ScaleMode.SCALE_HEIGHT)
                    withIgnoreIfSmaller(true)
                }
                .asBitmap()
                .setCompressListener(object : RequestBuilder.Callback<Bitmap> {
                    override fun onStart() {
                        L.d("compressByteArrayAndGetFileWithRxJava onStart")
                    }

                    override fun onSuccess(result: Bitmap) {
                        L.d("compressByteArrayAndGetFileWithRxJava onSuccess")
                    }

                    override fun onError(throwable: Throwable) {
                        L.e("compressByteArrayAndGetFileWithRxJava onError", throwable)
                    }
                })
                .get(Dispatchers.IO)
            withContext(Dispatchers.Main) {
                Glide.with(context).load(bitmap).into(binding.iv7)
            }
        }
    }

    /**
     * Sample:
     * - Source:    [File]
     * - Algorithm: [automatic]
     * - Launch:    [Flowable]
     * - Result:    [Bitmap]
     */
    @SuppressLint("CheckResult")
    private fun compressFileAndGetBitmapWithRxJava() {
        val file = getOrSaveBitmap() ?: return
        Compress.with(context, file)
            .setQuality(80)
            .automatic()
            .asBitmap()
            .asFlowable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Glide.with(context).load(it).into(binding.iv8)
            }, {
                L.e("compressFileAndGetBitmapWithRxJava onError", it)
            })
    }

    /**
     * Sample:
     * - Source:    [AssetsResource] (custom)
     * - Algorithm: [automatic]
     * - Launch:    [Flowable]
     * - Result:    [Bitmap]
     */
    @SuppressLint("CheckResult")
    private fun compressInputStreamAndGetBitmapWithRxJava() {
        Compress.with(context, AssetsResource("img_lena.png"))
            .setQuality(80)
            .automatic()
            .asBitmap()
            .asFlowable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Glide.with(context).load(it).into(binding.iv9)
            }, {
                L.e("compressInputStreamAndGetBitmapWithRxJava onError", it)
            })
    }

    /**
     * Sample:
     * - Source:    [AssetsResource] (custom)
     * - Algorithm: [AlwaysHalfAlgorithm] (custom)
     * - Launch:    [AsyncTask]
     * - Result:    [Bitmap]
     */
    private fun compressInputStreamAndGetBitmapWithAsync() {
        Compress.with(context, AssetsResource("img_lena.png"))
            .setQuality(80)
            .algorithm(AlwaysHalfAlgorithm())
            .asBitmap()
            .setCompressListener(object : RequestBuilder.Callback<Bitmap> {
                override fun onStart() {
                    L.d("compressInputStreamAndGetBitmapWithAsync onStart")
                }

                override fun onSuccess(result: Bitmap) {
                    L.d("compressInputStreamAndGetBitmapWithAsync onSuccess")
                    Glide.with(context).load(result).into(binding.iv10)
                }

                override fun onError(throwable: Throwable) {
                    L.e("compressInputStreamAndGetBitmapWithAsync onError", throwable)
                }
            })
            .launch()
    }

    private fun getOrSaveBitmap(): File? {
        val file = File(PathUtils.getExternalAppPicturesPath() +"/lena.jpg")
        if (file.exists()) return file
        val srcBitmap = BitmapFactory.decodeResource(resources, R.drawable.img_lena)
        return if (srcBitmap.save(file, Bitmap.CompressFormat.JPEG)) file else null
    }
}
