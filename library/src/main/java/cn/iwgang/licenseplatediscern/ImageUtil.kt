package cn.iwgang.licenseplatediscern

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import java.io.File
import kotlin.math.roundToInt

/**
 * 图片处理工具类
 *
 * Created by iWgang on 19/12/16.
 * https://github.com/iwgang/LicensePlateDiscern
 */
class ImageUtil {
    companion object {

        /**
         * 图片压缩
         * @param picPath   需要压缩的原图片路径
         * @param reqWidth  请求理想最大宽高
         * @param reqHeight 请求理想最大高度
         */
        fun compressor(picPath: String, reqWidth: Int, reqHeight: Int): Bitmap? {
            val picFile = File(picPath)
            // 如果原图都小于1024kb，直接返回
            if (picFile.length() <= 1024) return BitmapFactory.decodeFile(picPath)

            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.RGB_565
            options.inJustDecodeBounds = true // 只读取图片，不加载到内存中
            BitmapFactory.decodeFile(picPath, options)
            // 计算 inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

            options.inJustDecodeBounds = false // 加载到内存中

            // 根据缩放比例重新生成 Bitmap
            var finalBitmap = BitmapFactory.decodeFile(picPath, options)
            // 修正旋转
            if (null != finalBitmap) {
                finalBitmap = fixRotation(finalBitmap, picPath)
            }
            return finalBitmap
        }

        /**
         * 修复旋转角度
         * @param bitmap  需要修复旋转角度的 bitmap
         * @param picPath 需要压缩的原图片路径
         */
        private fun fixRotation(bitmap: Bitmap, picPath: String): Bitmap? {
            val exif = ExifInterface(picPath)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
            if (0 == orientation) {
                return bitmap
            }
            val matrix = Matrix()
            when (orientation) {
                6 -> matrix.postRotate(90f)
                3 -> matrix.postRotate(180f)
                8 -> matrix.postRotate(270f)
            }
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }

        /**
         * 计算缩放比例
         * @param options   BitmapFactory.Options
         * @param reqWidth  请求理想最大宽高
         * @param reqHeight 请求理想最大高度
         */
        private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
            val width = options.outWidth
            val height = options.outHeight
            var inSampleSize = 1
            if (height > reqHeight || width > reqWidth) {
                // 计算图片高度和我们需要高度的最接近比例值
                val heightRatio = (height.toFloat() / reqHeight.toFloat()).roundToInt()
                // 宽度比例值
                val widthRatio = (width.toFloat() / reqWidth.toFloat()).roundToInt()
                // 取比例值中的较大值作为inSampleSize
                inSampleSize = if (heightRatio > widthRatio) heightRatio else widthRatio
            }
            return inSampleSize
        }

    }
}