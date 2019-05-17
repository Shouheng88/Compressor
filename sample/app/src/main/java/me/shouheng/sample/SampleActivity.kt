package me.shouheng.sample

import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.shouheng.compress.Compress
import me.shouheng.compress.strategy.Strategies
import me.shouheng.compress.strategy.config.ScaleMode
import me.shouheng.sample.databinding.ActivitySampleBinding
import me.shouheng.utils.ui.ImageUtils
import me.shouheng.utils.ui.ToastUtils
import me.shouheng.utils.ui.ViewUtils
import java.io.ByteArrayOutputStream

/**
 * @author WngShhng (shouheng2015@gmail.com)
 * @version 2019/5/17 22:04
 */
class SampleActivity : BaseActivity() {

    private lateinit var binding: ActivitySampleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_sample, null, false)
        setContentView(binding.root)

        val d = Observable.create<Bitmap> {
            val bitmap = Compress.with(this, BitmapFactory.decodeResource(resources, R.drawable.img_lena))
                .strategy(Strategies.compressor())
                .setMaxHeight(100f)
                .setMaxHeight(120f)
                .setScaleMode(ScaleMode.SCALE_LARGER)
                .asBitmap()
                .get()
            val result = ImageUtils.addCircleBorder(bitmap, ViewUtils.dp2px(1f), Color.RED)
            it.onNext(result)
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            binding.iv2.setImageBitmap(it)
        }, {
            ToastUtils.showShort("error : $it")
        })

        val d2 = Observable.create<Bitmap> {
            var bitmap = BitmapFactory.decodeResource(resources, R.drawable.img_lena)
            val out = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            val byteArray = out.toByteArray()
            bitmap = Compress.with(this@SampleActivity, byteArray)
                .strategy(Strategies.compressor())
                .setMaxHeight(50f)
                .setMaxHeight(60f)
                .setScaleMode(ScaleMode.SCALE_LARGER)
                .asBitmap()
                .get()
            val result = ImageUtils.addCornerBorder(bitmap, ViewUtils.dp2px(1f),
                Color.GREEN, ViewUtils.dp2px(20f).toFloat())
            it.onNext(result)
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            binding.iv3.setImageBitmap(it)
        }, {
            ToastUtils.showShort("error : $it")
        })
    }
}
