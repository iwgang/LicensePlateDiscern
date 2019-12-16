package cn.iwgang.licenseplatediscern.view

import android.graphics.*
import android.os.AsyncTask
import cn.iwgang.licenseplatediscern.LicensePlateDiscernCore
import java.io.ByteArrayOutputStream

typealias OnTaskDiscernListener = (String?) -> Unit

/**
 * 车牌识别处理任务
 *
 * Created by iWgang on 19/12/14.
 * https://github.com/iwgang/LicensePlateDiscern
 */
class DiscernAsyncTask constructor(
        private val previewWidth: Int,
        private val previewHeight: Int,
        private val discernRect: Rect,
        private val data: ByteArray,
        private val onTaskDiscernListener: OnTaskDiscernListener
) : AsyncTask<Void, Void, String>() {

    override fun doInBackground(vararg params: Void): String? {
        try {
            val image = YuvImage(data, ImageFormat.NV21, previewWidth, previewHeight, null)
            val stream = ByteArrayOutputStream(data.size)
            if (!image.compressToJpeg(Rect(0, 0, previewWidth, previewHeight), 100, stream)) {
                return null
            }
            val bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size())
            stream.close()

            val matrix = Matrix()
            matrix.postRotate(90f)
            val bitmap = Bitmap.createBitmap(bmp, discernRect.top, discernRect.left, discernRect.bottom - discernRect.top, discernRect.right - discernRect.left, matrix, true)
            return LicensePlateDiscernCore.discern(bitmap)?.firstOrNull()
        } catch (ex: Exception) {
        }
        return null
    }

    override fun onPostExecute(str: String?) {
        super.onPostExecute(str)
        onTaskDiscernListener(str)
    }
}