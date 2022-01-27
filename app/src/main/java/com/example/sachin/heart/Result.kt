package com.example.sachin.heart

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class Result : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val BIText = findViewById<TextView>(R.id.BIText)
        BIText.text = intent.extras?.get("BI").toString()
    }
}