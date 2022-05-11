package com.bewell.presenter

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.*
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.widget.Button
import android.widget.RadioGroup
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.bewell.base.MainContract
import com.bewell.storage.Preferences.Companion.FILE_NAME
import com.bewell.storage.Preferences.Companion.PREF_MEASURE_TIME_IN_SEC
import com.bewell.view.GivePermissionsView
import com.bewell.view.HRVMeasureView
import com.bewell.view.StartMeasureView
import java.lang.Exception

class StartMeasurePresenter: MainContract.Presenter<StartMeasureView> {
    private var view: StartMeasureView? = null
    private var measureTimeInSec = 0
    private lateinit var sharedPreferences: SharedPreferences

    override fun attachView(startMeasureView: StartMeasureView) {
        view = startMeasureView
    }

    override fun detachView() {
        view = null
    }

    fun onTimeChosen(confirmButton: Button, timesRadioGroup: RadioGroup, checkedId: Int) {

        val order = timesRadioGroup.indexOfChild(timesRadioGroup.findViewById(checkedId))
        confirmButton.isEnabled = true

        measureTimeInSec = when(order) {
            0 -> 150
            1 -> 300
            2 -> 450
            else -> throw Exception("Wrong id of radiobutton")
        }
    }

    fun onConfirmButtonClicked(activity: Activity, radioGroup: RadioGroup) {
        sharedPreferences = view!!.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putInt(PREF_MEASURE_TIME_IN_SEC, measureTimeInSec).apply()

        val intent = Intent(view, HRVMeasureView::class.java)
        intent.flags = FLAG_ACTIVITY_NEW_TASK
        startActivity(view!!.applicationContext, intent, null)

        //activity.finish()
    }

    fun checkPermissions(activity: Activity) {
        val permissionStatus = ContextCompat.checkSelfPermission(view!!.applicationContext, Manifest.permission.CAMERA)

        if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(view!!.applicationContext, GivePermissionsView::class.java)
            intent.flags = FLAG_ACTIVITY_NEW_TASK
            startActivity(view!!.applicationContext, intent, null)
            activity.finish()
        }
    }
}