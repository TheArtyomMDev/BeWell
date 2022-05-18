package com.bewell.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bewell.databinding.ActivityResultBinding
import com.bewell.presenter.ResultPresenter

class ResultView : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var presenter: ResultPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        presenter = ResultPresenter()
        presenter.attachView(this)

        presenter.setupResultsRecyclerView(binding.resultsRecyclerView, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }
}
