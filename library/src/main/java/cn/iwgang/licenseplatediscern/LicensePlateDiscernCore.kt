package cn.iwgang.licenseplatediscern

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import cn.iwgang.licenseplatediscern.lpr.LprDiscernHelper
import java.io.File

/**
 * 车牌识别核心类
 *
 * Created by iWgang on 19/12/14.
 * https://github.com/iwgang/LicensePlateDiscern
 */
class LicensePlateDiscernCore {

    companion object {
        /** 默认可信度 */
        const val DEFAULT_CONFIDENCE = 0.9F

        /**
         * 初始化，建议在 Application 的 onCreate 中调用
         * @param context  Context
         */
        fun init(context: Context) {
            LprDiscernHelper.init(context.applicationContext)
        }

        /**
         * 识别
         * @param bitmap        需要识别车牌的 bitmap
         * @param confidence    可信度 0 - 1
         * @return Array<LicensePlateInfo> 车牌信息列表
         */
        fun discern(bitmap: Bitmap, confidence: Float = DEFAULT_CONFIDENCE) = LprDiscernHelper.discern(bitmap, confidence)

        /**
         * 识别
         * @param context    Context
         * @param picPath    需要识别车牌的图片路径
         * @param confidence 可信度 0 - 1
         * @return Array<LicensePlateInfo> 车牌信息列表
         */
        fun discern(context: Context, picPath: String, confidence: Float = DEFAULT_CONFIDENCE): Array<LicensePlateInfo>? {
            // 检查权限及文件是否存在
            if (context.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, android.os.Process.myPid(), android.os.Process.myUid()) == PackageManager.PERMISSION_GRANTED && File(picPath).exists()) {
                val bitmap = ImageUtil.compressor(picPath, 2000, 2000)
                if (null != bitmap) {
                    return discern(bitmap, confidence)
                }
            }
            return null
        }
    }

}