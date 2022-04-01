package com.bewell.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import android.util.Log
import android.widget.Button
import com.bewell.R

class AskToGivePermissions : AppCompatActivity() {
    private val REQUEST_CODE_CAMERA = 100

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ask_to_give_permissions)

        //спрашиваем разрешение на использование камеры
        val givePermissionsButton = findViewById<Button>(R.id.give_permissions_button)
        givePermissionsButton.setOnClickListener {
            Log.d("AskForPermissions", "ASKED")
            requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CODE_CAMERA)
        }
    }

    //в зависимоти от результата выдачи разрешения запускаем основное приложение
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_CAMERA -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    startActivity(Intent(this, StartActivity::class.java))
                }
                return
            }

        }
    }
}