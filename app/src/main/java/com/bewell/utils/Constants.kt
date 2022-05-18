package com.bewell.utils

import com.bewell.R

object Constants {
    const val TAG = "BeWell"

    const val WAKELOCK_TAG = "HRVMeasure:WakeLock"
    const val WAKELOCK_TIME = 600


    const val RMSSD = "RMSSD"

    const val REQUEST_CODE_CAMERA = 100
    const val REQUEST_SIGNUP = 0

    object SDNN {
        const val NAME = "SDNN"

        const val MIN = 30.0
        const val MAX = 96.0

    }


    enum class TYPE {
        GREEN, RED
    }
}