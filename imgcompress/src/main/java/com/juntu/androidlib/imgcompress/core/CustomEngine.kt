package com.juntu.androidlib.imgcompress.core

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.juntu.androidlib.imgcompress.config.CompressConfig
import com.juntu.androidlib.imgcompress.provider.InputStreamProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by cj on 2020/9/17.
 * Email:codesexy@163.com
 * Function:
 * desc:
 */
class CustomEngine(
    var config: CompressConfig
) {


    @Throws(IOException::class)
    fun compress(provider: InputStreamProvider, targetFile: File): File {
        return if (config.isEnablePixelCompress) {
            compressImageByPixel(provider, targetFile)
        } else {
            compressImageByQuality(provider, targetFile)
        }
    }


    @Throws(IOException::class)
    fun compressImageByPixel(provider: InputStreamProvider, targetFile: File): File {
        val newOpts = BitmapFactory.Options()
        newOpts.inJustDecodeBounds = true // 只读边,不读内容

        BitmapFactory.decodeFile(provider.getPath(), newOpts)
        newOpts.inJustDecodeBounds = false
        val width = newOpts.outWidth
        val height = newOpts.outHeight

        val maxSize: Float = config.maxPixel.toFloat()
        var be = 1
        if (width >= height && width > maxSize) { // 缩放比,用高或者宽其中较大的一个数据进行计算
            be = (newOpts.outWidth / maxSize).toInt()
            be++
        } else if (width < height && height > maxSize) {
            be = (newOpts.outHeight / maxSize).toInt()
            be++
        }
        if (width <= config.unCompressNormalPixel || height <= config.unCompressNormalPixel) {
            be = 2
            if (width <= config.unCompressMinPixel || height <= config.unCompressMinPixel)
                be = 1
        }
        newOpts.inSampleSize = be // 设置采样率

        newOpts.inPreferredConfig = Bitmap.Config.ARGB_8888 // 该模式是默认的,可不设

        newOpts.inPurgeable = true // 同时设置才会有效

        newOpts.inInputShareable = true // 当系统内存不够时候图片自动被回收

        val bitmap = BitmapFactory.decodeFile(provider.getPath(), newOpts)
        return if (config.isEnableQualityCompress) {
            compressImageByQuality(provider, targetFile) // 压缩好比例大小后再进行质量压缩
        } else {
            bitmap.compress(
                Bitmap.CompressFormat.JPEG, 100,
                FileOutputStream(targetFile)
            )
            targetFile
        }
    }


    @Throws(IOException::class)
    private fun compressImageByQuality(provider: InputStreamProvider, targetFile: File): File {
        val baos = ByteArrayOutputStream()
        var options = 100
        var bitmap = BitmapFactory.decodeFile(provider.getPath())
        bitmap.compress(
            Bitmap.CompressFormat.JPEG,
            options,
            baos
        ) // 质量压缩方法，把压缩后的数据存放到baos中 (100表示不压缩，0表示压缩到最小)

        while (baos.toByteArray().size > config.maxSize) { // 循环判断如果压缩后图片是否大于指定大小,大于继续压缩
            baos.reset() // 重置baos即让下一次的写入覆盖之前的内容
            options -= 5 // 图片质量每次减少5
            if (options <= 5) options = 5 // 如果图片质量小于5，为保证压缩后的图片质量，图片最底压缩质量为5
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos) // 将压缩后的图片保存到baos中
            if (options == 5) break // 如果图片的质量已降到最低则，不再进行压缩
        }

        val fos = FileOutputStream(targetFile) //将压缩后的图片保存的本地上指定路径中
        fos.write(baos.toByteArray())
        fos.flush()
        fos.close()
        baos.flush()
        baos.close()
        bitmap.recycle()
        return targetFile
    }


}