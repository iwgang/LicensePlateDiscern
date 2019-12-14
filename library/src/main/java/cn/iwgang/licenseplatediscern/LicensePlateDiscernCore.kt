package cn.iwgang.licenseplatediscern

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import java.io.File

/**
 * 车牌识别核心类
 *
 * Created by iWgang on 19/12/14.
 * https://github.com/iwgang/LicensePlateDiscern
 */
object LicensePlateDiscernCore {
    private var mPlateRecognitionHandle: Long? = null
    private val SDCARD_DIR by lazy { Environment.getExternalStorageDirectory().absolutePath + File.separator + "xxootest" }

    /**
     * 车牌识别训练模型初始化（需要存储权限，建议在识别页面再调用）
     *
     * @param context  Context
     * @param savePath 车牌识别训练模型文件存放本地的位置
     */
    private fun initRecognizer(context: Context, savePath: String) {
        AssetsUtil.copyAssets(context, "lprmodel", savePath)
        mPlateRecognitionHandle = LprDiscernCore.init(
                savePath + File.separator + "cascade.xml",
                savePath + File.separator + "HorizonalFinemapping.prototxt",
                savePath + File.separator + "HorizonalFinemapping.caffemodel",
                savePath + File.separator + "Segmentation.prototxt",
                savePath + File.separator + "Segmentation.caffemodel",
                savePath + File.separator + "CharacterRecognization.prototxt",
                savePath + File.separator + "CharacterRecognization.caffemodel",
                savePath + File.separator + "SegmenationFree-Inception.prototxt",
                savePath + File.separator + "SegmenationFree-Inception.caffemodel"
        )
    }

    fun onResume(context: Context) {
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(
                    OpenCVLoader.OPENCV_VERSION_3_4_0,
                    context,
                    object : BaseLoaderCallback(context.applicationContext) {})
        } else {
            initRecognizer(context.applicationContext, SDCARD_DIR)
        }
    }

    fun discern(bitmap: Bitmap): String? {
        if (null == mPlateRecognitionHandle) return null

        val m = Mat(bitmap.width, bitmap.height, CvType.CV_8UC4)
        Utils.bitmapToMat(bitmap, m)
        return LprDiscernCore.discern(m.nativeObjAddr, mPlateRecognitionHandle!!)
    }

}