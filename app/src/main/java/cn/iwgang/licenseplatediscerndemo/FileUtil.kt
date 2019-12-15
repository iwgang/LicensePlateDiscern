package cn.iwgang.licenseplatediscerndemo

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import java.io.File


class FileUtil {
    companion object {
        fun getFileFromUri(uri: Uri, context: Context): String? {
            return when (uri.scheme) {
                "content" -> getFileFromContentUri(uri, context)
                "file" -> File(uri.path).absolutePath
                else -> null
            }
        }

        private fun getFileFromContentUri(contentUri: Uri, context: Context): String? {
            var filePath: String? = null
            val filePathColumn = arrayOf(MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME)
            val contentResolver = context.contentResolver
            val cursor = contentResolver.query(contentUri, filePathColumn, null, null, null)
            if (cursor != null) {
                cursor.moveToFirst()
                filePath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]))
                cursor.close()
            }
            return filePath
        }
    }
}