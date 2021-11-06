package com.example.sachin.heart

import android.R.attr
import android.app.Activity
import android.os.Bundle
import android.view.SurfaceView
import android.view.SurfaceHolder
import android.widget.TextView
import android.os.PowerManager
import android.hardware.Camera
import android.os.PowerManager.WakeLock
import android.hardware.Camera.PreviewCallback
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log
import android.view.View
import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import java.lang.NullPointerException
import java.util.concurrent.atomic.AtomicBoolean
import com.jjoe64.graphview.series.LineGraphSeries

import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint

import android.R.attr.data
import android.graphics.*
import android.widget.ImageView

import java.io.ByteArrayOutputStream
import java.lang.Exception
import android.graphics.BitmapFactory

import android.graphics.ImageFormat

import android.graphics.YuvImage





/**
 * This class extends Activity to handle a picture preview, process the preview
 * for a red values and determine a heart beat.
 *
 * @author Justin Wetherell <phishman3579></phishman3579>@gmail.com>
 */
class HeartRateMonitor : Activity() {
    enum class TYPE {
        GREEN, RED
    }

    //Данные об ударах сердца
    object Beats {
        var currentCount = 0

        var currentStartTime: Long = 0
        var startTime: Long = 0
        var endTime: Long = 0
        var currentBeatTime: Long = 0
        var lastBeatTime: Long = 0

        var times: MutableList<DataPoint> = ArrayList()

        fun addBeat() {
            currentBeatTime = System.currentTimeMillis()
            times.add(DataPoint((currentBeatTime - 1 - startTime.toDouble())/1000, 0.0))
            times.add(DataPoint((currentBeatTime - startTime.toDouble())/1000, 1.0))
            times.add(DataPoint((currentBeatTime + 1 - startTime.toDouble())/1000, 0.0))
        }

        fun startMonitor() {
            startTime = System.currentTimeMillis()
        }
    }

    //служебные переменные, котррые надо переработать
    var averageIndex = 0
    val averageArraySize = 4
    val averageArray = IntArray(averageArraySize)
    var beatsIndex = 0
    val beatsArraySize = 3
    val beatsArray = IntArray(beatsArraySize)
    var current = TYPE.GREEN
        private set

    //переменные для интерфейса
    val TAG = "HeartRateMonitor"
    val processing = AtomicBoolean(false)
    var IPreviewHolder: SurfaceHolder? = null
    var camera: Camera? = null

    var IBpmText: TextView? = null
    var IRedLevelText: TextView? = null

    var IGraph: GraphView? = null
    var IBitmapImageFromCamera: ImageView? = null
    var wakeLock: WakeLock? = null

    val previewCallback = PreviewCallback { data, cam ->



        if (false) {
            var newType = TYPE.RED
            if (newType != current) {

                Beats.currentCount++
                Log.d(TAG, "BEAT!! beats=" + Beats.currentCount)

                Beats.addBeat()
                Beats.lastBeatTime = Beats.currentBeatTime
            }


            val endTime = System.currentTimeMillis()
            val totalTimeInSecs = (endTime - Beats.currentStartTime) / 1000.0
            if (totalTimeInSecs >= 10) {
                val bps = Beats.currentCount / totalTimeInSecs
                val dpm = (bps * 60.0).toInt()

                //очистить граф
                IGraph!!.removeAllSeries()
                //записать новые данные в граф
                val series: LineGraphSeries<DataPoint> = LineGraphSeries(Beats.times.toTypedArray())
                IGraph!!.addSeries(series)

                val beatsAvg = (Beats.currentCount / totalTimeInSecs)
                IBpmText!!.text = "$beatsAvg bpm"
                Beats.currentStartTime = System.currentTimeMillis()
                Beats.currentCount = 0
            }
            processing.set(false)
        }
    }
    val surfaceCallback: SurfaceHolder.Callback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            try {
                camera!!.setPreviewDisplay(IPreviewHolder)
                camera!!.setPreviewCallback(previewCallback)
            } catch (t: Throwable) {
                Log.e("Preview-surfaceCallback", "Exception in setPreviewDisplay()", t)
            }
        }

        @RequiresApi(Build.VERSION_CODES.ECLAIR)
        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            val parameters = camera!!.parameters
            parameters.flashMode = Camera.Parameters.FLASH_MODE_TORCH
            val size = getSmallestPreviewSize(width, height, parameters)
            if (size != null) {
                parameters.setPreviewSize(size.width, size.height)
                Log.d(TAG, "Using width=" + size.width + " height=" + size.height)
            }
            camera!!.parameters = parameters
            camera!!.startPreview()
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            // Ignore
        }
    }

    fun getSmallestPreviewSize(width: Int, height: Int, parameters: Camera.Parameters): Camera.Size? {
        var result: Camera.Size? = null
        for (size in parameters.supportedPreviewSizes) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size
                } else {
                    val resultArea = result.width * result.height
                    val newArea = size.width * size.height
                    if (newArea < resultArea) result = size
                }
            }
        }
        return result
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        //инициализация переменных
        val preview = findViewById<View>(R.id.preview) as SurfaceView
        IPreviewHolder = preview.holder
        IPreviewHolder!!.addCallback(surfaceCallback)
        IBpmText = findViewById(R.id.text)
        IRedLevelText = findViewById(R.id.red_level_text)
        IGraph = findViewById<View>(R.id.graph) as GraphView
        IBitmapImageFromCamera = findViewById(R.id.bitmapImageFromCamera)

        //wakelock
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(FLAG_KEEP_SCREEN_ON, "HearRate:mywakelock")

        //стартуем монитор
        Beats.startMonitor()
    }

    public override fun onResume() {
        super.onResume()
        wakeLock!!.acquire(10 * 60 * 1000L /*10 minutes*/)
        camera = Camera.open()
        Beats.currentStartTime = System.currentTimeMillis()
    }

    public override fun onPause() {
        super.onPause()
        wakeLock!!.release()
        camera!!.setPreviewCallback(null)
        camera!!.stopPreview()
        camera!!.release()
        camera = null
    }

}