package com.bewell.view

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bewell.R
import com.bewell.databinding.ActivityStartBinding
import com.bewell.presenter.StartMeasurePresenter
import com.bewell.utils.Constants
import com.google.android.material.color.DynamicColors

class StartMeasureView : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding
    private lateinit var presenter: StartMeasurePresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        presenter = StartMeasurePresenter()
        presenter.attachView(this)

        presenter.checkPermissions(this)

        binding.timesRadioGroup.setOnCheckedChangeListener{ _, checkedId ->
            presenter.onTimeChosen(binding.confirmButton, binding.timesRadioGroup, checkedId)
        }

        binding.confirmButton.setOnClickListener {
            presenter.onConfirmButtonClicked(this, binding.timesRadioGroup)
            //this.finish()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }
}

