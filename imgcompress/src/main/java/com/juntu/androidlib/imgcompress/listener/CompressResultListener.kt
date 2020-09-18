package com.juntu.androidlib.imgcompress.listener


import com.juntu.androidlib.imgcompress.entity.Image

/**
 * Created by cj on 2020/9/14.
 * Email:codesexy@163.com
 * Function:
 * desc:
 */

/**
 * 单张压缩结果通知上部应用
 */
interface CompressListener<T> {

    fun onCompressSuccess(image: Image<T>)

    fun onCompressFailed(image: Image<T>, errorMsg: String)
}


internal interface Compress {

    /**
     * 核心压缩
     */
    fun compress()
}


interface CompressResultListener<T> {

    fun onStart() {}

    /**
     * 整体压缩完成以后
     */
    fun onCompressComplete(
        successImages: List<Image<T>>,
        failedImages: List<Image<T>>?,
        errMsg: List<Exception>?
    )


    fun onProgress(progress: Float) {}

    /**
     * 整个压缩停止了
     * 这个地方可以消失弹框
     */
    fun onStop() {}

}

interface OnRenameListener {

    fun rename(filePath: String?): String? = filePath
}