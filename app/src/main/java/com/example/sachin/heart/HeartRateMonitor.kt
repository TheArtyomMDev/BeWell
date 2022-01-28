package com.example.sachin.heart

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.hardware.Camera
import android.hardware.Camera.PreviewCallback
import android.os.Bundle
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import android.widget.TextView
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import org.nield.kotlinstatistics.standardDeviation
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToInt
import android.graphics.RectF
import android.view.animation.AnimationUtils
import java.util.Collections.rotate


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

    class CustomView @JvmOverloads constructor(context: Context,
                                               attrs: AttributeSet? = null, defStyleAttr: Int = 0)
        : View(context, attrs, defStyleAttr) {

        var rotateAngle = 0F
        var deltaAngle = 0.01F
        var resizedWidth = 950
        var resizedHeight = 750

        private var loadBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.level_back)
        private var resizedLoadBitmap: Bitmap = Bitmap.createScaledBitmap(loadBitmap, resizedWidth, resizedHeight, false)
        private var rotator = Matrix()
        private val paint = Paint()
        private var translated = false

        // Called when the view should render its content.
        override fun onDraw(canvas: Canvas?) {
            super.onDraw(canvas)

            val width = measuredWidth.toFloat()
            val height = measuredHeight.toFloat()
            val radius = (0.5*width/2).toFloat()

            if(!translated) {
                rotator.postTranslate(40F, 55F)
                translated = true
            }
            rotator.postRotate(1F, resizedWidth/2F, resizedHeight/2F)
            // rotate around x,y
            // NOTE: coords in bitmap-space!

            paint.color = Color.WHITE
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 200F
            canvas!!.drawCircle(width/2, height/2, radius, paint);

            //canvas.drawBitmap(mBitmap, width/2-425, height/2-400, paint);
            canvas.drawBitmap(resizedLoadBitmap, rotator, paint);

            rotateAngle += deltaAngle
            if(rotateAngle > 5.0F || rotateAngle < 0F) deltaAngle = -deltaAngle
            invalidate()
        }
    }


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        //инициализация переменных
        val preview = findViewById<View>(R.id.preview) as SurfaceView
        previewHolder = preview.holder
        previewHolder!!.addCallback(surfaceCallback)
        text = findViewById(R.id.text)
        imgavgtxt = findViewById(R.id.red_level_text)
        graph = findViewById<View>(R.id.graph) as GraphView
        var animationRotateCenter = AnimationUtils.loadAnimation(this, R.anim.rotation);


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
        private const val TAG = "HeartRateMonitor"
        private val processing = AtomicBoolean(false)
        private var previewHolder: SurfaceHolder? = null
        private var camera: Camera? = null

        private var text: TextView? = null
        private var imgavgtxt: TextView? = null

        private var graph: GraphView? = null
        private var wakeLock: WakeLock? = null
        private var averageIndex = 0
        private const val averageArraySize = 4
        private val averageArray = IntArray(averageArraySize)
        var current = TYPE.GREEN
            private set
        private var beatsIndex = 0
        private const val beatsArraySize = 3
        private val beatsArray = IntArray(beatsArraySize)
        private var beats = 0.0
        private var startTime: Long = 0
        private var generalStartTime: Long = 0
        private var beatsTime: MutableList<DataPoint> = ArrayList()
        private var generalBeatsTime: MutableList<Double> = ArrayList()
        private var intervalsBeatsTime: MutableList<Double> = ArrayList()
        private var currentBeatTime: Long = 0
        private var lastBeatTime: Long = 0
        private fun <T> modeOf(a: Array<T>): Pair<T, Int> {
            val sortedByFreq = a.groupBy { it }.entries.sortedByDescending { it.value.size }
            val maxFreq = sortedByFreq.first().value.size
            val modes = sortedByFreq.takeWhile { it.value.size == maxFreq }
            return Pair(modes.first().key, maxFreq)
        }
    }
        val previewCallback = PreviewCallback { data, cam ->

            if (data == null) throw NullPointerException()
            val size = cam.parameters.previewSize ?: throw NullPointerException()
            if (!processing.compareAndSet(false, true)) return@PreviewCallback
            val width = size.width
            val height = size.height
            val imgAvg = ImageProcessing.decodeYUV420SPtoRedAvg(data.clone(), height, width)

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

            if(generalStartTime == 0L){
                generalStartTime = System.currentTimeMillis()
            }

            if (imgAvg < rollingAverage) {
                newType = TYPE.RED
                if (newType != current) {

                    beats++
                    Log.d(TAG, "BEAT!! beats=$beats")

                    currentBeatTime = System.currentTimeMillis()

                    beatsTime.add(DataPoint((currentBeatTime - 100 - startTime.toDouble())/1000, 0.0))
                    beatsTime.add(DataPoint((currentBeatTime - startTime.toDouble())/1000, 1.0))
                    beatsTime.add(DataPoint((currentBeatTime + 100 - startTime.toDouble())/1000, 0.0))

                    generalBeatsTime.add((currentBeatTime - generalStartTime.toDouble())/1000)

                    lastBeatTime = currentBeatTime
                }
            } else if (imgAvg > rollingAverage) {
                newType = TYPE.GREEN
            }

            if (averageIndex == averageArraySize) averageIndex = 0
            averageArray[averageIndex] = imgAvg
            averageIndex++

            // Transitioned from one state to another to the same
            if (newType != current) current = newType

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
            if ((endTime - generalStartTime)/1000.0 > 20) {
                for(i in 1 until generalBeatsTime.size) {
                    intervalsBeatsTime.add(((generalBeatsTime[i] - generalBeatsTime[i - 1]) * 100).roundToInt()/100.0)
                }

                //val SD = calculateSD(intervalsBeatsTime.toDoubleArray())
                val SD = intervalsBeatsTime.standardDeviation()
                val (Mo, freq) = modeOf(intervalsBeatsTime.toTypedArray())
                val AMo = (freq.toDouble() / intervalsBeatsTime.size)*100

                val BI = AMo/(2*Mo*SD)

                Log.d(TAG, "___$SD $Mo $AMo $BI")


                //распечатка времени всех ударов в лог
                for (beatTime in generalBeatsTime) {
                    Log.d(TAG, beatTime.toString())
                }

                //распечатка времени всех промежутков в лог
                for (beatTime in intervalsBeatsTime) {
                    Log.d(TAG, beatTime.toString())
                }

                val intent = Intent(this, Result::class.java)
                intent.putExtra("BI", BI)

                startActivity(intent)
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
    }
