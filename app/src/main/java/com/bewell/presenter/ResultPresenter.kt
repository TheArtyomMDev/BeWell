package com.bewell.presenter

import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bewell.R
import com.bewell.base.MainContract
import com.bewell.ui.ResultRecyclerAdapter
import com.bewell.view.ResultView
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class ResultPresenter: MainContract.Presenter<ResultView>  {
    private var view: ResultView? = null
    private var params = mutableListOf<Array<String>>()

    override fun attachView(resultView: ResultView) {
        view = resultView
    }

    override fun detachView() {
        view = null
    }

    fun setupCollapsingToolbar(collapsingToolbar: CollapsingToolbarLayout) {
        val tf = ResourcesCompat.getFont(view!!.applicationContext, R.font.montserrat_semi_bold)
        collapsingToolbar.setCollapsedTitleTypeface(tf)
        collapsingToolbar.setExpandedTitleTypeface(tf)
    }

    fun setupResultsRecyclerView(resultsRecyclerView: RecyclerView) {
        val myAdapter = ResultRecyclerAdapter(view!!.applicationContext)
        resultsRecyclerView.layoutManager = LinearLayoutManager(view!!.applicationContext)
        resultsRecyclerView.adapter = myAdapter

        myAdapter.addData(view!!.intent)

        //params = myAdapter.params
    }

    fun uploadMeasureInfo() {
        val db = Firebase.firestore

    }

}
