package cn.iwgang.licenseplatediscern

import android.content.Context
import cn.iwgang.licenseplatediscern.lpr.LprDiscernUtil

/**
 * 车牌识别核心类
 *
 * Created by iWgang on 19/12/14.
 * https://github.com/iwgang/LicensePlateDiscern
 */
object LicensePlateDiscernCore {

    /**
     * 初始化，建议在 Application 的 onCreate 中调用
     *
     * @param context  Context
     */
    fun init(context: Context) {
        LprDiscernUtil.init(context.applicationContext)
    }

}