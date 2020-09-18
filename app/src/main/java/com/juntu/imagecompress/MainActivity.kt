package com.juntu.imagecompress

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.juntu.androidlib.imgcompress.ImageCompress
import com.juntu.androidlib.imgcompress.config.CompressConfig
import com.juntu.androidlib.imgcompress.config.EngineType
import com.juntu.androidlib.imgcompress.entity.Image
import com.juntu.androidlib.imgcompress.listener.CompressResultListener
import com.juntu.androidlib.imgcompress.listener.OnRenameListener
import java.util.logging.Logger

class MainActivity : AppCompatActivity() {

    lateinit var compressConfig: CompressConfig
    lateinit var imgs: MutableList<Image<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // 运行时权限申请
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val perms = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            if (checkSelfPermission(perms[0]) === PackageManager.PERMISSION_DENIED ||
                checkSelfPermission(perms[1]) === PackageManager.PERMISSION_DENIED
            ) {
                requestPermissions(perms, 200)
            }
        }

        imgs = mutableListOf()
        imgs.apply {
            add(Image("/storage/emulated/0/cloudwalk/200526161135_idcard/idcard.jpg"))
            add(Image("/storage/emulated/0/DCIM/Camera/IMG_20200918_150740.jpg"))
//            add(Image("/storage/emulated/0/DCIM/Camera/IMG_20200918_15074.jpg"))
        }

        compressConfig = CompressConfig.Builder()
            .setNnCompressMinPixel(1000) // 最小像素不压缩，默认值：1000
            .setNnCompressNormalPixel(2000) // 标准像素不压缩，默认值：2000
            .setMaxPixel(1000) // 长或宽不超过的最大像素 (单位px)，默认值：1200
            .setMaxSize(100 * 1024) // 压缩到的最大大小 (单位B)，默认值：200 * 1024 = 200KB
            .enablePixelCompress(true) // 是否启用像素压缩，默认值：true
            .enableQualityCompress(true) // 是否启用质量压缩，默认值：true
            .setCacheDir("cache")
            .setEngineType(EngineType.LuBan)
            .create()

    }

    fun luban(view: View) {
        ImageCompress.Builder<String>()
            .with(this)
            .setCompressConfig(compressConfig)
            .load(imgs)
            .setRenameListener(object : OnRenameListener {
                override fun rename(filePath: String?): String? {
                    return super.rename(filePath)
                }
            })
            .setCompressListener(object : CompressResultListener<String> {
                override fun onCompressComplete(
                    successImages: List<Image<String>>,
                    failedImages: List<Image<String>>?,
                    errMsg: List<Exception>?
                ) {
                    successImages.forEach {
                        Log.e("success", it.toString())
                    }
                    failedImages?.forEach {
                        Log.e("failed", it.toString())
                    }
                    errMsg?.forEach {
                        Log.e("failed", it.toString())
                    }
                }
            })
            .launch()
    }


    fun custom(view: View) {

    }
}