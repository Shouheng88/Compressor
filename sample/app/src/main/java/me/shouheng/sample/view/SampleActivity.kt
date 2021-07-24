package me.shouheng.sample.view

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import automatic
import concrete
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.shouheng.compress.Compress
import me.shouheng.compress.strategy.config.ScaleMode
import me.shouheng.sample.R
import me.shouheng.sample.databinding.ActivitySampleBinding
import me.shouheng.utils.constant.ActivityDirection
import me.shouheng.utils.ktx.dp
import me.shouheng.utils.ui.ImageUtils
import me.shouheng.vmlib.anno.ActivityConfiguration
import me.shouheng.vmlib.base.CommonActivity
import me.shouheng.vmlib.comn.EmptyViewModel
import java.io.ByteArrayOutputStream

/**
 * Sample strategy activity
 *
 * @author Shouheng Wang
 * @version 2019/5/17 22:04
 */
@ActivityConfiguration(exitDirection = ActivityDirection.ANIMATE_SLIDE_BOTTOM_FROM_TOP)
class SampleActivity : CommonActivity<EmptyViewModel, ActivitySampleBinding>() {

    override fun getLayoutResId(): Int = R.layout.activity_sample

    override fun doCreateView(savedInstanceState: Bundle?) {
        compressOnCurrentThread()
        compress()
    }

    /** The sample of do image compress on current thread (blocking). */
    @SuppressLint("CheckResult")
    private fun compressOnCurrentThread() {
        Observable.create<Bitmap> {
            val srcBitmap = BitmapFactory.decodeResource(resources, R.drawable.img_lena)
            // Compress and try to get the result on current thread.
            val bitmap = Compress.with(context, srcBitmap)
                .concrete {
                    maxHeight = 100f
                    maxWidth = 120f
                    scaleMode = ScaleMode.SCALE_LARGER
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

    @SuppressLint("CheckResult")
    private fun compress() {
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
}
