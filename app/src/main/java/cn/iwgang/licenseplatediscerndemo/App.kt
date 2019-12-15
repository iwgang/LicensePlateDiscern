package cn.iwgang.licenseplatediscerndemo

import android.app.Application
import cn.iwgang.licenseplatediscern.LicensePlateDiscernCore

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        LicensePlateDiscernCore.init(this)
    }

}