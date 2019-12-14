package cn.iwgang.licenseplatediscern

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * Assets 资源处理类
 *
 * Created by iWgang on 19/12/14.
 * https://github.com/iwgang/LicensePlateDiscern
 */
class AssetsUtil {
    companion object {

        /**
         * 复制Assets
         * @param context           Context
         * @param desAssetsDirName  需要复制的目标Assets名称，为空则从根目录复制
         * @param savePath          目录保存路径
         * @return 复制结果
         */
        fun copyAssets(context: Context, desAssetsDirName: String?, savePath: String): Boolean {
            val files: Array<String>?
            try {
                files = context.assets.list(desAssetsDirName)
            } catch (e1: IOException) {
                return false
            }


            if (null == files || files.isEmpty()) {
                return false
            }

            val mWorkingPath = File(savePath)
            if (!mWorkingPath.exists()) {
                mWorkingPath.mkdirs()
            }

            for (file in files) {
                try {
                    if (!file.contains(".")) {
                        if (desAssetsDirName!!.isEmpty()) {
                            copyAssets(context, file, "$savePath$file/")
                        } else {
                            copyAssets(
                                    context, "$desAssetsDirName/$file", "$savePath/$file/"
                            )
                        }
                        continue
                    }
                    val outFile = File(mWorkingPath, file)
                    if (outFile.exists())
                        continue
                    val inputStream: InputStream = if (desAssetsDirName!!.isNotEmpty()) {
                        context.assets.open("$desAssetsDirName/$file")
                    } else {
                        context.assets.open(file)
                    }

                    val out = FileOutputStream(outFile)
                    val buf = ByteArray(1024)
                    var len = inputStream.read(buf)
                    while (len > 0) {
                        out.write(buf, 0, len)
                        len = inputStream.read(buf)
                    }
                    inputStream.close()
                    out.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }

            return true
        }

    }
}