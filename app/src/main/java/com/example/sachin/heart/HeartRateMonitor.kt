package com.example.sachin.heart

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
    inner class Beats {
        var count = 0

        //служебные переменные, котррые надо переработать
        var averageIndex = 0
        val averageArraySize = 4
        val averageArray = IntArray(averageArraySize)
        var beatsIndex = 0
        val beatsArraySize = 3
        val beatsArray = IntArray(beatsArraySize)

        var beats = 0.0
        var startTime: Long = 0
        var beatsTime: MutableList<DataPoint> = ArrayList()
        var currentBeatTime: Long = 0
        var lastBeatTime: Long = 0
    }

    val TAG = "HeartRateMonitor"
    private val processing = AtomicBoolean(false)
    private var previewHolder: SurfaceHolder? = null
    private var camera: Camera? = null

    private var text: TextView? = null
    private var imgavgtxt: TextView? = null
    private var rollavgtxt: TextView? = null

    private var graph: GraphView? = null
    private var wakeLock: WakeLock? = null

    private val previewCallback = PreviewCallback { data, cam ->

        if (data == null) throw NullPointerException()
        val size = cam.parameters.previewSize ?: throw NullPointerException()
        if (!processing.compareAndSet(false, true)) return@PreviewCallback
        val width = size.width
        val height = size.height
        val imgAvg = ImageProcessing.decodeYUV420SPtoRedAvg(data.clone(), height, width)
        // Log.i(TAG, "imgAvg="+imgAvg);
        if (imgAvg == 0 || imgAvg == 255) {
            processing.set(false)
            return@PreviewCallback
        }
        var averageArrayAvg = 0
        var averageArrayCnt = 0
        for (i in averageArray.indices) {
            if (averageArray[i] > 0) {
                averageArrayAvg += averageArray[i]
                averageArrayCnt++
            }
        }
        val rollingAverage = if (averageArrayCnt > 0) averageArrayAvg / averageArrayCnt else 0
        var newType = current
        imgavgtxt!!.text = "image average:" + Integer.toString(imgAvg)
        rollavgtxt!!.text = "rolling average:" + Integer.toString(rollingAverage)
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
        if (averageIndex == averageArraySize) averageIndex = 0
        averageArray[averageIndex] = imgAvg
        averageIndex++

        // Transitioned from one state to another to the same
        if (newType != current) {
            current = newType
        }
        val endTime = System.currentTimeMillis()
        val totalTimeInSecs = (endTime - startTime) / 1000.0
        if (totalTimeInSecs >= 10) {
            val bps = beats / totalTimeInSecs
            val dpm = (bps * 60.0).toInt()

            //очистить граф
            graph!!.removeAllSeries()

            //записать новые данные в граф
            val series: LineGraphSeries<DataPoint> = LineGraphSeries(beatsTime.toTypedArray())
            graph!!.addSeries(series)

            //распечатка времени ударов в лог
            for (beatTime in beatsTime) {
                Log.d(TAG, beatTime.toString())
            }
            beatsTime.clear()

            //если удары выходят за рамки разумного (<30 или >180)
            if (dpm < 30 || dpm > 180) {
                startTime = System.currentTimeMillis()
                beats = 0.0
                processing.set(false)
                return@PreviewCallback
            }

            //среднее по всем измерениям ударов
            // Log.d(TAG,
            // "totalTimeInSecs="+totalTimeInSecs+" beats="+beats);
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
            text!!.text = "$beatsAvg bpm"
            startTime = System.currentTimeMillis()
            beats = 0.0
        }
        processing.set(false)
    }
    private val surfaceCallback: SurfaceHolder.Callback = object : SurfaceHolder.Callback {
        /**
         * {@inheritDoc}
         */
        override fun surfaceCreated(holder: SurfaceHolder) {
            try {
                camera!!.setPreviewDisplay(previewHolder)
                camera!!.setPreviewCallback(previewCallback)
            } catch (t: Throwable) {
                Log.e("Preview-surfaceCallback", "Exception in setPreviewDisplay()", t)
            }
        }

        /**
         * {@inheritDoc}
         */
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

        /**
         * {@inheritDoc}
         */
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

    companion object {

    }
}