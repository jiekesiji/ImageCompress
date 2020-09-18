package com.juntu.androidlib.imgcompress.entity

/**
 * Created by cj on 2020/9/14.
 * Email:codesexy@163.com
 * Function:
 * desc:
 *      T 类型可以是 pathString File Uri
 */
data class Image<T>(
    val originalPath: T? = null,//原始文件
    val shouldCompressed: Boolean = true,//是否需要压缩
    val compressedPath: String = ""//压缩后存放的地址
)