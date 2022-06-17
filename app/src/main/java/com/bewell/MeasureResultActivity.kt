package com.bewell

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bewell.adapters.ResultRecyclerAdapter
import com.bewell.databinding.ActivityResultBinding
import com.bewell.utils.Constants
import com.bewell.viewmodels.MeasureResultViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MeasureResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private val vm by viewModel<MeasureResultViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        vm.intent = intent
        setupRecyclerView(binding.resultsRecyclerView)
    }


    private fun setupRecyclerView(resultsRecyclerView: RecyclerView) {
        val myAdapter = ResultRecyclerAdapter(this)
        myAdapter.measure = vm.measure
        myAdapter.params = vm.params

        resultsRecyclerView.layoutManager = LinearLayoutManager(this)
        resultsRecyclerView.adapter = myAdapter
    }

}
