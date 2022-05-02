package com.bewell.presenter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Camera
import android.os.PowerManager
import android.util.Log
import android.view.SurfaceHolder
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import com.bewell.R
import com.bewell.base.MainContract
import com.bewell.model.HRVMeasureModel.param
import com.bewell.storage.Preferences
import com.bewell.storage.Preferences.Companion.PREF_GENERAL_START_TIME_IN_MILLI_SEC
import com.bewell.storage.Preferences.Companion.PREF_GOING
import com.bewell.ui.*
import com.bewell.utils.Constants
import com.bewell.utils.Constants.TAG
import com.bewell.utils.ImageProcessing
import com.bewell.view.HRVMeasureView
import com.bewell.view.ResultView
import org.nield.kotlinstatistics.standardDeviation
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

class HRVMeasurePresenter: MainContract.Presenter<HRVMeasureView> {
    private var view: HRVMeasureView? = null

    private lateinit var previewHolder: SurfaceHolder
    private lateinit var wakeLock: PowerManager.WakeLock
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var bpmText: TextView

    private val processing = AtomicBoolean(false)
    private var camera: Camera? = null

    private var going = false
    private var measureTimeInSec = 0
    private var generalStartTimeInMilliSec: Long = 0
    private var imageAverage = 0
    private var averageIndex = 0
    private val averageArraySize = 4
    private var beatsIndex = 0
    private val beatsArraySize = 3
    private val beatsArray = IntArray(beatsArraySize)
    private var beats = 0.0
    private var startTime: Long = 0
    private var currentBeatTime: Long = 0
    private var lastBeatTime: Long = 0

    var current = Constants.TYPE.GREEN
        private set

