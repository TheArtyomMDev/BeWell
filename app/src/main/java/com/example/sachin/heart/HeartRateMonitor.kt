package com.example.sachin.heart

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.Camera
import android.hardware.Camera.PreviewCallback
import android.os.Bundle
import android.os.CountDownTimer
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import org.nield.kotlinstatistics.standardDeviation
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.system.exitProcess


class HeartRateMonitor : Activity() {
    enum class TYPE {
        GREEN, RED
    }
    private val TAG = "HeartRateMonitor"
    private val processing = AtomicBoolean(false)
    private var previewHolder: SurfaceHolder? = null
    private var camera: Camera? = null
    private var text: TextView? = null
    private var imgavgtxt: TextView? = null
    private var timeText: TextView? = null
    private var preview: SurfaceView? = null
    private var cancelButton: Button? = null
    private var graph: GraphView? = null
    private var wakeLock: WakeLock? = null

    var current = TYPE.GREEN
        private set

    private var measureTimeInSec = 10
    private var averageIndex = 0
    private val averageArraySize = 4
    private var beatsIndex = 0
    private val beatsArraySize = 3
    private val beatsArray = IntArray(beatsArraySize)
    private var beats = 0.0
    private var startTime: Long = 0
    private var generalStartTime: Long = 0
    private var currentBeatTime: Long = 0
    private var lastBeatTime: Long = 0

    private val averageArray = IntArray(averageArraySize)
    private var beatsTime: MutableList<DataPoint> = ArrayList()
    private var generalBeatsTime: MutableList<Double> = ArrayList()
    private var intervalsBeatsTime: MutableList<Double> = ArrayList()

    private fun <T> modeOf(a: Array<T>): Pair<T, Int> {
        val sortedByFreq = a.groupBy { it }.entries.sortedByDescending { it.value.size }
        val maxFreq = sortedByFreq.first().value.size
        val modes = sortedByFreq.takeWhile { it.value.size == maxFreq }
        return Pair(modes.first().key, maxFreq)
    }

    private val timer = object: CountDownTimer(measureTimeInSec*1000L, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            if((millisUntilFinished/1000).toInt()%60 < 10)
                timeText!!.text = "${(millisUntilFinished/60000).toInt()}:0${(millisUntilFinished/1000)%60}"
            else timeText!!.text = "${(millisUntilFinished/60000).toInt()}:${(millisUntilFinished/1000)%60}"
        }

