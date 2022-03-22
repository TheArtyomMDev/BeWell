package com.example.sachin.heart

import android.content.Context
import android.graphics.*
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.TextView
import java.lang.reflect.Array


class Result : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val resultInfoText = findViewById<TextView>(R.id.result_info_text)

        var out = ""
        val bundle = intent.extras
        if (bundle != null) {
            for (key in bundle.keySet()) {
                out += "$key: ${bundle[key]}\n"
            }
        }

        resultInfoText.text = out
    }
}