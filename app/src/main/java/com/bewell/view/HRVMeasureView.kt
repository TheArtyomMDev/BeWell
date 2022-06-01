package com.bewell.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.hardware.Camera
import android.hardware.Camera.PreviewCallback
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import com.bewell.R
import com.bewell.databinding.MainBinding
import com.bewell.presenter.HRVMeasurePresenter
import com.bewell.presenter.StartMeasurePresenter
import com.bewell.utils.ImageProcessing
import com.bewell.view.ResultView
import org.nield.kotlinstatistics.standardDeviation
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.system.exitProcess



class HRVMeasureView : Activity() {

    private lateinit var binding: MainBinding
    private lateinit var presenter: HRVMeasurePresenter

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        presenter = HRVMeasurePresenter()
        presenter.attachView(this)

        presenter.setup(this, intent, binding.preview.holder, binding.bpm)
    }

    public override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    public override fun onPause() {
        super.onPause()
        presenter.onPause()
    }
}