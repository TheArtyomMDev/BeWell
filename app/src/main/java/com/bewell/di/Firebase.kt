package com.bewell.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.koin.dsl.module

var firebaseModule = module {
    fun provideFirestore(firebase: Firebase): FirebaseFirestore {
        return firebase.firestore
    }

    fun provideAuth(firebase: Firebase): FirebaseAuth {
        return firebase.auth
    }

    fun provideFirebase(): Firebase {
        return Firebase
    }

    single {
        provideFirebase()
    }

    single {
        provideFirestore(get())
    }

    single {
        provideAuth(get())
    }
}