package cn.iwgang.licenseplatediscern.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.ImageFormat
import android.hardware.Camera
import android.os.AsyncTask
import android.util.AttributeSet
import android.util.Size
import android.view.GestureDetector
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.FrameLayout
import cn.iwgang.licenseplatediscern.LicensePlateDiscernCore
import cn.iwgang.licenseplatediscern.LicensePlateInfo
import cn.iwgang.licenseplatediscern.R
import java.io.IOException
import kotlin.math.abs


typealias OnDiscernListener = (LicensePlateInfo) -> Unit

/**
 * 车牌识别 View
 *
 * Created by iWgang on 19/12/14.
 * https://github.com/iwgang/LicensePlateDiscern
 */
class LicensePlateDiscernView(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int
) : FrameLayout(context, attrs, defStyleAttr), SurfaceHolder.Callback, Camera.PreviewCallback, OnGestureListener {
    private val mGestureDetector by lazy { GestureDetector(context, DiscernViewGestureDetector(this)) }
    private lateinit var mLicensePlateDiscernForeView: LicensePlateDiscernForeView
    private lateinit var mOnTaskDiscernListener: OnTaskDiscernListener

    private var mSurfaceHolder: SurfaceHolder? = null
    private var mCamera: Camera? = null
    private var mDiscernAsyncTask: DiscernAsyncTask? = null
    private var mCanHandleDiscern = true
    private var mOnDiscernListener: OnDiscernListener? = null
    private var mDiscernConfidence: Float = LicensePlateDiscernCore.DEFAULT_CONFIDENCE
    private var mDoubleTapZoom: Boolean = true
    private var mSurfaceWidth: Int = 0
    private var mSurfaceHeight: Int = 0

    constructor(context: Context, attributeSet: AttributeSet) : this(context, attributeSet, 0)

    init {
        initView(context, attrs)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView(context: Context, attrs: AttributeSet) {
        setBackgroundColor(Color.BLACK)

        val surfaceView = SurfaceView(context)
        surfaceView.holder.run {
            setKeepScreenOn(true)
            addCallback(this@LicensePlateDiscernView)
        }
        addView(surfaceView)

        val ta = context.obtainStyledAttributes(attrs, R.styleable.LicensePlateDiscernView)
        mDiscernConfidence = ta.getFloat(R.styleable.LicensePlateDiscernView_lpd_discernConfidence, LicensePlateDiscernCore.DEFAULT_CONFIDENCE)
        mDoubleTapZoom = ta.getBoolean(R.styleable.LicensePlateDiscernView_lpd_doubleTapZoom, true)
        if (mDiscernConfidence > 1 || mDiscernConfidence <= 0) {
            mDiscernConfidence = LicensePlateDiscernCore.DEFAULT_CONFIDENCE
        }
        ta.recycle()

        if (mDoubleTapZoom) {
            surfaceView.setOnTouchListener { _, event ->
                mGestureDetector.onTouchEvent(event)
                true
            }
        }

        mLicensePlateDiscernForeView = LicensePlateDiscernForeView(context, attrs)
        addView(mLicensePlateDiscernForeView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        mOnTaskDiscernListener = object : OnTaskDiscernListener {
            override fun invoke(lpInfo: LicensePlateInfo?) {
                if (null != lpInfo) {
                    mCanHandleDiscern = false
                    mOnDiscernListener?.invoke(lpInfo)
                }
            }
        }
    }

    /**
     * 设置识别回调
     *
     * @param listener OnDiscernListener
     */
    fun setOnDiscernListener(listener: OnDiscernListener) {
        mOnDiscernListener = listener
    }

    /**
     * 重新识别
     */
    fun reDiscern() {
        mCanHandleDiscern = true
    }

    override fun onDiscernViewDoubleTap() {
        if (null == mCamera) return

        val parameters = mCamera!!.parameters
        if (parameters.isZoomSupported) {
            if (parameters.zoom == 0) {
                zoom()
            } else {
                zoomOut()
            }
        }
    }

    override fun onPreviewFrame(data: ByteArray, camera: Camera) {
        val taskStatus = mDiscernAsyncTask?.status
        if (!mCanHandleDiscern || null == mOnDiscernListener || taskStatus == AsyncTask.Status.PENDING || taskStatus == AsyncTask.Status.RUNNING) return

        val size = mCamera!!.parameters.previewSize
        mDiscernAsyncTask = DiscernAsyncTask(
                previewWidth = size.width,
                previewHeight = size.height,
                discernRect = mLicensePlateDiscernForeView.getDiscernRect(),
                discernConfidence = mDiscernConfidence,
                data = data,
                onTaskDiscernListener = mOnTaskDiscernListener
        ).apply { execute() }
    }

    private fun initCamera() {
        if (null == mSurfaceHolder || !hasCameraPermission()) return

        mCamera = Camera.open(0)
        mCamera!!.setDisplayOrientation(90) // 固定为后置摄像头竖屏

        val parameters = mCamera!!.parameters
        val findBestSize = findBestPreviewSize(parameters.supportedPreviewSizes)
        parameters.setPreviewSize(findBestSize.width, findBestSize.height)
        parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
        parameters.previewFormat = ImageFormat.NV21
        val previewFpsRange = findBestPreviewFpsRange(mCamera!!)
        if (previewFpsRange != null) {
            parameters.setPreviewFpsRange(previewFpsRange[Camera.Parameters.PREVIEW_FPS_MIN_INDEX], previewFpsRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX])
        }
        mCamera!!.parameters = parameters

        try {
            mCamera!!.setPreviewDisplay(mSurfaceHolder)
        } catch (e: IOException) {
        }

        mCamera!!.setPreviewCallback(this)
        mCamera!!.startPreview()

        mCanHandleDiscern = true
    }

    /**
     * 开启预览
     */
    fun startPreview() {
        initCamera()
    }

    /**
     * 打开闪光灯
     */
    fun openFlash() {
        if (null != mCamera) {
            val parameters = mCamera!!.parameters
            parameters.flashMode = Camera.Parameters.FLASH_MODE_TORCH
            mCamera!!.parameters = parameters
        }
    }

    /**
     * 关闭闪光灯
     */
    fun closeFlash() {
        if (null != mCamera) {
            val parameters = mCamera!!.parameters
            parameters.flashMode = Camera.Parameters.FLASH_MODE_OFF
            mCamera!!.parameters = parameters
        }
    }

    /**
     * 放大
     * return true 成功，false 失败（有可能因为不支持变焦导致失败）
     */
    private fun zoom(): Boolean {
        if (null == mCamera) return false

        val parameters = mCamera!!.parameters
        if (parameters.isZoomSupported) {
            parameters.zoom = parameters.zoomRatios.size / 2
            mCamera!!.parameters = parameters
            return true
        }
        return false
    }

    /**
     * 缩小（恢复正常）
     * return true 成功，false 失败（有可能因为不支持变焦导致失败）
     */
    private fun zoomOut(): Boolean {
        if (null == mCamera) return false

        val parameters = mCamera!!.parameters
        if (parameters.isZoomSupported) {
            parameters.zoom = 0
            mCamera!!.parameters = parameters
            return true
        }
        return false
    }


    /**
     * 查找最好的预览尺寸
     *
     * @param supportSizeList supportSizeList
     * @return Size
     */
    private fun findBestPreviewSize(supportSizeList: List<Camera.Size>): Size {
        var bestWidth = 0
        var bestHeight = 0
        var lastSupportDiff = Integer.MAX_VALUE
        for (previewSize in supportSizeList) {
            val curSupportWidth = previewSize.width
            val curSupportHeight = previewSize.height
            val curSupportDiff = abs(curSupportWidth - mSurfaceHeight) + abs(curSupportHeight - mSurfaceWidth)
            if (curSupportDiff == 0) {
                bestWidth = curSupportWidth
                bestHeight = curSupportHeight
                break
            } else if (curSupportDiff < lastSupportDiff) {
                bestWidth = curSupportWidth
                bestHeight = curSupportHeight
                lastSupportDiff = curSupportDiff
            }
        }

        return if (bestWidth > 0 && bestHeight > 0) Size(bestWidth, bestHeight) else Size(mSurfaceHeight shr 3 shl 3, mSurfaceWidth shr 3 shl 3)
    }

    /**
     * 查询最终预览 Fps 范围
     */
    private fun findBestPreviewFpsRange(camera: Camera): IntArray? {
        val defDesiredPreviewFps = 60.0f
        val desiredPreviewFpsScaled = (defDesiredPreviewFps * 1000.0f).toInt()
        var selectedFpsRange: IntArray? = null
        var minDiff = Integer.MAX_VALUE
        val previewFpsRangeList = camera.parameters.supportedPreviewFpsRange
        for (range in previewFpsRangeList) {
            val deltaMin = desiredPreviewFpsScaled - range[Camera.Parameters.PREVIEW_FPS_MIN_INDEX]
            val deltaMax = desiredPreviewFpsScaled - range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]
            val diff = abs(deltaMin) + abs(deltaMax)
            if (diff < minDiff) {
                selectedFpsRange = range
                minDiff = diff
            }
        }
        return selectedFpsRange
    }

    private fun hasCameraPermission(): Boolean {
        return context.checkPermission(Manifest.permission.CAMERA, android.os.Process.myPid(), android.os.Process.myUid()) == PackageManager.PERMISSION_GRANTED
    }

    override fun surfaceCreated(holder: SurfaceHolder) {}

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        mSurfaceHolder = holder
        mSurfaceWidth = width
        mSurfaceHeight = height
        initCamera()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        if (mCamera != null) {
            mCanHandleDiscern = false
            mCamera!!.setPreviewCallback(null)
            mCamera!!.release()
            mCamera = null
            mSurfaceHolder = null
        }
    }

}