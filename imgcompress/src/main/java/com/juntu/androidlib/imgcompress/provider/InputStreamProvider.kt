package com.juntu.androidlib.imgcompress.provider

import java.io.IOException
import java.io.InputStream

/**
 * Created by cj on 2020/9/14.
 * Email:codesexy@163.com
 * Function:
 * desc:
 */
interface InputStreamProvider {

    @Throws(IOException::class)
    fun open(): InputStream

    fun close()

    fun getPath(): String


}