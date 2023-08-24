package com.example.bluetoothhiddemo

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.bluetoothhiddemo.databinding.ActivityMouseBinding
import com.example.bluetoothhiddemo.utils.MouseUtils

class MouseActivity : AppCompatActivity(),View.OnTouchListener {
    private lateinit var binding: ActivityMouseBinding
    private lateinit var mouseUtils: MouseUtils
    private val TAG = "MouseActivity"
    private  var mPointCount:kotlin.Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMouseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mouseUtils = MouseUtils()
        binding.mousePad.setOnTouchListener(this)
        binding.btnLeft.setOnTouchListener(this)
        binding.btnRight.setOnTouchListener(this)
        binding.middlePad.setOnTouchListener(this)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        mPointCount = event!!.pointerCount
        var ret = true
        when (v?.id) {
            R.id.mouse_pad -> ret = mouseUtils.mouseMove(event!!)
            R.id.btn_left -> ret = mouseUtils.mouseLeft(event!!)
            R.id.btn_right -> ret = mouseUtils.mouseRight(event!!)
            R.id.middle_pad -> ret = mouseUtils.mouseMiddle(event!!)
        }
        return ret
    }

//    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
//        //双指滚动，x为水平滚动，y为垂直滚动，消抖处理
//        var distanceX = distanceX
//        var distanceY = distanceY
//        if (mPointCount == 2) {
//            if (Math.abs(distanceX) > Math.abs(distanceY)) {
//                distanceX = if (distanceX > 0) 1 else if (distanceX < 0) -1 else 0.toFloat()
//                distanceY = 0f
//            } else {
//                distanceY = if (distanceY > 0) -1 else if (distanceY < 0) 1 else 0.toFloat()
//                distanceX = 0f
//            }
//            mBluetoothHidManager.sendWheel(distanceX.toByte(), distanceY.toByte())
//        }
//        return false
//    }
}