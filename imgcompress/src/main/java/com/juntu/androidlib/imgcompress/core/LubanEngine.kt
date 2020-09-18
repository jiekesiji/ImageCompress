package com.juntu.androidlib.imgcompress.core

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.text.TextUtils
import android.util.Log
import com.juntu.androidlib.imgcompress.checker.Checker
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
 * desc:使用鲁班的压缩引擎
 *
 *
 */
class LubanEngine(
    var config: CompressConfig
) {

    var srcWidth: Int = 0
    var srcHeight: Int = 0


    var options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
        inSampleSize = 1
    }


    @Throws(IOException::class)
    fun compress(provider: InputStreamProvider, targetFile: File): File {
        BitmapFactory.decodeStream(provider.open(), null, options)
        srcHeight = options.outHeight
        srcWidth = options.outWidth

        val options = BitmapFactory.Options()
        options.inSampleSize = computeSize()

        var tagBitmap = BitmapFactory.decodeStream(provider.open(), null, options)
        val stream = ByteArrayOutputStream()

        if (Checker.SINGLE.isJPG(provider.open())) {
            tagBitmap = rotatingImage(tagBitmap!!, Checker.SINGLE.getOrientation(provider.open()))
        }
        tagBitmap!!.compress(
            if (config.focusAlpha) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG,
            60,
            stream
        )
        tagBitmap!!.recycle()
        val fos = FileOutputStream(targetFile)
        fos.write(stream.toByteArray())
        fos.flush()
        fos.close()
        stream.close()
        return targetFile
    }


    private fun computeSize(): Int {
        srcWidth = if (srcWidth % 2 == 1) srcWidth + 1 else srcWidth
        srcHeight = if (srcHeight % 2 == 1) srcHeight + 1 else srcHeight
        val longSide = Math.max(srcWidth, srcHeight)
        val shortSide = Math.min(srcWidth, srcHeight)
        val scale = shortSide.toFloat() / longSide
        return if (scale <= 1 && scale > 0.5625) {
            if (longSide < 1664) {
                1
            } else if (longSide < 4990) {
                2
            } else if (longSide < 10240) {
                4
            } else {
                if (longSide / 1280 == 0) 1 else longSide / 1280
            }
        } else if (scale <= 0.5625 && scale > 0.5) {
            if (longSide / 1280 == 0) 1 else longSide / 1280
        } else {
            Math.ceil(longSide / (1280.0 / scale)).toInt()
        }
    }

    private fun rotatingImage(bitmap: Bitmap, angle: Int): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }

}