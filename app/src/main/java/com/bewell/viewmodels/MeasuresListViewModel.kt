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

    val email = auth.currentUser!!.email!!
    var measuresLD = MutableLiveData<List<Measure>>().apply { value = listOf() }
    var isDataSynced = MutableLiveData<Boolean>().apply { value = false }
    var chosenTimeLD = MutableLiveData<Date>().apply { value = Date() }

    fun getMeasuresFromDate(date: Date) {
        CoroutineScope(Dispatchers.IO).launch {
            measuresLD.postValue(measureRepo.getMeasuresFromDate(date, email))
        }
    }

    fun deleteMeasure(id: String) {
        measureRepo.deleteMeasure(email, id)
    }

    init {
        measureRepo.setSnapshotListener(::getMeasuresFromDate, chosenTimeLD, auth.currentUser!!.email!!, isDataSynced)
    }
}