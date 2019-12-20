package cn.iwgang.licenseplatediscern.view

import android.view.GestureDetector
import android.view.MotionEvent


/**
 * 识别View手势监听类
 *
 * Created by iWgang on 19/12/20.
 * https://github.com/iwgang/LicensePlateDiscern
 */
class DiscernViewGestureDetector(private val onGestureListener: OnGestureListener) : GestureDetector.SimpleOnGestureListener() {
    override fun onDoubleTap(e: MotionEvent?): Boolean {
        onGestureListener.onDiscernViewDoubleTap()
        return true
    }
}

interface OnGestureListener {
    fun onDiscernViewDoubleTap()
}