package cn.iwgang.licenseplatediscerndemo

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = Color.TRANSPARENT

        setContentView(R.layout.activity_main)

        cv_licensePlateDiscernView.setOnDiscernListener { lp ->
            tv_resultInfo.text = "识别结果：$lp"
            cv_licensePlateDiscernView.reDiscern()
        }

        handlePermission()
    }

    private fun handlePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
        } else {
            cv_licensePlateDiscernView.startPreview()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (101 == requestCode) {
            permissions.forEachIndexed { index, per ->
                if (per == Manifest.permission.CAMERA && grantResults[index] == PERMISSION_GRANTED) {
                    cv_licensePlateDiscernView.startPreview()
                    return@forEachIndexed
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        cv_licensePlateDiscernView.onResume()
    }

}
