package com.bewell.view

import android.content.Intent
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bewell.R
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

        //presenter.setupCollapsingToolbar(binding.collapsingToolbar)
        presenter.setupResultsRecyclerView(binding.resultsRecyclerView)

        presenter.uploadMeasureInfo()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }
}

