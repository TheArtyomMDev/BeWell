package com.bewell.view.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.bewell.storage.Preferences
import com.bewell.storage.Preferences.Companion.FILE_NAME
import com.bewell.storage.Preferences.Companion.PREF_GENERAL_START_TIME_IN_MILLI_SEC
import com.bewell.storage.Preferences.Companion.PREF_GOING
import com.bewell.storage.Preferences.Companion.PREF_MEASURE_TIME_IN_SEC

class MeasureProgressView @JvmOverloads constructor(context: Context,
                                            attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {

    private val oval = RectF()
    private val paint = Paint()
    private val primaryColour = TypedValue()
    private val surfaceColour = TypedValue()

    private var angle = 0.0F



    // Called when the view should render its content.
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        var sharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        var generalStartTimeInMilliSec =
            sharedPreferences.getLong(PREF_GENERAL_START_TIME_IN_MILLI_SEC, 0)
        var measureTimeInSec =
            sharedPreferences.getInt(PREF_MEASURE_TIME_IN_SEC, 0)

        println(generalStartTimeInMilliSec)

        context.theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, primaryColour, true)
        context.theme.resolveAttribute(com.google.android.material.R.attr.colorSurface, surfaceColour, true)

        val width = measuredWidth.toFloat()
        val height = measuredHeight.toFloat()
        val radius = width/8
        val centerX = width / 2
        val centerY = height / 2

        paint.color = primaryColour.data
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 30F
        //canvas!!.drawCircle(width/2, height/2, radius, paint)

        oval.set(centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius)
        canvas!!.drawArc(oval, 270F, angle, false, paint)

        angle = (360*(System.currentTimeMillis() - generalStartTimeInMilliSec)/(1000F*measureTimeInSec))

        invalidate()
    }
}