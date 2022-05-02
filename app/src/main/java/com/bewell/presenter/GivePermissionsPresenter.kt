package com.bewell.presenter

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat.startActivity
import com.bewell.base.MainContract
import com.bewell.utils.Constants
import com.bewell.view.GivePermissionsView
import com.bewell.view.StartMeasureView

class GivePermissionsPresenter: MainContract.Presenter<GivePermissionsView>  {
    private var view: GivePermissionsView? = null

    override fun attachView(givePermissionsView: GivePermissionsView) {
        view = givePermissionsView
    }

    override fun detachView() {
        view = null
    }

    fun askPermissions(activity: Activity) {
        Log.d("AskForPermissions", "ASKED")
        requestPermissions(
            activity,
            arrayOf(Manifest.permission.CAMERA),
            Constants.REQUEST_CODE_CAMERA)
    }

    fun checkPermissions(requestCode: Int, grantResults: IntArray) {
        when (requestCode) {
            Constants.REQUEST_CODE_CAMERA -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    val intent = Intent(view!!.applicationContext, StartMeasureView::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(view!!.applicationContext, intent, null)
                }
            }
        }
    }

}