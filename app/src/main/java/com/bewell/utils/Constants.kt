package com.bewell.utils

object Constants {
    const val TAG = "BeWell"
    const val WAKELOCK_TAG = "HRVMeasure:WakeLock"
    const val WAKELOCK_TIME = 600

    const val SDNN = "SDNN"
    const val RMSSD = "RMSSD"

    const val REQUEST_CODE_CAMERA = 100

    enum class TYPE {
        GREEN, RED
    }
}