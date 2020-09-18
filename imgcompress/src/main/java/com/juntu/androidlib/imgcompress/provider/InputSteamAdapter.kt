package com.juntu.androidlib.imgcompress.provider

import java.io.IOException
import java.io.InputStream

/**
 * Created by cj on 2020/9/14.
 * Email:codesexy@163.com
 * Function:
 * desc:
 */
abstract class InputSteamAdapter : InputStreamProvider {

    var inputStream: InputStream? = null

    override fun open(): InputStream {
        close()
        inputStream = openInternal()
        return inputStream!!
    }

    override fun close() {
        if (inputStream != null) {
            try {
                inputStream!!.close()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                inputStream = null
            }
        }
    }

    @Throws(IOException::class)
    abstract fun openInternal(): InputStream?
}