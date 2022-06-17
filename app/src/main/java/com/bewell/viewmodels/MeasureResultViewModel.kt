package com.bewell.viewmodels

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.bewell.data.HRVParam
import com.bewell.data.Measure
import com.bewell.storage.repository.HRVParamRepository

class MeasureResultViewModel(hrvParamRepo: HRVParamRepository): ViewModel() {
    var intent: Intent? = null
        set(value) {
            field = value
            if(value != null) setMeasure(value)
        }
    var params: List<HRVParam> = hrvParamRepo.getAllParams()
    lateinit var measure: Measure

    private fun setMeasure(intent: Intent?) {
        measure = intent?.extras?.getSerializable("measure") as Measure
        println(measure)
    }
}
