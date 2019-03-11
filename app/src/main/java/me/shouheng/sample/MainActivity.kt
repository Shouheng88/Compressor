package me.shouheng.sample

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.shouheng.compress.Compress
import me.shouheng.compress.listener.CompressListener
import me.shouheng.compress.strategy.Strategies
import me.shouheng.compress.utils.LogUtils
import me.shouheng.sample.utils.FileManager
import java.io.File

class MainActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 0x0100
        const val REQUEST_SELECT_IMAGE = 0x0101
    }

    private lateinit var originalFile: File
    private lateinit var ivOriginal: ImageView
    private lateinit var tvOriginal: TextView
    private lateinit var ivResult: ImageView
    private lateinit var tvResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ivOriginal = findViewById(R.id.iv_original)
        tvOriginal = findViewById(R.id.tv_original)
        ivResult = findViewById(R.id.iv_result)
        tvResult = findViewById(R.id.tv_result)

        LogUtils.setDebug(true)
    }

    fun compressor(v: View) {
        val tag = ivOriginal.tag
        if (tag != null) {
            val filePath = tag as String
            val d = Compress.with(this, File(filePath))
                .setQuality(60)
                .setTargetDir("")
                .setCompressListener(object : CompressListener {
                    override fun onStart() {
                        LogUtils.d("---- Start")
                    }

                    override fun onSuccess(result: File?) {
                        LogUtils.d("---- Success")
                    }

                    override fun onError(throwable: Throwable?) {
                        LogUtils.d("---- Error")
                    }
                })
                .strategy(Strategies.compressor())
                .setMaxHeight(100f)
                .setMaxWidth(100f)
                .asFlowable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { displayResult(it.absolutePath) }
        }
    }

    fun compressorWithCallback(v: View) {
        val tag = ivOriginal.tag
        if (tag != null) {
            val filePath = tag as String
            val d = Compress.with(this, File(filePath))
                .setQuality(60)
                .setCompressListener(object : CompressListener {
                    override fun onStart() {
                        LogUtils.d("---- Start")
                    }

                    override fun onSuccess(result: File?) {
                        LogUtils.d("---- Success")
                        displayResult(result?.absolutePath)
                    }

                    override fun onError(throwable: Throwable?) {
                        LogUtils.d("---- Error")
                    }
                })
                .strategy(Strategies.compressor())
                .setMaxHeight(100f)
                .setMaxWidth(100f)
                .launch()
        }
    }

    fun luban(v: View) {
        val tag = ivOriginal.tag
        if (tag != null) {
            val filePath = tag as String
            val d = Compress.with(this, File(filePath))
                .setQuality(60)
                .setCompressListener(object : CompressListener {
                    override fun onStart() {
                        LogUtils.d("---- Start")
                    }

                    override fun onSuccess(result: File?) {
                        LogUtils.d("---- Success")
                    }

                    override fun onError(throwable: Throwable?) {
                        LogUtils.d("---- Error")
                    }
                })
                .strategy(Strategies.luban())
                .setIgnoreSize(100, true)
                .asFlowable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { displayResult(it.absolutePath) }
        }
    }

    fun lubanWithCallback(v: View) {
        val tag = ivOriginal.tag
        if (tag != null) {
            val filePath = tag as String
            val d = Compress.with(this, File(filePath))
                .setQuality(60)
                .setCompressListener(object : CompressListener {
                    override fun onStart() {
                        LogUtils.d("---- Start")
                    }

                    override fun onSuccess(result: File?) {
                        LogUtils.d("---- Success")
                        displayResult(result?.absolutePath)
                    }

                    override fun onError(throwable: Throwable?) {
                        LogUtils.d("---- Error")
                    }
                })
                .strategy(Strategies.luban())
                .setIgnoreSize(100, true)
                .launch()
        }
    }

    fun capture(v: View) {
        val file = FileManager.createAttachment(this, ".png")
        if (file != null) {
            originalFile = file
            val uri = FileManager.getUriFromFile(this, file)
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        } else{
            Toast.makeText(this, "Can't create file!", Toast.LENGTH_SHORT).show()
        }
    }

    fun choose(v: View) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_SELECT_IMAGE)
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
        ivOriginal.setImageBitmap(bitmap)
        val size = bitmap.byteCount
        tvOriginal.text = "Original:\nwidth: $actualWidth\nheight:$actualHeight\nsize:$size"
        ivOriginal.tag = filePath
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
        ivResult.setImageBitmap(bitmap)
        val size = bitmap.byteCount
        tvResult.text = "Result:\nwidth: $actualWidth\nheight:$actualHeight\nsize:$size"
        ivResult.tag = filePath
    }

}
