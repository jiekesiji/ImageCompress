package com.juntu.androidlib.imgcompress

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import com.juntu.androidlib.imgcompress.config.CompressConfig
import com.juntu.androidlib.imgcompress.core.CompressEngine
import com.juntu.androidlib.imgcompress.entity.Image
import com.juntu.androidlib.imgcompress.listener.Compress
import com.juntu.androidlib.imgcompress.listener.CompressListener
import com.juntu.androidlib.imgcompress.listener.CompressResultListener
import com.juntu.androidlib.imgcompress.listener.OnRenameListener
import com.juntu.androidlib.imgcompress.provider.InputSteamAdapter
import com.juntu.androidlib.imgcompress.provider.InputStreamProvider
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.Exception

/**
 * Created by cj on 2020/9/14.
 * Email:codesexy@163.com
 * Function:
 * desc:使用鲁班核心算法  并对其进行扩展
 * 使其符合项目的需求
 *
 * 1. 这个类提供开发者使用
 *
 *
 */
class ImageCompress<T> private constructor(
    val context: Context,
    val resultListener: CompressResultListener<T>,
    val onRename: OnRenameListener = object : OnRenameListener {},
    val config: CompressConfig = CompressConfig.Builder().create(),
    val imgs: MutableList<Image<T>>
) : Compress {

    companion object {
        val threadPool = ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(),
            2 * Runtime.getRuntime().availableProcessors(),
            3,
            TimeUnit.MINUTES,
            LinkedBlockingDeque<Runnable>(),
            Executors.defaultThreadFactory(), ThreadPoolExecutor.DiscardPolicy()
        )
    }

    private var engine: CompressEngine


    var completeCount: AtomicInteger = AtomicInteger()

    var successImages: MutableList<Image<T>> = mutableListOf()
    var failedImages: MutableList<Image<T>> = mutableListOf()
    var errMsg: MutableList<Exception> = mutableListOf()
    var handler: Handler = Handler(Looper.getMainLooper())

    init {
        //压缩引擎
        engine = CompressEngine(config, context)

    }

    override fun compress() {
        if (!imgs.isNullOrEmpty()) {
            handler.post { resultListener.onStart() }
        } else {
            handler.post { resultListener.onStop() }
        }

        imgs.forEach {
            threadPool.execute {
                engine.compress(it, onRename, object : CompressListener<T> {
                    override fun onCompressSuccess(image: Image<T>) {
                        determineIsFinish(image, true)
                    }

                    override fun onCompressFailed(image: Image<T>, errorMsg: String) {
                        determineIsFinish(image, false, errorMsg)
                    }
                })
            }
        }
    }

    @Synchronized
    private fun determineIsFinish(image: Image<T>, isSuccess: Boolean, errorMsg: String = "") {

        if (isSuccess) {
            successImages.add(image)
        } else {
            failedImages.add(image)
            errMsg.add(IllegalStateException(errorMsg))
        }


        if (completeCount.incrementAndGet() == imgs.size) {
            handler.post {
                resultListener.onCompressComplete(successImages, failedImages, errMsg)
                resultListener.onStop()
            }
        }

        handler.post {
            resultListener.onProgress(completeCount.get().toFloat() / imgs.size)
        }
    }


    class Builder<T>() {
        private lateinit var context: Context

        private var compressListener: CompressResultListener<T> =
            object : CompressResultListener<T> {
                override fun onCompressComplete(
                    successImages: List<Image<T>>,
                    failedImages: List<Image<T>>?,
                    errMsg: List<Exception>?
                ) {

                }
            }

        private var renameListener: OnRenameListener = object : OnRenameListener {}

        private var compressConfig: CompressConfig = CompressConfig.Builder().create()

        private var imageData: MutableList<Image<T>> = mutableListOf()

        fun with(activity: Activity): Builder<T> = apply { context = activity.applicationContext }

        fun with(fragment: Fragment): Builder<T> = apply {
            context = fragment.context!!.applicationContext
        }

        fun setCompressListener(listener: CompressResultListener<T>): Builder<T> = apply {
            this.compressListener = listener
        }

        fun setRenameListener(listener: OnRenameListener): Builder<T> = apply {
            this.renameListener = listener
        }

        fun setCompressConfig(config: CompressConfig): Builder<T> = apply {
            this.compressConfig = config
        }

        fun load(imgs: MutableList<Image<T>>): Builder<T> = apply {
            this.imageData.addAll(imgs)
        }

        fun load(vararg img: Image<T>): Builder<T> = apply {
            img.forEach {
                this.imageData.add(it)
            }
        }

        fun launch() {
            ImageCompress<T>(
                context,
                compressListener,
                renameListener,
                compressConfig,
                imageData
            ).compress()
        }
    }


}