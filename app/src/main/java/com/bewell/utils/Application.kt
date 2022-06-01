package com.bewell.utils

import android.app.Application
import com.google.android.material.color.DynamicColors

class Application: Application() {
    override fun onCreate() {
        super.onCreate()
        // Apply dynamic color
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}