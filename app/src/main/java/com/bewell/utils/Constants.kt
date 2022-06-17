package com.bewell.utils

import android.app.Application
import com.bewell.R
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object Constants: KoinComponent {

    val context: Application by inject()
    const val TAG = "BeWell"

    const val WAKELOCK_TAG = "HRVMeasure:WakeLock"
    const val WAKELOCK_TIME = 600

    const val REQUEST_CODE_CAMERA = 100

    object SDNN {
        val dimension = context.resources.getString(R.string.ms_dimesion)
        val minValue = 30.0
        val maxValue = 96.0
        val info = context.resources.getString(R.string.sdnn_info)
    }

    object MRR {
        val dimension = context.resources.getString(R.string.ms_dimesion)
        val minValue = 660.0
        val maxValue = 1370.0
        val info = context.resources.getString(R.string.mrr_info)
    }

    object MxDMn {

        val dimension = context.resources.getString(R.string.ms_dimesion)
        val minValue = 120.0
        val maxValue = 450.0
        val info = context.resources.getString(R.string.mxdmn_info)
    }

    object Mode {
        const val name = "Mode"
        val dimension = context.resources.getString(R.string.ms_dimesion)
        val minValue = 660.0
        val maxValue = 1370.0
        val info = context.resources.getString(R.string.mode_info)
    }

    object RMSSD {
        const val name = "RMSSD"
        val dimension = context.resources.getString(R.string.ms_dimesion)
        val minValue = 15.0
        val maxValue = 90.0
        val info = context.resources.getString(R.string.rmssd_info)
    }

    object AMo50 {
        const val name = "AMo50"
        val dimension = "%"
        val minValue = 26.0
        val maxValue = 50.0
        val info = context.resources.getString(R.string.amo50_info)
    }

    object CV {
        const val name = "CV"
        val dimension = "%"
        val minValue = 5.1
        val maxValue = 8.3
        val info = context.resources.getString(R.string.cv_info)
    }

    object IN {
        const val name = "IN"
        val dimension = context.resources.getString(R.string.points_dimension)
        val minValue = 30.0
        val maxValue = 140.0
        val info = context.resources.getString(R.string.in_info)
    }


    enum class TYPE {
        GREEN, RED
    }
}