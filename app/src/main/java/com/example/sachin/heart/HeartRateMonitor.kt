package com.example.sachin.heart

import android.R.attr
import android.app.Activity
import android.os.Bundle
import com.example.sachin.heart.R
import android.view.SurfaceView
import com.example.sachin.heart.HeartRateMonitor
import android.view.SurfaceHolder
import android.widget.TextView
import android.os.PowerManager
import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.drawable.BitmapDrawable
import android.hardware.Camera
import android.os.PowerManager.WakeLock
import com.example.sachin.heart.HeartRateMonitor.TYPE
import android.hardware.Camera.PreviewCallback
import android.util.Log
import android.util.Log.DEBUG
import android.view.View
import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import com.example.sachin.heart.BuildConfig.DEBUG
import com.example.sachin.heart.ImageProcessing
import java.lang.NullPointerException
import java.util.concurrent.atomic.AtomicBoolean
import com.jjoe64.graphview.series.LineGraphSeries

import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint

import android.R.attr.data
import android.graphics.*
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.Image

import android.util.Size
import org.jetbrains.annotations.Contract
import java.io.ByteArrayOutputStream
import java.lang.Math.abs


/**
 * This class extends Activity to handle a picture preview, process the preview
 * for a red values and determine a heart beat.
 */
class HeartRateMonitor : Activity() {
    enum class TYPE {
        GREEN, RED
    }


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        //инициализация переменных
        val preview = findViewById<View>(R.id.preview) as SurfaceView
        previewHolder = preview.holder
        previewHolder!!.addCallback(surfaceCallback)
        text = findViewById(R.id.text)
        imgavgtxt = findViewById(R.id.img_avg_text)
        rollavgtxt = findViewById(R.id.rollavg_text)
        graph = findViewById<View>(R.id.graph) as GraphView

