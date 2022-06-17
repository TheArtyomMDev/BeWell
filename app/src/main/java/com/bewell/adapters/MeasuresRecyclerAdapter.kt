package com.bewell.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.bewell.MeasureResultActivity
import com.bewell.databinding.MeasureItemBinding
import com.bewell.data.Measure
import com.bewell.viewmodels.MeasureResultViewModel

class MeasuresRecyclerAdapter(
    val context: Context,
    lifecycle: LifecycleOwner,
    private val measuresLD: LiveData<List<Measure>>) :
    RecyclerView.Adapter<MeasuresRecyclerAdapter.ListViewHolder>()  {

    var intent = Intent(context, MeasureResultActivity::class.java)

    init {
        measuresLD.observe(lifecycle) {
            notifyDataSetChanged()
        }
    }


    class ListViewHolder(val binding: MeasureItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = MeasureItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.binding.measure = measuresLD.value!![position]
        holder.binding.cardContainer.setOnClickListener {
            intent.putExtra("measure", measuresLD.value!![position])
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = measuresLD.value!!.size
}