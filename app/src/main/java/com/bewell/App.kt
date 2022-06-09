package com.bewell

import android.app.Application
import com.bewell.di.databaseModule
import com.bewell.di.firebaseModule
import com.bewell.di.viewModelsModule
import com.google.android.material.color.DynamicColors
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class App: Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@App)
            modules(listOf(databaseModule, viewModelsModule, firebaseModule))
        }

        // Apply dynamic color
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}