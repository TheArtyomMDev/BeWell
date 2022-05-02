package com.bewell.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bewell.R
import com.bewell.databinding.ActivityAskToGivePermissionsBinding
import com.bewell.databinding.ActivityResultBinding
import com.bewell.presenter.GivePermissionsPresenter
import com.bewell.presenter.ResultPresenter
import com.bewell.utils.Constants.REQUEST_CODE_CAMERA

class GivePermissionsView : AppCompatActivity() {

    private lateinit var binding: ActivityAskToGivePermissionsBinding
    private lateinit var presenter: GivePermissionsPresenter

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAskToGivePermissionsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        presenter = GivePermissionsPresenter()
        presenter.attachView(this)

        binding.givePermissionsButton.setOnClickListener {
            presenter.askPermissions(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        presenter.checkPermissions(requestCode, grantResults)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

}