package com.bewell.di

import android.content.Context
import android.content.SharedPreferences
import com.bewell.storage.Preferences
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import org.koin.dsl.module

val databaseModule = module {

    fun provideSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(Preferences.FILE_NAME, Context.MODE_PRIVATE)
    }

    single {
        provideSharedPreferences(get())
    }

}