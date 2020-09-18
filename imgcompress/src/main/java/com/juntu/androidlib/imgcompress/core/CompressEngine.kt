package com.juntu.androidlib.imgcompress.core

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import com.juntu.androidlib.imgcompress.checker.Checker
import com.juntu.androidlib.imgcompress.config.CompressConfig
import com.juntu.androidlib.imgcompress.config.EngineType
import com.juntu.androidlib.imgcompress.entity.Image
import com.juntu.androidlib.imgcompress.listener.CompressListener
import com.juntu.androidlib.imgcompress.listener.OnRenameListener
import com.juntu.androidlib.imgcompress.provider.InputSteamAdapter
import com.juntu.androidlib.imgcompress.provider.InputStreamProvider
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

/**
 * Created by cj on 2020/9/14.
 * Email:codesexy@163.com
 * Function:
 * desc:压缩的核心类
 */
class CompressEngine(
    var config: CompressConfig,
    var context: Context
) {

    private val DEFAULT_DISK_CACHE_DIR = "disk_cache"

    fun <T> compress(image: Image<T>, onRename: OnRenameListener, listener: CompressListener<T>) {
        var provider = getProvider(image)

        var file = File(provider.getPath())
        if (!file.exists()) {
            listener.onCompressFailed(image, "path=${provider.getPath()},errorMsg=文件不存在")
            return
        }

        //本身就不需要压缩就直接返回
        // 小于设定的值就不进行压缩
        if (!image.shouldCompressed || !Checker.SINGLE.needCompress(
                config.ignoreSize,
                provider.getPath()
            )
        ) {
            listener.onCompressSuccess(
                Image<T>(
                    image.originalPath,
                    false,
                    provider.getPath()
                )
            )
            return
        }

        try {

            var targetFile = getImageCacheFile(context, Checker.SINGLE.extSuffix(provider))
            var rename = onRename.rename(provider.getPath())
            if (!TextUtils.isEmpty(rename) and !TextUtils.equals(provider.getPath(), rename)) {
                targetFile = getImageCustomFile(context, rename!!)
            }

            if (targetFile == null) {
                listener.onCompressFailed(image, "path=${provider.getPath()},errorMsg=创建文件错误")
            }


            var imageFile = when (config.engineType) {
                EngineType.LuBan -> LubanEngine(config).compress(provider, targetFile!!)
                else -> CustomEngine(config).compress(provider, targetFile!!)
            }
            listener.onCompressSuccess(
                image = Image<T>(
                    image.originalPath,
                    false,
                    imageFile.absolutePath
                )
            )
        } catch (e: Exception) {
            listener.onCompressFailed(image, "path=${provider.getPath()},errorMsg=${e.message}")
        }
    }


    private fun <T> getProvider(data: Image<T>): InputStreamProvider {
        return when (data.originalPath) {
            is String -> {
                object : InputSteamAdapter() {
                    override fun openInternal(): InputStream? = FileInputStream(data.originalPath)

                    override fun getPath(): String = data.originalPath
                }
            }
            is File -> {
                object : InputSteamAdapter() {
                    override fun openInternal(): InputStream? = FileInputStream(data.originalPath)

                    override fun getPath(): String = data.originalPath.absolutePath
                }
            }
            is Uri -> {
                object : InputSteamAdapter() {
                    override fun openInternal(): InputStream? =
                        context.contentResolver.openInputStream(data.originalPath)

                    override fun getPath(): String = data.originalPath.path!!
                }
            }
            else -> {
                throw IllegalArgumentException("Incoming data type exception, it must be String, File, Uri")
            }
        }
    }

    private fun getImageCacheFile(
        context: Context,
        suffix: String?
    ): File? {
        if (TextUtils.isEmpty(config.cacheDir)) {
            config.cacheDir = getImageCacheDir(context)?.getAbsolutePath()
        }

        val cacheBuilder: String = config.cacheDir + "/" +
                System.currentTimeMillis() +
                (Math.random() * 1000).toInt() +
                if (TextUtils.isEmpty(suffix)) ".jpg" else suffix
        return File(cacheBuilder)
    }


    private fun getImageCacheDir(context: Context): File? {
        return getImageCacheDir(context, DEFAULT_DISK_CACHE_DIR)
    }

    private fun getImageCacheDir(
        context: Context,
        cacheName: String
    ): File? {
        val cacheDir = context.externalCacheDir
        if (cacheDir != null) {
            val result = File(cacheDir, cacheName)
            return if (!result.mkdirs() && (!result.exists() || !result.isDirectory)) {
                // File wasn't able to create a directory, or the result exists but not a directory
                null
            } else result
        }

        return null
    }


    private fun getImageCustomFile(
        context: Context,
        filename: String
    ): File? {
        if (TextUtils.isEmpty(config.cacheDir)) {
            config.cacheDir = getImageCacheDir(context)!!.absolutePath
        }
        val cacheBuilder: String = config.cacheDir + "/" + filename
        return File(cacheBuilder)
    }


}