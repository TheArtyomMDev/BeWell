package com.bewell.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bewell.R
import java.lang.Exception


class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val okButton = findViewById<Button>(R.id.ok_button)
        val measureTimeEditText = findViewById<EditText>(R.id.measure_time_text)

        var measureTime = 0
        var measureTimeText = measureTimeEditText.text
        val permissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)

        if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
            startActivity(Intent(this, AskToGivePermissions::class.java))
            this.finish()
        }

        measureTimeEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                measureTimeText = measureTimeEditText.text

                try {
                    measureTime = measureTimeText.toString().toInt()
                    okButton.isEnabled = measureTime in 10..600
                } catch(e: Exception) {
                    okButton.isEnabled = false
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        okButton.setOnClickListener {
            val intent = Intent(this, HeartRateMonitor::class.java)
            intent.putExtra("measureTime", measureTime)
            startActivity(intent)
        }

    }
}