package com.bewell.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bewell.databinding.ActivityResultBinding
import com.bewell.model.HRVParam
import com.bewell.presenter.ResultPresenter
import com.bewell.ui.ResultRecyclerAdapter

class ResultView : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var presenter: ResultPresenter
    private var params = mutableListOf<HRVParam>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        presenter = ResultPresenter()
        presenter.attachView(this)



        setupRecyclerView(binding.resultsRecyclerView)
    }

    private fun setupRecyclerView(resultsRecyclerView: RecyclerView) {
        val myAdapter = ResultRecyclerAdapter(this)
        resultsRecyclerView.layoutManager = LinearLayoutManager(this)
        resultsRecyclerView.adapter = myAdapter

        myAdapter.addData(intent)

        params = myAdapter.params

       // presenter.onParamsReceived(params)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }
}
