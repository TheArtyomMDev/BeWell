package com.bewell.presenter

import android.app.Activity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bewell.base.MainContract
import com.bewell.ui.ResultRecyclerAdapter
import com.bewell.view.ResultView

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
    }

}