        //wakelock
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(FLAG_KEEP_SCREEN_ON, "HearRate:mywakelock")
    }

    public override fun onResume() {
        super.onResume()
        wakeLock!!.acquire(10 * 60 * 1000L /*10 minutes*/)
        camera = Camera.open()
        startTime = System.currentTimeMillis()
    }

    public override fun onPause() {
        super.onPause()
        wakeLock!!.release()
        camera!!.setPreviewCallback(null)
        camera!!.stopPreview()
        camera!!.release()
        camera = null
    }

        private val TAG = "HeartRateMonitor"
        private val processing = AtomicBoolean(false)
        private var previewHolder: SurfaceHolder? = null
        private var camera: Camera? = null

        private var text: TextView? = null

        private var imgavgtxt: TextView? = null
        private var rollavgtxt: TextView? = null

        private var graph: GraphView? = null
        private var wakeLock: WakeLock? = null
        private var averageIndex = 0
        private val averageArraySize = 4
        private val averageArray = IntArray(averageArraySize)
        private var averageIndex1 = 0
        private val averageArraySize1 = 4
        private val averageArray1 = IntArray(averageArraySize1)
        var current = TYPE.GREEN
            private set
        var current1 = TYPE.GREEN
            private set
        private var beatsIndex = 0
        private val beatsArraySize = 3
        private val beatsArray = IntArray(beatsArraySize)
    private var beatsIndex1 = 0
    private val beatsArraySize1 = 3
    private val beatsArray1 = IntArray(beatsArraySize1)
        private var beats = 0.0
        private var beats1 = 0.0
        private var startTime: Long = 0
        private var startTime1: Long = 0
        private var beatsTime: MutableList<DataPoint> = ArrayList()
    private var beatsTime1: MutableList<DataPoint> = ArrayList()
        private var diffBetweenMethods: MutableList<DataPoint> = ArrayList()
        private var currentBeatTime: Long = 0
    private var currentBeatTime1: Long = 0
        private var lastBeatTime: Long = 0
    private var lastBeatTime1: Long = 0
        private val previewCallback = PreviewCallback { data, cam ->


            if (data == null) throw NullPointerException()
            val size = cam.parameters.previewSize ?: throw NullPointerException()
            if (!processing.compareAndSet(false, true)) return@PreviewCallback

            val width = size.width
            val height = size.height
            val imgAvg = ImageProcessing.decodeYUV420SPtoRedAvg(data.clone(), height, width)
            val imgAvg1 = ImageProcessing.decodeRedFromRGBBitmap(data.clone(), height, width)

            diffBetweenMethods.add(DataPoint((System.currentTimeMillis()/1000  - startTime/1000).toDouble(), abs(imgAvg - imgAvg1).toDouble()))

            if (imgAvg == 0 || imgAvg == 255) {
                processing.set(false)
                return@PreviewCallback
            }

            var averageArrayAvg = 0
            var averageArrayCnt = 0
            var averageArrayAvg1 = 0
            var averageArrayCnt1 = 0
            for (i in averageArray.indices) {
                if (averageArray[i] > 0) {
                    averageArrayAvg += averageArray[i]
                    averageArrayCnt++
                }
            }
            for (i in averageArray1.indices) {
                if (averageArray1[i] > 0) {
                    averageArrayAvg1 += averageArray1[i]
                    averageArrayCnt1++
                }
            }
            val rollingAverage = if (averageArrayCnt > 0) averageArrayAvg / averageArrayCnt else 0
            val rollingAverage1 = if (averageArrayCnt1 > 0) averageArrayAvg1 / averageArrayCnt1 else 0

            var newType = current
            var newType1 = current1
            imgavgtxt!!.text = "image average:" + Integer.toString(imgAvg) + " / " + Integer.toString(imgAvg1)
            rollavgtxt!!.text = "rolling average:" + Integer.toString(rollingAverage) + " / " + Integer.toString(rollingAverage1)

            if (imgAvg < rollingAverage) {
                newType = TYPE.RED
                if (newType != current) {

                    beats++
                    Log.d(TAG, "BEAT!! beats=" + beats)

                    currentBeatTime = System.currentTimeMillis()

                    beatsTime.add(DataPoint((currentBeatTime - startTime.toDouble())/1000, 1.0))
                    beatsTime.add(DataPoint((currentBeatTime + 1 - startTime.toDouble())/1000, 0.0))
                    lastBeatTime = currentBeatTime
                }
            } else if (imgAvg > rollingAverage) {
                newType = TYPE.GREEN
            }
            if (imgAvg1 < rollingAverage1) {
                newType1 = TYPE.RED
                if (newType1 != current) {

                    beats1++
                    Log.d(TAG, "BEAT 1 !! beats=" + beats1)

                    currentBeatTime1 = System.currentTimeMillis()

                    beatsTime1.add(DataPoint((currentBeatTime1 - startTime.toDouble())/1000, 4.0))
                    beatsTime1.add(DataPoint((currentBeatTime1 + 1 - startTime.toDouble())/1000, 3.0))
                    lastBeatTime1 = currentBeatTime1
                }
            } else if (imgAvg1 > rollingAverage1) {
                newType1 = TYPE.GREEN
            }


            if (averageIndex == averageArraySize) averageIndex = 0
            averageArray[averageIndex] = imgAvg
            averageIndex++

            if (averageIndex1 == averageArraySize1) averageIndex1 = 0
            averageArray1[averageIndex1] = imgAvg1
            averageIndex1++

            // Transitioned from one state to another to the same
            if (newType != current) {
                current = newType
            }
            if (newType1 != current1) {
                current1 = newType1
            }

            val endTime = System.currentTimeMillis()
            val totalTimeInSecs = (endTime - startTime) / 1000.0
            if (totalTimeInSecs >= 10) {
                val bps = beats / totalTimeInSecs
                val bps1 = beats1 / totalTimeInSecs
                val dpm = (bps * 60.0).toInt()
                val dpm1 = (bps1 * 60.0).toInt()

                //очистить граф
                graph!!.removeAllSeries()

                //записать новые данные в граф
                val series: LineGraphSeries<DataPoint> = LineGraphSeries(beatsTime.toTypedArray())
                val series1: LineGraphSeries<DataPoint> = LineGraphSeries(beatsTime1.toTypedArray())
                val diffBetweenMethodsSeries: LineGraphSeries<DataPoint> = LineGraphSeries(diffBetweenMethods.toTypedArray())


                graph!!.addSeries(series)
                graph!!.addSeries(series1)

                diffBetweenMethods.clear()
                beatsTime.clear()
                beatsTime1.clear()

                //если удары в минуту выходят за рамки разумного (<30 или >180)
                if (dpm < 30 || dpm > 180) {
                    startTime = System.currentTimeMillis()
                    beats = 0.0
                    processing.set(false)
                    return@PreviewCallback
                }

                //среднее по всем измерениям ударов
                if (beatsIndex == beatsArraySize) beatsIndex = 0
                beatsArray[beatsIndex] = dpm
                beatsIndex++
                var beatsArrayAvg = 0
                var beatsArrayCnt = 0
                for (i in beatsArray.indices) {
                    if (beatsArray[i] > 0) {
                        beatsArrayAvg += beatsArray[i]
                        beatsArrayCnt++
                    }
                }
                val beatsAvg = beatsArrayAvg / beatsArrayCnt

                //среднее по всем измерениям ударов
                if (beatsIndex1 == beatsArraySize1) beatsIndex1 = 0
                beatsArray1[beatsIndex1] = dpm1
                beatsIndex1++
                var beatsArrayAvg1 = 0
                var beatsArrayCnt1 = 0
                for (i in beatsArray1.indices) {
                    if (beatsArray1[i] > 0) {
                        beatsArrayAvg1 += beatsArray1[i]
                        beatsArrayCnt1++
                    }
                }
                val beatsAvg1 = beatsArrayAvg1 / beatsArrayCnt1


                text!!.text = "$beatsAvg bpm / $beatsAvg1 bpm"
                startTime = System.currentTimeMillis()
                beats = 0.0
            }
            processing.set(false)
        }
        private val surfaceCallback: SurfaceHolder.Callback = object : SurfaceHolder.Callback {

            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    camera!!.setPreviewDisplay(previewHolder)
                    camera!!.setPreviewCallback(previewCallback)
                } catch (t: Throwable) {
                    Log.e("Preview-surfaceCallback", "Exception in setPreviewDisplay()", t)
                }
            }

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

        private fun getSmallestPreviewSize(width: Int, height: Int, parameters: Camera.Parameters): Camera.Size? {
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
}