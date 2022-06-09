package com.bewell.viewmodels

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModel
import com.bewell.data.HRVParam
import com.bewell.utils.Constants.TAG
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.koin.core.context.GlobalContext.get

import java.text.SimpleDateFormat
import java.util.*


class MeasureResultViewModel(db: FirebaseFirestore): ViewModel() {
    var params = mutableListOf<HRVParam>()

    fun uploadMeasureInfo() {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("GMT+7")
        val currentDate = sdf.format(Date())

        val db = Firebase.firestore
        val out = hashMapOf<String, Double>()

        for (item in params) {
            out[item.name] = item.value
        }

        db.collection("measure").document(Firebase.auth.currentUser!!.email.toString())
            .set(hashMapOf(
                currentDate.toString() to out
            ), SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Measure added")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding measure", e)
            }

    }

}
