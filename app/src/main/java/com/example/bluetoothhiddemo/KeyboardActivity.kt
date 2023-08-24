package com.example.bluetoothhiddemo

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.bluetoothhiddemo.databinding.ActivityKeyboardBinding
import com.example.bluetoothhiddemo.tools.HidConsts

class KeyboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityKeyboardBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        binding = ActivityKeyboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        registerKeyButton()
        HidConsts.cleanKbd()
    }

    private fun registerKeyButton() {
        for (i in 0 until binding.keysButtons.childCount) {
            val view: View = binding.keysButtons.getChildAt(i)
            view.setOnTouchListener(onTouchListener)
        }
    }

    var onTouchListener = OnTouchListener { v, event ->
        if (event.action == MotionEvent.ACTION_DOWN) {
            HidConsts.kbdKeyDown(v.tag.toString())
        } else if (event.action == MotionEvent.ACTION_UP) {
            HidConsts.kbdKeyUp(v.tag.toString())
        }
        false
    }
}