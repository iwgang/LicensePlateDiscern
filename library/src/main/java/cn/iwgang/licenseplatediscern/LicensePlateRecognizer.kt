package cn.iwgang.licenseplatediscern

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import cn.iwgang.licenseplatediscern.lpr.LprDiscernUtil
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import java.io.File

/**
 * 车牌识别的识别器
 *
 * Created by iWgang on 19/12/15.
 * https://github.com/iwgang/LicensePlateDiscern
 */
class LicensePlateRecognizer(val context: Context) {
    private var mLprDiscernHandle: Long? = null
    private val mLoaderCallback by lazy {
        object : BaseLoaderCallback(context.applicationContext) {
            override fun onManagerConnected(status: Int) {
                if (status == LoaderCallbackInterface.SUCCESS) {
                    mLprDiscernHandle = LprDiscernUtil.discernPrepare(context)
                } else {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    fun discern(bitmap: Bitmap): Array<String>? {
        if (null == mLprDiscernHandle) return null

        return LprDiscernUtil.discern(bitmap, mLprDiscernHandle!!)
    }

    fun discern(picPath: String): Array<String>? {
        if (null == mLprDiscernHandle) return null

        // 检查权限及文件是否存在
        if (context.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, android.os.Process.myPid(), android.os.Process.myUid()) == PackageManager.PERMISSION_GRANTED && File(picPath).exists()) {
            val bitmap = ImageUtil.compressor(picPath, 1080, 1920)
            if (null != bitmap) {
                return discern(bitmap)
            }
        }
        return null
    }

    fun onResume() {
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, context.applicationContext, mLoaderCallback)
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

}