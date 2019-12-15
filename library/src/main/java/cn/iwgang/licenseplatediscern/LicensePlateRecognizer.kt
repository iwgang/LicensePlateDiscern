package cn.iwgang.licenseplatediscern

import android.content.Context
import android.graphics.Bitmap
import cn.iwgang.licenseplatediscern.lpr.LprDiscernUtil
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader

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

    fun onResume() {
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, context.applicationContext, mLoaderCallback)
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

}