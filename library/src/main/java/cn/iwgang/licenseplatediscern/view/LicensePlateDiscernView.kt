package cn.iwgang.licenseplatediscern.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.ImageFormat
import android.hardware.Camera
import android.os.AsyncTask
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.FrameLayout
import cn.iwgang.licenseplatediscern.LicensePlateRecognizer
import java.io.IOException
import kotlin.math.abs

typealias OnDiscernListener = (String) -> Unit

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
) : FrameLayout(context, attrs, defStyleAttr), SurfaceHolder.Callback, Camera.PreviewCallback {
    private val mLicensePlateRecognizer by lazy { LicensePlateRecognizer(context) }
    private lateinit var mLicensePlateDiscernForeView: LicensePlateDiscernForeView
    private lateinit var mOnTaskDiscernListener: OnTaskDiscernListener

    private var mSurfaceHolder: SurfaceHolder? = null
    private var mCamera: Camera? = null
    private var mDiscernAsyncTask: DiscernAsyncTask? = null
    private var mCanHandleDiscern = true
    private var mOnDiscernListener: OnDiscernListener? = null


    constructor(context: Context, attributeSet: AttributeSet) : this(context, attributeSet, 0)

    init {
        initView(context, attrs)
    }

    private fun initView(context: Context, attrs: AttributeSet) {
        setBackgroundColor(Color.BLACK)

        val surfaceView = SurfaceView(context)
        surfaceView.holder.run {
            setKeepScreenOn(true)
            addCallback(this@LicensePlateDiscernView)
        }
        addView(surfaceView)

        mLicensePlateDiscernForeView = LicensePlateDiscernForeView(context, attrs)
        addView(mLicensePlateDiscernForeView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        mOnTaskDiscernListener = object : OnTaskDiscernListener {
            override fun invoke(lp: String?) {
                if (!TextUtils.isEmpty(lp)) {
                    mCanHandleDiscern = false
                    mOnDiscernListener?.invoke(lp!!)
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

    fun onResume() {
        mLicensePlateRecognizer.onResume()
    }

    override fun onPreviewFrame(data: ByteArray, camera: Camera) {
        val taskStatus = mDiscernAsyncTask?.status
        if (!mCanHandleDiscern || null == mOnDiscernListener || taskStatus == AsyncTask.Status.PENDING || taskStatus == AsyncTask.Status.RUNNING) return

        val size = mCamera!!.parameters.previewSize
        mDiscernAsyncTask = DiscernAsyncTask(
                previewWidth = size.width,
                previewHeight = size.height,
                discernRect = mLicensePlateDiscernForeView.getDiscernRect(),
                data = data,
                onTaskDiscernListener = mOnTaskDiscernListener,
                licensePlateRecognizer = mLicensePlateRecognizer
        ).apply { execute() }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        mSurfaceHolder = holder
        initCamera()
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
    }


    fun startPreview() {
        initCamera()
    }

    /**
     * 查找最好的预览尺寸
     *
     * @param supportSizeList supportSizeList
     * @return Size
     */
    private fun findBestPreviewSize(supportSizeList: List<Camera.Size>): Size {
        val displayMetrics = resources.displayMetrics
        var bestWidth = 0
        var bestHeight = 0
        var lastSupportDiff = Integer.MAX_VALUE
        for (previewSize in supportSizeList) {
            val curSupportWidth = previewSize.width
            val curSupportHeight = previewSize.height
            val curSupportDiff = abs(curSupportWidth - displayMetrics.heightPixels) + abs(curSupportHeight - displayMetrics.widthPixels)
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

        return if (bestWidth > 0 && bestHeight > 0) Size(bestWidth, bestHeight) else Size(displayMetrics.heightPixels shr 3 shl 3, displayMetrics.widthPixels shr 3 shl 3)
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

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        if (mCamera != null) {
            mCanHandleDiscern = false
            mCamera!!.setPreviewCallback(null)
            mCamera!!.release()
            mCamera = null
        }
    }

}