package com.bewell.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.bewell.data.Measure
import com.bewell.storage.repository.MeasureRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class MeasuresListViewModel(private  val measureRepo: MeasureRepository, private val auth: FirebaseAuth) : ViewModel() {

    /*
    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

     */

    lateinit var result: LiveData<List<Measure>>
    var measuresLD = MutableLiveData<List<Measure>>().apply { value = listOf() }

    fun getMeasuresFromDate(date: Date, email: String) {
        CoroutineScope(Dispatchers.IO).launch {
            measuresLD.postValue(measureRepo.getMeasuresFromDate(date, email))
        }
    }

    fun deleteMeasure(id: String) {
        measureRepo.deleteMeasure(auth.currentUser!!.email!!, id)
    }
}