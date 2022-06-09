package com.bewell.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bewell.databinding.MeasureItemBinding
import com.bewell.data.Measure

class MeasuresRecyclerAdapter(private val measures: List<Measure>) :
    RecyclerView.Adapter<MeasuresRecyclerAdapter.ListViewHolder>()  {

    class ListViewHolder(val binding: MeasureItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = MeasureItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {

    }

    override fun getItemCount() = measures.size
}