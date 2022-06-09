package com.bewell.viewmodels

import android.content.pm.PackageManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bewell.utils.Constants

class AskToGivePermissionsViewModel: ViewModel()  {
    var requestResult = MutableLiveData<Boolean>().apply { postValue(false) }

    fun checkPermissions(requestCode: Int, grantResults: IntArray) {
        when (requestCode) {
            Constants.REQUEST_CODE_CAMERA -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                    requestResult.value = true
            }
        }
    }
}