        override fun onFinish() {

        }
    }

    class CustomView @JvmOverloads constructor(context: Context,
                                               attrs: AttributeSet? = null, defStyleAttr: Int = 0)
        : View(context, attrs, defStyleAttr) {

        var rotateAngle = 0F
        var deltaAngle = 0.3F
        var resizedWidth = 900
        var resizedHeight = 900

        private var loadBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.load)
        private var resizedLoadBitmap: Bitmap = Bitmap.createScaledBitmap(loadBitmap, resizedWidth, resizedHeight, false)
        private var loadBitmapBackground: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.load_background)
        private var resizedLoadBitmapBackground: Bitmap = Bitmap.createScaledBitmap(loadBitmapBackground, resizedWidth, resizedHeight, false)
        private var rotator = Matrix()
        private val paint = Paint()
        private var translated = false

        var deltaX1 = 0.0F
        var deltaY1 = -100.0F
        var reverseX1 = false
        var reverseY1 = false

        var deltaX2 = 0.0F
        var deltaY2 = 100.0F
        var reverseX2 = true
        var reverseY2 = true

        var deltaX3 = -100.0F
        var deltaY3 = 0.0F
        var reverseX3 = false
        var reverseY3 = true

        // Called when the view should render its content.
        override fun onDraw(canvas: Canvas?) {
            super.onDraw(canvas)

            val width = measuredWidth.toFloat()
            val height = measuredHeight.toFloat()
            val radius = (width/100).toFloat()

            /*
            if(!translated) {
                rotator.postTranslate(63F, 63F)
                //rotator.postRotate(90F, resizedWidth/2F + 60.9F, resizedHeight/2F + 60.9F)
                translated = true
            }
            */

            /*rotator.postRotate(rotateAngle, resizedWidth/2F + 60.9F, resizedHeight/2F + 60.9F)*/
            // rotate around x,y
            // NOTE: coords in bitmap-space!

            val value = TypedValue()
            context.theme.resolveAttribute(R.attr.colorPrimary, value, true)

            paint.color = value.data
            paint.style = Paint.Style.FILL_AND_STROKE
            //paint.strokeWidth = 20F
            //canvas!!.drawPoint(width/2, height/2, paint)
            canvas!!.drawCircle(width/2+deltaX1, height/2+deltaY1, radius, paint)
            canvas!!.drawCircle(width/2-300+deltaX2, height/2+deltaY2, radius, paint)
            canvas!!.drawCircle(width/2+300+deltaX3, height/2+deltaY3, radius, paint)



            /*
            //canvas.drawBitmap(mBitmap, width/2-425, height/2-400, paint);
            canvas.drawBitmap(resizedLoadBitmapBackground, width/2 - resizedWidth/2F, height/2 - resizedHeight/2F, paint)
            canvas.drawBitmap(resizedLoadBitmap, rotator, paint);

            rotateAngle += deltaAngle
            if(rotateAngle > 15.0F || rotateAngle < 0F) deltaAngle = -deltaAngle
            */

            invalidate()

            if(deltaX1 > 100F) reverseX1 = true
            else if(deltaX1 < -100F) reverseX1 = false
            if(deltaY1 > 100F) reverseY1 = true
            else if(deltaY1 < -100F) reverseY1 = false
            if(!reverseX1) deltaX1 += 5F
            else deltaX1 -= 5F
            if(!reverseY1) deltaY1 += 5F
            else deltaY1 -= 5F

            if(deltaX2 > 100F) reverseX2 = true
            else if(deltaX2 < -100F) reverseX2 = false
            if(deltaY2 > 100F) reverseY2 = true
            else if(deltaY2 < -100F) reverseY2 = false
            if(!reverseX2) deltaX2 += 5F
            else deltaX2 -= 5F
            if(!reverseY2) deltaY2 += 5F
            else deltaY2 -= 5F

            if(deltaX3 > 100F) reverseX3 = true
            else if(deltaX3 < -100F) reverseX3 = false
            if(deltaY3 > 100F) reverseY3 = true
            else if(deltaY3 < -100F) reverseY3 = false
            if(!reverseX3) deltaX3 += 5F
            else deltaX3 -= 5F
            if(!reverseY3) deltaY3 += 5F
            else deltaY3 -= 5F

        }
    }

    class CustomView2 @JvmOverloads constructor(context: Context,
                                               attrs: AttributeSet? = null, defStyleAttr: Int = 0)
        : View(context, attrs, defStyleAttr) {

        val oval = RectF()

        private val paint = Paint()

        // Called when the view should render its content.
        override fun onDraw(canvas: Canvas?) {
            super.onDraw(canvas)

            val width = measuredWidth.toFloat()
            val height = measuredHeight.toFloat()
            val radius = width/4
            val centerX = width / 2
            val centerY = height / 2

            paint.color = Color.WHITE
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 30F
            //canvas!!.drawCircle(width/2, height/2, radius, paint)

            oval.set(centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius)
            canvas!!.drawArc(oval, 270F, 100F, false, paint)

            invalidate()
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        val permissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
            startActivity(Intent(this, AskToGivePermissions::class.java))
            this.finish()
        }

        //инициализация переменных
        preview = findViewById<View>(R.id.preview) as SurfaceView
        cancelButton = findViewById<Button>(R.id.cancel_button)
        previewHolder = preview?.holder
        previewHolder!!.addCallback(surfaceCallback)
        text = findViewById(R.id.result_info_text)
        imgavgtxt = findViewById(R.id.red_level_text)
        timeText = findViewById(R.id.time_text)

        timer.start()
        cancelButton?.setOnClickListener {
            moveTaskToBack(true);
            exitProcess(-1)
        }

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
            if ((endTime - generalStartTime)/1000.0 > measureTimeInSec) {

                var toAdd: Double?
                for(i in 1 until generalBeatsTime.size) {
                    toAdd = ((generalBeatsTime[i] - generalBeatsTime[i - 1]) * 100).roundToInt()/100.0
                    if(toAdd < 1.2 && toAdd > 0.6) intervalsBeatsTime.add(toAdd)
                }

                //val SD = calculateSD(intervalsBeatsTime.toDoubleArray())
                val SD = (intervalsBeatsTime.standardDeviation()*1000).toInt()/1000.0
                val MRR = (intervalsBeatsTime.average()*1000).toInt()/1000.0
                val MxDMn = ((intervalsBeatsTime.maxOrNull()!! - intervalsBeatsTime.minOrNull()!!)*1000).toInt()/1000.0
                val (Mo, freq) = modeOf(intervalsBeatsTime.toTypedArray())
                val AMo = ((freq.toDouble() / intervalsBeatsTime.size)*10000).toInt()/100.0
                val CV = (10000*SD/MRR).toInt()/100.0

                var sumOfDiff = 0.0
                var numOfDiff = 0.0
                for(i in 1 until intervalsBeatsTime.size) {
                    sumOfDiff += (intervalsBeatsTime[i] - intervalsBeatsTime[i-1]).pow(2)
                    numOfDiff += 1.0
                }
                val RMSSD = (sqrt(sumOfDiff/numOfDiff)*100).toInt()/100.0
                val intent = Intent(this, Result::class.java)

                val values = listOf(
                    param(SD*1000, "SDNN", "мс", 30.0, 96.0, "Стандартное отклонение интервалов. Показывает состояние вегетативной нервной системы"),
                    param(MRR*1000, "MRR", "мс", 660.0, 1370.0, "Средняя длительность интервалов. Оценивает напряженность организма в целом"),
                    param(MxDMn*1000, "MxDMn", "мс", 120.0, 450.0, "Размах интервалов. Чем выше, тем ниже напряжение"),
                    param(Mo*1000, "Mo", "мс", 660.0, 1370.0, "Самая частая длина интервалов. Отвечает за стабильность процессов в организме"),
                    param(RMSSD*1000, "RMSSD", "мс", 20.0, 89.0, "Корень из среднего значения квадратов различий между соседними интервалами. Оценка активности парасимпатического отдела"),
                    param(AMo, "AMo50", "%", 26.0, 50.0, "Амплитуда моды. Показывает активность парасимпатического отдела"),
                    param(CV, "CV", "%", 5.1, 8.3, "Коэффициент вариабельности. Основной показатель здоровья сердца")
                )


                for(elem in values) {

                    var colour = "red"
                    if(elem.value >= elem.minValue && elem.value <= elem.maxValue)
                        colour = "green"
                    else if(min(elem.value-elem.maxValue, elem.value-elem.minValue) < 0.2*(elem.maxValue - elem.minValue))
                        colour = "yellow"

                    intent.putStringArrayListExtra(
                        elem.name,
                        arrayListOf(
                            "${elem.value} ${elem.dimension}",
                            "Норма ${elem.minValue}-${elem.maxValue}",
                            colour,
                            elem.description
                        )
                    )
                }

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)

                startActivity(intent)

                finish()

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
data class param(
    val value: Double,
    val name: String,
    val dimension: String,
    val minValue: Double,
    val maxValue: Double,
    val description: String
)