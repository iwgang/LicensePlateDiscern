package cn.iwgang.licenseplatediscerndemo

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
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
    }

    override fun onResume() {
        super.onResume()
        cv_licensePlateDiscernView.onResume()
    }

}
