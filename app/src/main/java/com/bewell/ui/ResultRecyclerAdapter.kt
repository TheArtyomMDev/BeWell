package com.bewell.ui

import android.content.Context
import android.content.Intent
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.transition.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bewell.R
import com.bewell.databinding.RecyclerviewItemBinding

class ResultRecyclerAdapter(context: Context): RecyclerView
.Adapter<ResultRecyclerAdapter.MyViewHolder>() {

    val params = mutableListOf<Array<String>>()
    private lateinit var itemBinding: RecyclerviewItemBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        itemBinding = RecyclerviewItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(itemBinding)
    }

    class MyViewHolder(private val itemBinding: RecyclerviewItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {

    }

    fun addData(intent: Intent) {
        val bundle = intent.extras
        if (bundle != null) {
            for (key in bundle.keySet()) {
                val list = intent.getStringArrayListExtra(key)
                params.add(arrayOf(key, list!![0], list[1], list[2], list[3]))
            }
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        //holder.bind(params[position])

        val param = params[position]

        itemBinding.textViewLarge.text = param[0]
        itemBinding.textViewMiddle.text = param[1]
        itemBinding.textViewSmall.text = param[2]

        itemBinding.descriptionText.text = param[4]

        itemBinding.descriptionButton.setOnClickListener {
            // The transition of the hiddenView is carried out
            //  by the TransitionManager class.
            // Here we use an object of the AutoTransition
            // Class to create a default transition.

            if (itemBinding.descriptionText.visibility == View.VISIBLE) {
                itemBinding.descriptionText.visibility = View.GONE
                itemBinding.descriptionButton.text = "Подробнее"
            }

            // If the CardView is not expanded, set its visibility
            // to visible and change the expand more icon to expand less.
            else {
                itemBinding.descriptionText.visibility = View.VISIBLE
                itemBinding.descriptionButton.text = "Свернуть"
            }


            TransitionManager.beginDelayedTransition(itemBinding.cardView, AutoTransition())
        }

        when (param[3]) {
            "green" -> itemBinding.cardView.setCardBackgroundColor(
                ContextCompat.getColor(
                    itemBinding.cardView.context, R.color.soft_green
                ))
            "yellow" -> itemBinding.cardView.setCardBackgroundColor(
                ContextCompat.getColor(
                    itemBinding.cardView.context, R.color.soft_yellow
                ))
            "red" -> itemBinding.cardView.setCardBackgroundColor(
                ContextCompat.getColor(
                    itemBinding.cardView.context, R.color.soft_red
                ))
        }

    }

    override fun getItemCount() = params.size

}