package com.example.sachin.heart

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
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.CollapsingToolbarLayout

class Result : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val collapsingToolbar = findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar)
        val tf = ResourcesCompat.getFont(this, R.font.montserrat_semi_bold)
        collapsingToolbar.setCollapsedTitleTypeface(tf)
        collapsingToolbar.setExpandedTitleTypeface(tf)

        val recyclerView: RecyclerView = findViewById(R.id.results_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = CustomRecyclerAdapter(fillList())
    }

    private fun fillList(): MutableList<Array<String>> {

        val data = mutableListOf<Array<String>>()
        val bundle = intent.extras
        if (bundle != null) {
            for (key in bundle.keySet()) {
                val list = intent.getStringArrayListExtra(key)
                data.add(arrayOf(key, list!![0], list[1], list[2], list[3]))
            }
        }

        return data
    }

}

class CustomRecyclerAdapter(private val params: MutableList<Array<String>>) : RecyclerView
.Adapter<CustomRecyclerAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val largeTextView: TextView = itemView.findViewById(R.id.textViewLarge)
        val descriptionText: TextView = itemView.findViewById(R.id.description_text)
        val middleTextView: TextView = itemView.findViewById(R.id.textViewMiddle)
        val smallTextView: TextView = itemView.findViewById(R.id.textViewSmall)
        val card: CardView = itemView.findViewById(R.id.card_view)
        val descriptionButton: Button = itemView.findViewById(R.id.description_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val param = params[position]

        holder.largeTextView.text = param[0]
        holder.middleTextView.text = param[1]
        holder.smallTextView.text = param[2]

        holder.descriptionText.text = param[4]

        holder.descriptionButton.setOnClickListener {
            // The transition of the hiddenView is carried out
            //  by the TransitionManager class.
            // Here we use an object of the AutoTransition
            // Class to create a default transition.

            if (holder.descriptionText.visibility == View.VISIBLE) {
                holder.descriptionText.visibility = View.GONE
                holder.descriptionButton.text = "Подробнее"
            }

            // If the CardView is not expanded, set its visibility
            // to visible and change the expand more icon to expand less.
            else {
                holder.descriptionText.visibility = View.VISIBLE
                holder.descriptionButton.text = "Свернуть"
            }

            TransitionManager.beginDelayedTransition(holder.card, AutoTransition())
        }

        when (param[3]) {
            "green" -> holder.card.setCardBackgroundColor(ContextCompat.getColor(
                holder.itemView.context, R.color.soft_green))
            "yellow" -> holder.card.setCardBackgroundColor(ContextCompat.getColor(
                holder.itemView.context, R.color.soft_yellow))
            "red" -> holder.card.setCardBackgroundColor(ContextCompat.getColor(
                holder.itemView.context, R.color.soft_red))
        }

    }

    override fun getItemCount() = params.size
}