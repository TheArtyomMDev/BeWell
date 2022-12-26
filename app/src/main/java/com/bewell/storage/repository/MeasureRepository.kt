package com.bewell.storage.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.bewell.data.Measure
import com.bewell.data.State
import com.bewell.utils.Constants.TAG
import com.google.common.util.concurrent.Service
import com.google.firebase.firestore.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

import java.util.*

class MeasureRepository(private val db: FirebaseFirestore) {
    lateinit var snapshotListener: ListenerRegistration

    suspend fun addMeasure(email: String, id: String, measure: Measure): Boolean {

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

    fun deleteMeasure(email: String, id: String) {
        db.collection("measure")
            .document(email)
            .update(
                mapOf(
                id to FieldValue.delete()
            )
            )

    }

    fun setSnapshotListener(func: (Date) -> Unit, chosenTimeLD: MutableLiveData<Date>, email: String, isDataSynced: MutableLiveData<Boolean>) {
        snapshotListener = db
            .collection("measure")
            .document(email)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, _ ->
                func(chosenTimeLD.value!!)
                if(snapshot?.metadata?.hasPendingWrites() != null)
                    isDataSynced.value = !(snapshot.metadata.hasPendingWrites())

                println("isDataSynced: ${isDataSynced.value}")
            }
    }

    fun getId(): String {
        return UUID.randomUUID().toString()
    }

    /*
    fun getAllMeasures(email: String) = flow<State<List<Measure>>> {
        emit(State.loading())

        val result = mutableListOf<Measure>()

        val res = db.collection("measure").document(email).get().await().data
        val keys = res?.keys

        if (keys != null) for(key in keys) {
            println(key)
            val measureMap = res[key] as HashMap<String, Any>
            println(measureMap)

            val measure = Measure("", 0L, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

            for (field in measure.javaClass.declaredFields) {
                field.isAccessible = true
                println("name: ${field.name}")

                field.set(measure, measureMap[field.name])
            }

            result.add(measure)

        }


        emit(State.success(result.toList()))
    }

     */

    suspend fun getMeasuresFromDate(date: Date, email: String): List<Measure> {
        val result = mutableListOf<Measure>()
        val calendar = Calendar.getInstance()
        val milliSecondsInOneDay = 24 * 60 * 60 * 1000

        calendar.time = date
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0

        val dayStartTime = calendar.time.time
        val dayEndTime = dayStartTime + milliSecondsInOneDay

        println("$dayStartTime - $dayEndTime, ${date.time}")

        val res = db.collection("measure").document(email).get().await().data
        val keys = res?.keys

        // println(keys!!.toList())

        if (keys != null) for(key in keys) {
            println(key)
            val measureMap = res[key] as Map<String, Any>
            // println(measureMap)

            val timeCreated = measureMap["timeCreated"] as Long

            if(timeCreated in dayStartTime until dayEndTime) {
                println(measureMap["timeCreated"])
                val measure =
                    Measure("", 0L, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

                for (field in measure.javaClass.declaredFields) {
                    field.isAccessible = true
                    //println("name: ${field.name}")
                    field.set(measure, measureMap[field.name])
                }

                result.add(measure)
            }

        }

        return result.toList()
    }
}