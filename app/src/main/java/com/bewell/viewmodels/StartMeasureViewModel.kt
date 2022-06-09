package com.bewell.viewmodels

import android.app.Application
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.bewell.BuildConfig
import com.bewell.storage.Preferences.Companion.PREF_MEASURE_TIME_IN_SEC
import java.lang.Exception

class StartMeasureViewModel(application: Application, private val sharedPreferences: SharedPreferences):
    AndroidViewModel(application) {

    private var measureTimeInSec = 0
    var permissionsRequestResult = MutableLiveData<Boolean>().apply { postValue(true) }

    fun requestPermissions(permissions: List<String>) {
        for(permission in permissions) {
            val permissionStatus = ContextCompat.checkSelfPermission(
                getApplication<Application>().applicationContext, permission)

            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                permissionsRequestResult.value = false
                break
            }
        }
    }

    fun onTimeChosen(order: Int) {
        measureTimeInSec = when(order) {
            0 -> if (BuildConfig.DEBUG) 10 else 150
            1 -> 300
            2 -> 450
            else -> throw Exception("Wrong id of radiobutton")
        }
        sharedPreferences.edit().putInt(PREF_MEASURE_TIME_IN_SEC, measureTimeInSec).apply()
    }

}