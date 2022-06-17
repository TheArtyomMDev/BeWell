package com.bewell.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.bewell.storage.AppDatabase
import com.bewell.storage.repository.MeasureRepository
import com.bewell.storage.Preferences
import com.bewell.storage.repository.HRVParamRepository
import org.koin.dsl.module


val databaseModule = module {

    fun provideSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(Preferences.FILE_NAME, Context.MODE_PRIVATE)
    }

    fun provideHRVParamsDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "database")
            .createFromAsset("databases/database.db")
            .allowMainThreadQueries()
            .build()
    }

    single {
        provideSharedPreferences(get())
    }

    single {
        provideHRVParamsDatabase(get())
    }

    single {
        MeasureRepository(get())
    }

    single {
        HRVParamRepository(get())
    }

}