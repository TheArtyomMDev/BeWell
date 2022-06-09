package com.bewell

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bewell.databinding.ActivityResultBinding
import com.bewell.data.HRVParam
import com.bewell.viewmodels.MeasureResultViewModel
import com.bewell.adapters.ResultRecyclerAdapter
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.android.ext.android.inject

class MeasureResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private val vm by viewModel<MeasureResultViewModel>()
    private var params = mutableListOf<HRVParam>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        fetchParams()
        setupRecyclerView(binding.resultsRecyclerView)
        vm.params = params
        vm.uploadMeasureInfo()
    }

    private fun fetchParams() {
        for (key in intent.extras!!.keySet()) {
            params.add(intent.getSerializableExtra(key) as HRVParam)
        }
    }

    private fun setupRecyclerView(resultsRecyclerView: RecyclerView) {
        val myAdapter = ResultRecyclerAdapter(this)
        resultsRecyclerView.layoutManager = LinearLayoutManager(this)
        resultsRecyclerView.adapter = myAdapter

        myAdapter.params = params
    }
}
