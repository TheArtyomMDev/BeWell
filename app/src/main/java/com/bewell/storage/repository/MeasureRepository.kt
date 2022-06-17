package com.bewell.storage.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bewell.data.Measure
import com.bewell.utils.Constants.TAG
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.util.*

class MeasureRepository(private val db: FirebaseFirestore) {

    suspend fun addMeasure(email: String, id: String,  measure: Measure): Boolean {

        val toAdd = hashMapOf(
            id to measure
        )

        return try {
            db.collection("measure")
                .document(email)
                .set(toAdd, SetOptions.merge())
                .await()

            Log.d(TAG, "Saved with id $id")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getIdForCollection(collection: String): String {
        return db.collection(collection).document().id
    }

    suspend fun getMeasuresFromDate(date: Date, email: String): List<Measure> {
        val result = mutableListOf<Measure>()
        //val ret = MutableLiveData<List<Measure>>()
        val milliSecondsInOneDay = 24 * 60 * 60 * 1000

        val dayStartTime = date.time - date.time % milliSecondsInOneDay
        val dayEndTime = dayStartTime + milliSecondsInOneDay

        val res = db.collection("measure").document(email).get().await().data
        val keys = res?.keys

        if (keys != null) for(key in keys) {
            println(key)
            val measureMap = res[key] as HashMap<String, Any>
            println(measureMap)

            if (measureMap["timeCreated"] in dayStartTime until dayEndTime) {
                val measure = Measure("", 0L, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

                for (field in measure.javaClass.declaredFields) {
                    field.isAccessible = true
                    println("name: ${field.name}")

                    field.set(measure, measureMap[field.name])
                }

                result.add(measure)
            }
        }

        return result.toList()
    }
}