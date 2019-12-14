package cn.iwgang.licenseplatediscern.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import cn.iwgang.licenseplatediscern.R

/**
 * 车牌识别前置View（遮罩、识别框、扫码线等）
 *
 * Created by iWgang on 19/12/15.
 * https://github.com/iwgang/LicensePlateDiscern
 */
class LicensePlateDiscernForeView(context: Context, attrs: AttributeSet) : View(context) {
    private var mMaskPaint: Paint? = null // 遮罩画笔
    private var mAnglePaint: Paint? = null // 4个角的对焦角线画笔
    private var mBorderPaint: Paint? = null // 边框画笔
    private var mScanLinePaint: Paint? = null // 扫码丝画笔

    private lateinit var mDiscernRect: Rect // 识别矩形

    private var mScreenHeight: Int = 0
    private var mScreenWidth: Int = 0
    private var mAngleLength: Int = 0
    private var mAngleStrokeWidth: Int = 0
    private var mAngleOffset: Int = 0
    private var isShowScanLine: Boolean = false
    private var mInitializeScanLineY: Int = 0
    private var mCurScanLineY: Int = 0

    init {
        init(context, attrs)
    }

    @SuppressLint("CustomViewStyleable")
    private fun init(context: Context, attrs: AttributeSet) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.LicensePlateDiscernView)
        val discernRectTopMargin = ta.getDimension(R.styleable.LicensePlateDiscernView_lpd_discernRectTopMargin, 300f).toInt()
        val discernRectLRMargin = ta.getDimension(R.styleable.LicensePlateDiscernView_lpd_discernRectLRMargin, 30f).toInt()
        val discernRectWidth = ta.getDimension(R.styleable.LicensePlateDiscernView_lpd_discernRectWidth, 0f).toInt()
        val discernRectHeight = ta.getDimension(R.styleable.LicensePlateDiscernView_lpd_discernRectHeight, 200f).toInt()
        val maskColor = ta.getColor(R.styleable.LicensePlateDiscernView_lpd_maskColor, 0x50000000)
        mAngleLength = ta.getDimension(R.styleable.LicensePlateDiscernView_lpd_angleLength, 0f).toInt()
        mAngleStrokeWidth = ta.getDimension(R.styleable.LicensePlateDiscernView_lpd_angleStrokeWidth, 0f).toInt()
        mAngleOffset = ta.getDimension(R.styleable.LicensePlateDiscernView_lpd_angleOffset, 0f).toInt()
        val angleColor = ta.getColor(R.styleable.LicensePlateDiscernView_lpd_angleColor, Color.GREEN)
        val borderSize = ta.getDimension(R.styleable.LicensePlateDiscernView_lpd_borderSize, 0f).toInt()
        val borderColor = ta.getColor(R.styleable.LicensePlateDiscernView_lpd_borderColor, Color.WHITE)
        isShowScanLine = ta.getBoolean(R.styleable.LicensePlateDiscernView_lpd_isShowScanLine, true)
        val scanLineSize = ta.getDimension(R.styleable.LicensePlateDiscernView_lpd_scanLineSize, 3f).toInt()
        val scanLineColor = ta.getColor(R.styleable.LicensePlateDiscernView_lpd_scanLineColor, Color.GREEN)
        ta.recycle()

        if (maskColor != Color.TRANSPARENT) {
            mMaskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = maskColor
            }
        }

        if (borderSize > 0) {
            mBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                color = borderColor
                strokeWidth = borderSize.toFloat()
            }
        }

        if (mAngleLength > 0 && mAngleStrokeWidth > 0) {
            mAnglePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = angleColor
            }
        }

        mScanLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = scanLineColor
            strokeWidth = scanLineSize.toFloat()
        }

        mScreenHeight = context.resources.displayMetrics.heightPixels
        mScreenWidth = context.resources.displayMetrics.widthPixels
        mDiscernRect = genDiscernRect(discernRectTopMargin, if (discernRectWidth > 0) discernRectWidth else mScreenWidth - discernRectLRMargin * 2, discernRectHeight)
        mInitializeScanLineY = mDiscernRect.top + mAngleStrokeWidth
        mCurScanLineY = mInitializeScanLineY
    }

    private fun genDiscernRect(topMargin: Int, width: Int, height: Int): Rect {
        val left = (mScreenWidth - width) / 2
        return Rect().apply {
            set(left, topMargin, left + width, topMargin + height)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawMask(canvas) // 遮罩
        drawBorder(canvas) // 边框
        drawAngle(canvas) // 4个角的对焦角线
        if (isShowScanLine) {
            scanLine(canvas) // 扫描线
        } else {
            postInvalidate()
        }
    }

    private fun drawMask(canvas: Canvas) {
        if (null == mMaskPaint) return

        canvas.drawRect(0f, 0f, mScreenWidth.toFloat(), mDiscernRect.top.toFloat(), mMaskPaint!!) // top
        canvas.drawRect(0f, mDiscernRect.bottom.toFloat(), mScreenWidth.toFloat(), mScreenHeight.toFloat(), mMaskPaint!!) // bottom
        canvas.drawRect(0f, mDiscernRect.top.toFloat(), mDiscernRect.left.toFloat(), mDiscernRect.bottom.toFloat(), mMaskPaint!!) // right
        canvas.drawRect(mDiscernRect.right.toFloat(), mDiscernRect.top.toFloat(), mScreenWidth.toFloat(), mDiscernRect.bottom.toFloat(), mMaskPaint!!) // left
    }

    private fun drawBorder(canvas: Canvas) {
        if (null == mBorderPaint) return

        canvas.drawRect(mDiscernRect, mBorderPaint!!)
    }

    private fun drawAngle(canvas: Canvas) {
        if (null == mAnglePaint) return

        val top = mDiscernRect.top + mAngleOffset
        val bottom = mDiscernRect.bottom - mAngleOffset
        val left = mDiscernRect.left + mAngleOffset
        val right = mDiscernRect.right - mAngleOffset

        // top left
        canvas.drawRect(left.toFloat(), top.toFloat(), (left + mAngleLength).toFloat(), (top + mAngleStrokeWidth).toFloat(), mAnglePaint!!) // 水平
        canvas.drawRect(left.toFloat(), (top + mAngleStrokeWidth).toFloat(), (left + mAngleStrokeWidth).toFloat(), (top + mAngleLength).toFloat(), mAnglePaint!!) // 垂直

        // bottom left
        canvas.drawRect(left.toFloat(), (bottom - mAngleStrokeWidth).toFloat(), (left + mAngleLength).toFloat(), bottom.toFloat(), mAnglePaint!!) // 水平
        canvas.drawRect(left.toFloat(), (bottom - mAngleLength).toFloat(), (left + mAngleStrokeWidth).toFloat(), (bottom - mAngleStrokeWidth).toFloat(), mAnglePaint!!) // 垂直

        // top right
        canvas.drawRect((right - mAngleLength).toFloat(), top.toFloat(), right.toFloat(), (top + mAngleStrokeWidth).toFloat(), mAnglePaint!!) // 水平
        canvas.drawRect((right - mAngleStrokeWidth).toFloat(), (top + mAngleStrokeWidth).toFloat(), right.toFloat(), (top + mAngleLength).toFloat(), mAnglePaint!!) // 垂直

        // bottom right
        canvas.drawRect((right - mAngleLength).toFloat(), (bottom - mAngleStrokeWidth).toFloat(), right.toFloat(), bottom.toFloat(), mAnglePaint!!) // 水平
        canvas.drawRect((right - mAngleStrokeWidth).toFloat(), (bottom - mAngleLength).toFloat(), right.toFloat(), (bottom - mAngleStrokeWidth).toFloat(), mAnglePaint!!) // 垂直
    }

    private fun scanLine(canvas: Canvas) {
        mCurScanLineY += 5
        if (mCurScanLineY >= mDiscernRect.bottom - mAngleStrokeWidth) {
            mCurScanLineY = mInitializeScanLineY
        }

        val left = mDiscernRect.left + mAngleStrokeWidth
        val right = mDiscernRect.right - mAngleStrokeWidth

        canvas.drawLine(left.toFloat(), mCurScanLineY.toFloat(), right.toFloat(), mCurScanLineY.toFloat(), mScanLinePaint!!)
        postInvalidateDelayed(1, left, mDiscernRect.top, right, mDiscernRect.bottom)
    }

    /**
     * 获取识别矩形
     * @return Rect
     */
    fun getDiscernRect() = mDiscernRect

}