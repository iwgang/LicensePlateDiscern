package cn.iwgang.licenseplatediscern.lpr

import android.content.Context
import android.graphics.Bitmap
import android.text.TextUtils
import android.util.Log
import cn.iwgang.licenseplatediscern.LicensePlateInfo
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import java.io.File
import java.io.FileOutputStream

/**
 * LPR车牌识别库辅助类
 *
 * Created by iWgang on 19/12/15.
 * https://github.com/iwgang/LicensePlateDiscern
 */
object LprDiscernHelper {
    private const val ASSETS_MODEL_DIR_NAME = "lprmodel"
    private var mLprDiscernHandle: Long? = null


    /**
     * 初始化
     * @param context context
     */
    fun init(context: Context) {
        Thread {
            copyAssetsDiscernModel(context)
            mLprDiscernHandle = discernPrepare(context)
            if (OpenCVLoader.initDebug()) {
                Log.d("LicensePlateDiscern", "OpenCV init success")
            } else {
                Log.d("LicensePlateDiscern", "OpenCV init fail")
            }
        }.start()
    }

    /**
     * 识别
     * @param bitmap        需要识别车牌的 bitmap
     * @param confidence    可信度 0 - 1
     * @return Array<LicensePlateInfo> 车牌信息列表
     */
    fun discern(bitmap: Bitmap, confidence: Float): Array<LicensePlateInfo>? {
        if (null != mLprDiscernHandle) {
            val m = Mat(bitmap.width, bitmap.height, CvType.CV_8UC4)
            Utils.bitmapToMat(bitmap, m)
            val oriLp = LprDiscernCore.discern(m.nativeObjAddr, mLprDiscernHandle!!, confidence)
            if (!TextUtils.isEmpty(oriLp)) {
                return if (oriLp!!.contains(",")) {
                    oriLp.split(",").filter { !TextUtils.isEmpty(it) }.mapNotNull { convertLicensePlateInfo(it) }.toTypedArray()
                } else {
                    val retLpInfo = convertLicensePlateInfo(oriLp)
                    if (null != retLpInfo) arrayOf(retLpInfo) else null
                }
            }
        }
        return null
    }


    /**
     * 转换成 LicensePlateInfo 对象
     * @param infoStr 识别结果字符串 eg：川A888888:0.98
     * @return LicensePlateInfo 车牌信息
     */
    private fun convertLicensePlateInfo(infoStr: String): LicensePlateInfo? {
        val infoStrArray = infoStr.split(":")
        val fixLp = fixLicensePlate(infoStrArray[0])
        return if (!TextUtils.isEmpty(fixLp)) LicensePlateInfo(fixLp!!, infoStrArray[1].toFloat()) else null
    }

    /**
     * 校正车牌
     * @param lp 原车牌号
     * @return String 校正后的车牌号
     */
    private fun fixLicensePlate(lp: String): String? {
        // 校正第2位是数字的，如 川1A888888
        if (lp[1].isDigit()) {
            return if (lp.length >= 8) "${lp[0]}${lp.substring(2, lp.length)}" else null
        }
        return lp
    }

    /**
     * 识别预处理
     * @return Long LprDiscernHandle
     */
    private fun discernPrepare(context: Context): Long? {
        val cacheDir = "${context.cacheDir}${File.separator}"
        return LprDiscernCore.init(
                cacheDir + getDiscernModelPath("cascade.xml"),
                cacheDir + getDiscernModelPath("HorizonalFinemapping.prototxt"),
                cacheDir + getDiscernModelPath("HorizonalFinemapping.caffemodel"),
                cacheDir + getDiscernModelPath("Segmentation.prototxt"),
                cacheDir + getDiscernModelPath("Segmentation.caffemodel"),
                cacheDir + getDiscernModelPath("CharacterRecognization.prototxt"),
                cacheDir + getDiscernModelPath("CharacterRecognization.caffemodel"),
                cacheDir + getDiscernModelPath("SegmenationFree-Inception.prototxt"),
                cacheDir + getDiscernModelPath("SegmenationFree-Inception.caffemodel")
        )
    }

    /**
     * 复制Assets中的识别模块
     * @param context Context
     */
    private fun copyAssetsDiscernModel(context: Context) {
        val savePathFile = File("${context.cacheDir}${File.separator}$ASSETS_MODEL_DIR_NAME")

        if (savePathFile.exists()) return

        savePathFile.mkdirs()

        try {
            context.assets.list(ASSETS_MODEL_DIR_NAME)?.forEach { fileName ->
                val output = FileOutputStream(File(savePathFile, fileName))
                val inputStream = context.assets.open(getDiscernModelPath(fileName))
                inputStream.copyTo(output)
                inputStream.close()
                output.flush()
                output.close()
            }
        } catch (e: Exception) {
        }
    }

    /**
     * 获取识别模型路径
     * @param fileName 识别模型文件名称
     * @return String 识别模型路径
     */
    private fun getDiscernModelPath(fileName: String) = "$ASSETS_MODEL_DIR_NAME${File.separator}$fileName"

}