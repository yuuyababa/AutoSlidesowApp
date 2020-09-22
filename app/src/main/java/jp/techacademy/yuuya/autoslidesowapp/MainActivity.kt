package jp.techacademy.yuuya.autoslidesowapp

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import  kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100          //許可ダイアログ用
    private var slideTimer: Timer? = null               //タイマー用
    private var potisionNo: Int = 0                     //カーソル内ポジション保存用
    private var slideSwich = 0                          //スライドショーON/OFF
    private var slideHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //外部ストレージへの許可の確認
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                //許可あり
                getContentsInfo(0, potisionNo)

            } else {
                //許可なし。ダイアログを表示する。
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSIONS_REQUEST_CODE
                )
            }

        } else {
            getContentsInfo(0, potisionNo)
        }
        //次へボタン入力時動作
        nextView.setOnClickListener {
            if (slideSwich === 0) potisionNo = getContentsInfo(1, potisionNo)
        }
        //戻るボタン入力時動作
        previousView.setOnClickListener {
            if (slideSwich === 0) potisionNo = getContentsInfo(2, potisionNo)

        }

        //スライドショー機能切り替え
        playStop.setOnClickListener {

            if (slideSwich === 0) {
                slideViewer(slideSwich)
                slideSwich = 1
            } else {
                slideViewer(slideSwich)
                slideSwich = 0
            }
        }

    }

    //スライドショー用タイマー部分
    private fun slideViewer(sliderSwitch: Int) {

        if (sliderSwitch === 0) {
            slideTimer = Timer()
            slideTimer!!.schedule(
                object : TimerTask() {
                    override fun run() {
                        slideHandler.post {
                            potisionNo = getContentsInfo(1, potisionNo)
                        }
                    }
                },
                2000,
                2000
            )
        } else {
            slideTimer!!.cancel()
        }


    }

    //画像取得・および表示部分
    private fun getContentsInfo(next: Int, positionNo: Int): Int {
        //画像の情報を取得する。
        val resolver = contentResolver
        var cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            null
        )


        if (cursor!!.moveToFirst()) {


            if (next === 1) {

                cursor.moveToPosition(positionNo)

                if (cursor.isLast) {
                    cursor.moveToFirst()

                } else {
                    cursor.moveToNext()
                }

            } else if (next === 2) {

                cursor.moveToPosition(positionNo)

                if (cursor.isFirst) {
                    cursor.moveToLast()
                } else {
                    cursor.moveToPrevious()
                }
            }

            val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            val id = cursor.getLong(fieldIndex)
            val imageUri =
                ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )


            var cursorPotision = cursor.getPosition()

            slideViewer.setImageURI(imageUri)

            return cursorPotision

        } else {
            cursor.close()
            return -1
        }
    }
}