    private val averageArray = IntArray(averageArraySize)
    private var generalBeatsTime: MutableList<Double> = ArrayList()
    private var intervalsBeatsTime: MutableList<Double> = ArrayList()

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
                Log.d(Constants.TAG, "Using width=" + size.width + " height=" + size.height)
            }
            camera!!.parameters = parameters
            camera!!.startPreview()
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            // Ignore
        }
    }
    private val previewCallback = Camera.PreviewCallback { data, cam ->

        if (data == null) throw NullPointerException()
        val size = cam.parameters.previewSize ?: throw NullPointerException()
        if (!processing.compareAndSet(false, true)) return@PreviewCallback
        val width = size.width
        val height = size.height
        imageAverage = ImageProcessing.decodeYUV420SPtoRedAvg(data.clone(), height, width)

        updateProcessing(processing)

        if (imageAverage < 180 || imageAverage == 255) {
            processing.set(false)
            updateProcessing(processing)
            startValues()
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

        if (generalStartTimeInMilliSec == 0L) {
            generalStartTimeInMilliSec = System.currentTimeMillis()
            sharedPreferences.edit().putLong(PREF_GENERAL_START_TIME_IN_MILLI_SEC, generalStartTimeInMilliSec).apply()
        }

        if (imageAverage < rollingAverage) {
            newType = Constants.TYPE.RED
            if (newType != current) {

                beats++
                Log.d(TAG, "BEAT!! beats=$beats")

                currentBeatTime = System.currentTimeMillis()

                generalBeatsTime.add((currentBeatTime - generalStartTimeInMilliSec.toDouble()) / 1000)

                lastBeatTime = currentBeatTime
            }
        } else if (imageAverage > rollingAverage) {
            newType = Constants.TYPE.GREEN
        }

        if (averageIndex == averageArraySize) averageIndex = 0
        averageArray[averageIndex] = imageAverage
        averageIndex++

        // Transitioned from one state to another to the same
        if (newType != current) current = newType

        val endTime = System.currentTimeMillis()
        val totalTimeInSecs = (endTime - startTime) / 1000.0
        if (totalTimeInSecs >= 10) {

            val bps = beats / totalTimeInSecs
            val dpm = (bps * 60.0).toInt()

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
            bpmText.text = "$beatsAvg bpm"
            startTime = System.currentTimeMillis()
            beats = 0.0
        }
        if ((endTime - generalStartTimeInMilliSec) / 1000.0 > measureTimeInSec) {

            var toAdd: Double?
            for (i in 1 until generalBeatsTime.size) {
                toAdd = ((generalBeatsTime[i] - generalBeatsTime[i - 1]) * 100).roundToInt() / 100.0
                if (toAdd < 1.2 && toAdd > 0.6) intervalsBeatsTime.add(toAdd)
            }

            //val SD = calculateSD(intervalsBeatsTime.toDoubleArray())
            val SD = (intervalsBeatsTime.standardDeviation() * 1000).toInt() / 1000.0
            val MRR = (intervalsBeatsTime.average() * 1000).toInt() / 1000.0
            val MxDMn =
                ((intervalsBeatsTime.maxOrNull()!! - intervalsBeatsTime.minOrNull()!!) * 1000).toInt() / 1000.0
            val (Mo, freq) = modeOf(intervalsBeatsTime.toTypedArray())
            val AMo = ((freq.toDouble() / intervalsBeatsTime.size) * 10000).toInt() / 100.0
            val CV = (10000 * SD / MRR).toInt() / 100.0

            var sumOfDiff = 0.0
            var numOfDiff = 0.0
            for (i in 1 until intervalsBeatsTime.size) {
                sumOfDiff += (intervalsBeatsTime[i] - intervalsBeatsTime[i - 1]).pow(2)
                numOfDiff += 1.0
            }
            val RMSSD = (sqrt(sumOfDiff / numOfDiff) * 100).toInt() / 100.0
            val intent = Intent(view!!.applicationContext, ResultView::class.java)

            val values = listOf(
                param(
                    SD * 1000,
                    "SDNN",
                    "мс",
                    30.0,
                    96.0,
                    "Стандартное отклонение интервалов. Показывает состояние вегетативной нервной системы"
                ),
                param(
                    MRR * 1000,
                    "MRR",
                    "мс",
                    660.0,
                    1370.0,
                    "Средняя длительность интервалов. Оценивает напряженность организма в целом"
                ),
                param(
                    MxDMn * 1000,
                    "MxDMn",
                    "мс",
                    120.0,
                    450.0,
                    "Размах интервалов. Чем выше, тем ниже напряжение"
                ),
                param(
                    Mo * 1000,
                    "Mo",
                    "мс",
                    660.0,
                    1370.0,
                    "Самая частая длина интервалов. Отвечает за стабильность процессов в организме"
                ),
                param(
                    RMSSD * 1000,
                    "RMSSD",
                    "мс",
                    15.0,
                    90.0,
                    "Корень из среднего значения квадратов различий между соседними интервалами. Оценка активности парасимпатического отдела"
                ),
                param(
                    AMo,
                    "AMo50",
                    "%",
                    26.0,
                    50.0,
                    "Амплитуда моды. Показывает активность парасимпатического отдела"
                ),
                param(
                    CV,
                    "CV",
                    "%",
                    5.1,
                    8.3,
                    view!!.resources.getString(R.string.cv_info)
                )
            )


            for (elem in values) {

                var colour = "red"
                if (elem.value >= elem.minValue && elem.value <= elem.maxValue)
                    colour = "green"
                else if (min(
                        elem.value - elem.maxValue,
                        elem.value - elem.minValue
                    ) < 0.2 * (elem.maxValue - elem.minValue)
                )
                    colour = "yellow"

                intent.putStringArrayListExtra(
                    elem.name,
                    arrayListOf(
                        "${elem.value} ${elem.dimension}",
                        "${view!!.resources.getString(R.string.norm)} ${elem.minValue}-${elem.maxValue}",
                        colour,
                        elem.description
                    )
                )
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)

            startActivity(view!!.applicationContext, intent, null)

            view!!.finish()

        }
        processing.set(false)
    }

    override fun attachView(hrvMeasureView: HRVMeasureView) {
        view = hrvMeasureView
    }

    override fun detachView() {
        view = null
    }

    fun setup(activity: Activity, intent: Intent, holder: SurfaceHolder, bpmText: TextView) {
        holder.addCallback(surfaceCallback)

        val pm = activity.getSystemService(Activity.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, Constants.WAKELOCK_TAG)

        sharedPreferences = view!!.getSharedPreferences(Preferences.FILE_NAME, Context.MODE_PRIVATE)

        this.previewHolder = holder
        this.bpmText = bpmText
        this.measureTimeInSec = sharedPreferences.getInt(
            Preferences.PREF_MEASURE_TIME_IN_SEC, 0)
        //this.generalStartTimeInMilliSec = sharedPreferences.getLong(
            //Preferences.PREF_GENERAL_START_TIME_IN_MILLI_SEC, 0)
    }

    fun onResume() {
        wakeLock.acquire(Constants.WAKELOCK_TIME * 1000L)
        camera = Camera.open()
        startTime = System.currentTimeMillis()
    }

    fun onPause() {
        wakeLock.release()
        camera!!.setPreviewCallback(null)
        camera!!.stopPreview()
        camera!!.release()
        camera = null
    }

    private fun <T> modeOf(a: Array<T>): Pair<T, Int> {
        val sortedByFreq = a.groupBy { it }.entries.sortedByDescending { it.value.size }
        val maxFreq = sortedByFreq.first().value.size
        val modes = sortedByFreq.takeWhile { it.value.size == maxFreq }
        return Pair(modes.first().key, maxFreq)
    }

    private fun updateProcessing(x: AtomicBoolean) {
        sharedPreferences.edit().putBoolean(PREF_GOING, x.get()).apply()
        going = x.get()
        //println("set to ${x.get()}")
    }

    private fun startValues() {
        generalStartTimeInMilliSec = System.currentTimeMillis()
        sharedPreferences.edit().putLong(PREF_GENERAL_START_TIME_IN_MILLI_SEC, generalStartTimeInMilliSec).apply()
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

