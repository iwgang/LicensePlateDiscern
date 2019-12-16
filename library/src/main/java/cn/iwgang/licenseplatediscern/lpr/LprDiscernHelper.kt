package cn.iwgang.licenseplatediscern.lpr

import android.content.Context
import android.graphics.Bitmap
import android.text.TextUtils
import android.util.Log
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
     * @return Array<String> 字牌号列表
     */
    fun discern(bitmap: Bitmap): Array<String>? {
        if (null != mLprDiscernHandle) {
            val m = Mat(bitmap.width, bitmap.height, CvType.CV_8UC4)
            Utils.bitmapToMat(bitmap, m)
            val oriLp = LprDiscernCore.discern(m.nativeObjAddr, mLprDiscernHandle!!)
            if (!TextUtils.isEmpty(oriLp)) {
                return if (oriLp!!.contains(",")) {
                    oriLp.split(",").filter { !TextUtils.isEmpty(it) }.toTypedArray()
                } else {
                    arrayOf(oriLp)
                }
            }
        }
        return null
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

    private fun getDiscernModelPath(fileName: String) = "$ASSETS_MODEL_DIR_NAME${File.separator}$fileName"

}