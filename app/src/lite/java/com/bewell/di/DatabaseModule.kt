package com.bewell.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.bewell.storage.AppDatabase
import com.bewell.storage.Preferences
import org.koin.dsl.module


val databaseModule = module {

    fun provideSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(Preferences.FILE_NAME, Context.MODE_PRIVATE)
    }

    fun provideMeasuresDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "database"
        ).build()
    }

    single {
        provideSharedPreferences(get())
    }

    single {
        provideMeasuresDatabase(get())
    }

}