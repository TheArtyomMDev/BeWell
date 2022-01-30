package com.example.sachin.heart

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.TextView
import kotlin.math.roundToInt


class Result : AppCompatActivity() {

    companion object {
        var BItoText = 0F
        var backgroundLevel = 0
    }

    class BarView2 @JvmOverloads constructor(context: Context,
                                               attrs: AttributeSet? = null, defStyleAttr: Int = 0)
        : View(context, attrs, defStyleAttr) {

        private var resizedWidth = 900
        private var resizedHeight = 900
        private var radius = 364F
        private var deltaDown = -10.5F
        private var toAngle = 240F*(BItoText/500F)
        private var angle = 0F
        private var called = false

        private var levelBitmapBackground: Bitmap? = null
        private var resizedLevelBitmapBackground: Bitmap? = null

        private val paint = Paint()
        private val oval = RectF()
        private val oval2 = RectF()

        // Called when the view should render its content.
        override fun onDraw(canvas: Canvas?) {
            super.onDraw(canvas)

            if(!called) {
                if(BItoText <= 100) backgroundLevel = R.drawable.level_background_1
                else if(BItoText > 100 && BItoText <= 200) backgroundLevel = R.drawable.level_background_2
                else backgroundLevel = R.drawable.level_background_2

                toAngle = 240F*(BItoText/500F)

                levelBitmapBackground = BitmapFactory.decodeResource(resources, backgroundLevel)
                resizedLevelBitmapBackground = Bitmap.createScaledBitmap(levelBitmapBackground!!, resizedWidth, resizedHeight, false)

                called = true
            }

            canvas!!.drawBitmap(resizedLevelBitmapBackground!!, width/2 - resizedWidth/2F, height/2 - resizedHeight/2F, paint)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 100F

            //цвет в зависимоти от BI
            if(BItoText <= 100) paint.color = Color.parseColor("#5DD37E")
            else if(BItoText > 100 && BItoText <= 200) paint.color = Color.parseColor("#F7F514")
            else if(BItoText > 200 && BItoText <= 400) paint.color = Color.parseColor("#F77E14")
            else paint.color = Color.parseColor("#F52B2B")

            oval.set(width/2 - radius - 5F, height/2 - radius - deltaDown - 5F, width/2 + radius, height/2 + radius - deltaDown)
            canvas.drawArc(oval, 150F, angle, false, paint)

            paint.style = Paint.Style.FILL
            oval2.set(width/2 - radius - 2F, height/2 + radius - 220F, width/2 - radius + 98F, height/2 + radius - 115F)
            canvas.drawOval(oval2, paint)

            if(angle < toAngle) angle += 0.5F
            println(angle)
            println(toAngle)

            invalidate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val againButton = findViewById<Button>(R.id.again_button)
        val resultText = findViewById<TextView>(R.id.result_text)
        val resultInfoText = findViewById<TextView>(R.id.result_info_text)

        val BI: Double = intent.extras?.get("BI") as Double
        BItoText = (BI * 100).roundToInt() /100F
        if(BItoText > 500) BItoText = 500F

        resultText.text = BItoText.toString()
        if(BItoText <= 100) resultInfoText.text = "Всё в норме"
        else if(200 > BItoText && BItoText > 100) resultInfoText.text = "Советуем посетить врача"
        else resultInfoText.text = "Незамедлительно запишитесь к врачу"

        againButton.setOnClickListener {
            startActivity(Intent(this, HeartRateMonitor::class.java))
        }
    }
}