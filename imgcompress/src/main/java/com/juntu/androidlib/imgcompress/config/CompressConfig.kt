package com.juntu.androidlib.imgcompress.config

import java.io.Serializable
import kotlin.math.max

/**
 * Created by cj on 2020/9/14.
 * Email:codesexy@163.com
 * Function:
 * desc:
 */

class CompressConfig private constructor(
    var unCompressMinPixel: Int,
    var unCompressNormalPixel: Int,
    var maxPixel: Int,
    var maxSize: Int,
    var isEnablePixelCompress: Boolean,
    var isEnableQualityCompress: Boolean,
    var cacheDir: String?,
    var focusAlpha: Boolean,
    var ignoreSize: Int,
    var engineType: EngineType
) : Serializable {

    class Builder() {
        /**
         * 最小像素不压缩
         */
        var unCompressMinPixel: Int = 1000

        /**
         * 标准像素不压缩
         */
        var unCompressNormalPixel: Int = 2000

        /**
         * 长或宽不超过的最大像素,单位px
         */
        var maxPixel: Int = 1200

        /**
         * 压缩到的最大大小，单位B
         */
        var maxSize: Int = 200 * 1024

        /**
         * 是否启用像素压缩
         */
        var isEnablePixelCompress: Boolean = true

        /**
         * 是否启用质量压缩
         */
        var isEnableQualityCompress: Boolean = true


        /**
         * 压缩后缓存图片目录，非文件路径
         */
        var cacheDir: String? = ""


        /**
         * 是否保留Alpha通道   PNG有透明通道  JPEG没有透明通道
         */
        var focusAlpha: Boolean = false

        /**
         * 小于某个数值后就不压缩
         */
        var ignoreSize: Int = 100

        /**
         * 设置引擎的类型
         */
        var engineType: EngineType = EngineType.LuBan

        fun create(): CompressConfig = CompressConfig(
            unCompressMinPixel, unCompressNormalPixel,
            maxPixel, maxSize,
            isEnablePixelCompress, isEnableQualityCompress,
            cacheDir, focusAlpha, ignoreSize, engineType
        )

        fun setNnCompressMinPixel(unCompressMinPixel: Int): Builder =
            apply { this.unCompressMinPixel = unCompressMinPixel }


        fun setNnCompressNormalPixel(unCompressNormalPixel: Int): Builder =
            apply { this.unCompressNormalPixel = unCompressNormalPixel }


        fun setMaxPixel(maxPixel: Int): Builder =
            apply { this.maxPixel = maxPixel }


        fun setMaxSize(maxSize: Int): Builder =
            apply { this.maxSize = maxSize }


        fun enablePixelCompress(enablePixelCompress: Boolean): Builder =
            apply { this.isEnablePixelCompress = enablePixelCompress }

        fun enableQualityCompress(enableQualityCompress: Boolean): Builder =
            apply { this.isEnableQualityCompress = enableQualityCompress }

        fun setCacheDir(cacheDir: String?): Builder =
            apply { this.cacheDir = cacheDir }

        fun setFocusAlpha(focusAlpha: Boolean): Builder =
            apply { this.focusAlpha = focusAlpha }

        fun ignoreBy(ignoreSize: Int): Builder =
            apply { this.ignoreSize = ignoreSize }

        fun setEngineType(engineType: EngineType) =
            apply { this.engineType = engineType }
    }


}

enum class EngineType {
    /**
     * 使用鲁班压缩引擎进行压缩
     * 注意 使用这种压缩引擎的时候  自定义的配置
     * 不能够实用
     */
    LuBan,

    /**
     * 使用自定义的引擎
     *
     */
    Custom
}

