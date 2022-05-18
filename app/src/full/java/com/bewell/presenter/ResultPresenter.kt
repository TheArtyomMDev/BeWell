package com.bewell.presenter

import android.app.Activity
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bewell.base.MainContract
import com.bewell.ui.ResultRecyclerAdapter
import com.bewell.utils.Constants.TAG
import com.bewell.view.ResultView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import javax.security.auth.callback.Callback


class ResultPresenter: MainContract.Presenter<ResultView>  {
    private var view: ResultView? = null
    private var params = mutableListOf<Array<String>>()


    override fun attachView(resultView: ResultView) {
        view = resultView
    }

    override fun detachView() {
        view = null
    }

    fun setupResultsRecyclerView(resultsRecyclerView: RecyclerView, activity: Activity) {
        val myAdapter = ResultRecyclerAdapter(activity)
        resultsRecyclerView.layoutManager = LinearLayoutManager(view!!.applicationContext)
        resultsRecyclerView.adapter = myAdapter

        myAdapter.addData(view!!.intent)

        params = myAdapter.params

        uploadMeasureInfo()
    }

    fun uploadMeasureInfo() {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("GMT+7")
        val currentDate = sdf.format(Date())

        val db = Firebase.firestore
        val out = hashMapOf<String, String>()

        for (item in params) {
            out[item[0]] = item[1]
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
