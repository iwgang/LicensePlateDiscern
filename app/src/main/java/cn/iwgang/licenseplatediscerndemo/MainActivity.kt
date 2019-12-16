package cn.iwgang.licenseplatediscerndemo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import cn.iwgang.licenseplatediscern.LicensePlateDiscernCore
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = Color.TRANSPARENT

        setContentView(R.layout.activity_main)

        initView()

        handlePermission()
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {
        cv_licensePlateDiscernView.setOnDiscernListener { lp ->
            tv_resultInfo.text = "识别结果：$lp"
            cv_licensePlateDiscernView.reDiscern()
        }

        tv_selAlbumBtn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_EXTERNAL_STORAGE_PERMISSION)
            } else {
                startActivityForResult(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), REQUEST_CODE_ALBUM)
            }
        }

        tv_flashBtn.setOnClickListener {
            if (tv_flashBtn.text.contains("开")) {
                cv_licensePlateDiscernView.openFlash()
                tv_flashBtn.text = "闪光灯：关"
            } else {
                cv_licensePlateDiscernView.closeFlash()
                tv_flashBtn.text = "闪光灯：开"
            }
        }
    }

    private fun handlePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CODE_CAMERA_PERMISSION)
        } else {
            cv_licensePlateDiscernView.startPreview()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (REQUEST_CODE_CAMERA_PERMISSION == requestCode) {
            permissions.forEachIndexed { index, per ->
                if (per == Manifest.permission.CAMERA && grantResults[index] == PERMISSION_GRANTED) {
                    cv_licensePlateDiscernView.startPreview()
                    return@forEachIndexed
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak", "SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CODE_ALBUM -> {
                data?.data?.let { uri ->
                    FileUtil.getFileFromUri(uri, this)?.let { picPath ->
                        object : AsyncTask<Void, Void, Array<String>?>() {
                            override fun doInBackground(vararg params: Void?): Array<String>? {
                                return LicensePlateDiscernCore.discern(this@MainActivity, picPath)
                            }

                            override fun onPostExecute(result: Array<String>?) {
                                super.onPostExecute(result)

                                if (null == result || result.isEmpty()) {
                                    tv_resultInfo.text = "识别结果：未识别到车牌"
                                } else {
                                    tv_resultInfo.text = "识别结果：${result.joinToString()}"
                                }
                            }
                        }.execute()
                    }
                }
            }
        }
    }


    companion object {
        private const val REQUEST_CODE_CAMERA_PERMISSION = 111
        private const val REQUEST_CODE_EXTERNAL_STORAGE_PERMISSION = 112
        private const val REQUEST_CODE_ALBUM = 102
    }

}
