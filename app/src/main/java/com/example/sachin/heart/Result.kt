package com.example.sachin.heart

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.TextView
import kotlin.math.roundToInt

class Result : AppCompatActivity() {
    class BarView2 @JvmOverloads constructor(context: Context,
                                               attrs: AttributeSet? = null, defStyleAttr: Int = 0)
        : View(context, attrs, defStyleAttr) {

        // Called when the view should render its content.
        override fun onDraw(canvas: Canvas?) {
            super.onDraw(canvas)
            invalidate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val againButton = findViewById<Button>(R.id.again_button)
        val resultText = findViewById<TextView>(R.id.result_text)

        var BI: Double = intent.extras?.get("BI") as Double
        var BItoText = Math.round(BI * 100)/100F

        resultText.text = BItoText.toString()

        againButton.setOnClickListener {
            startActivity(Intent(this, HeartRateMonitor::class.java))
        }
    }
}