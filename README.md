[![@iwgang](https://img.shields.io/badge/weibo-%40iwgang-blue.svg)](http://weibo.com/iwgang)

# LicensePlateDiscern
车牌识别，支持扫描识别和选图识别

[下载体验DEMO](https://raw.githubusercontent.com/iwgang/LicensePlateDiscern/master/app/release/app-release.apk)

### 效果图
![](https://raw.githubusercontent.com/iwgang/LicensePlateDiscern/master/xx.jpg)  

### gradle
```
implementation 'com.github.iwgang:licenseplatediscern:1.0'

build.gradle
ndk {
    abiFilters 'armeabi-v7a' // 其它看自己需求添加
}
```

### 代码
``` kotlin
// 场景1：使用 LicensePlateDiscernView（需要相机权限）

// 识别结果回调
cv_licensePlateDiscernView.setOnDiscernListener { lp ->
    tv_resultInfo.text = "识别结果：$lp"
    cv_licensePlateDiscernView.reDiscern()
}

// 闪光灯 api
cv_licensePlateDiscernView.openFlash()
cv_licensePlateDiscernView.closeFlash()


// 场景2：用作选图识别等

// 图片路径识别（需要读取SD卡权限）
val lps = LicensePlateDiscernCore.discern(context, picPath)
// 图片 bitmap 识别
val lps = LicensePlateDiscernCore.discern(bitmap)
```

### LicensePlateDiscernView 布局
``` xml
<cn.iwgang.licenseplatediscern.view.LicensePlateDiscernView
    android:id="@+id/cv_licensePlateDiscernView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:lpd_borderColor="#80FFFFFF"
    app:lpd_borderSize="0.5dp"
    app:lpd_maskColor="#8004040f" />
```

### LicensePlateDiscernView 的自定义配置
|    参数 | 类型 | 默认值 | 说明|
|--- | --- | ---| ---|
| lpd_discernRectTopMargin | dimension | 260dp | 识别框上边距 |
| lpd_discernRectLRMargin | dimension | 30dp | 识别框左右边距<br/>设置过 lpd_discernRectWidth 时此字段失效 |
| lpd_discernRectWidth | dimension | 无 | 识别框宽度 |
| lpd_discernRectHeight | dimension | 120dp | 识别框高度 |
| lpd_maskColor | color | #50000000 | 识别框外的遮罩部分颜色<br/>不想要遮罩可以设置成 @android:color/transparent|
| lpd_angleLength | dimension | 22dp | 边角线长度 |
| lpd_angleStrokeWidth | dimension | 3dp | 边角线宽度 |
| lpd_angleOffset | dimension | 无 | 边角线宽度偏移值 |
| lpd_angleColor | color | Color.GREEN | 边角线颜色 |
| lpd_borderSize | dimension | 无 | 识别框边框 size |
| lpd_borderColor | color | Color.WHITE | 识别框边框颜色 |
| lpd_isShowScanLine | boolean | true | 是否显示扫描线 |
| lpd_scanLineSize | dimension | 1dp | 扫描线 size |
| lpd_scanLineColor | color | Color.GREEN | 扫描线颜色 |
| lpd_scanLineDelayed | integer | 16 | 扫描线延迟间距延迟时间，用于调整扫描线动画速度 |


### 想自己拉代码编译修改？
* 下载 OpenCV Android 3.4.6
* 下载 NDK r14b
* 配置 CMakeLists.txt 中的 OpenCV_DIR 成自己的

### 感谢

* [HyperLPR](https://github.com/zeusees/HyperLPR)

### 已知问题

- [ ] 选图识别的识别率很低 