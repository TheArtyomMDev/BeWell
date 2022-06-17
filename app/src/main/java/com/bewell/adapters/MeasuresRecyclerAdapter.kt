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
import com.bewell.storage.repository.MeasureRepository
import com.bewell.viewmodels.MeasureResultViewModel
import java.security.PrivateKey

class MeasuresRecyclerAdapter(
    val context: Context,
    lifecycle: LifecycleOwner,
    private val measuresLD: LiveData<List<Measure>>,
    val deleteMeasure: (id: String) -> Unit) :
    RecyclerView.Adapter<MeasuresRecyclerAdapter.ListViewHolder>()  {

    var intent = Intent(context, MeasureResultActivity::class.java)

    init {
        measuresLD.observe(lifecycle) {
            println("observed")
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
        holder.binding.delete.setOnClickListener {
            deleteMeasure(measuresLD.value!![position].id)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount() = measuresLD.value!!.size
}