package com.bewell.ui

import android.content.Context
import android.content.Intent
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.doOnLayout
import androidx.core.view.doOnNextLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bewell.R
import com.bewell.databinding.RecyclerviewItemBinding
import com.bewell.model.HRVParam
import com.bewell.utils.Constants.TAG
import com.bewell.utils.blendColors
import com.bewell.utils.dp
import com.bewell.utils.getValueAnimator
import com.bewell.utils.screenWidth


class ResultRecyclerAdapter(val context: Context) : RecyclerView.Adapter<ResultRecyclerAdapter.ListViewHolder>() {

    /** List Model. A sample model that only contains id */
    data class MainListModel(val id: Int)

    private val originalBg = TypedValue()
    private val expandedBg = TypedValue()

    //private val originalBg: Int = ContextCompat.getColor(context, R.color.background)
    //private val expandedBg: Int = ContextCompat.getColor(context, R.color.list_item_bg_expanded)

    private val listItemHorizontalPadding: Float = context.resources.getDimension(R.dimen.list_item_horizontal_padding)
    private val listItemVerticalPadding: Float = context.resources.getDimension(R.dimen.list_item_vertical_padding)
    private val originalWidth = context.screenWidth - 48.dp
    private val expandedWidth = context.screenWidth - 24.dp
    private var originalHeight = -1 // will be calculated dynamically
    private var expandedHeight = -1 // will be calculated dynamically

    var  expandedHeights = MutableList(20) {400}

    var animationPlaybackSpeed: Double = 0.8

    // filteredItems is a static field to simulate filtering of random items
    private val filteredItems = intArrayOf(2, 5, 6, 8, 12)
    private val modelList = List(20) { MainListModel(it) }
    private val adapterList: List<MainListModel> get() = modelList

    private val listItemExpandDuration: Long get() = (300L / animationPlaybackSpeed).toLong()
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private lateinit var recyclerView: RecyclerView
    private var expandedModel: MainListModel? = null
    private var isScaledDown = false

    val params = mutableListOf<HRVParam>()


    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    init {
        context.theme.resolveAttribute(com.google.android.material.R.attr.backgroundColor, originalBg, false)
        context.theme.resolveAttribute(com.google.android.material.R.attr.colorSecondaryContainer, expandedBg, true)
    }

    fun addData(intent: Intent) {
        Log.d(TAG, "data adding started")

        val bundle = intent.extras!!
        for (key in bundle.keySet()) {
            params.add(intent.getSerializableExtra(key) as HRVParam)
        }

        Log.d(TAG, "data adding finished")
    }

    override fun getItemCount(): Int = params.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = RecyclerviewItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val model = adapterList[position]
        val param = params[position]

        holder.binding.param = param

        //println("pos: $position ${holder.binding.descriptionText.text}")


        //expandItem(holder, model == expandedModel, animate = false)
        //scaleDownItem(holder, position, isScaledDown)


        holder.binding.cardContainer.setOnClickListener {
            println(expandedHeights[position])

            //println(expandedHeights[holder.layoutPosition])

            //holder.expandView.isVisible = true

            if (expandedModel == null) {

                // expand clicked view
                expandItem(holder, expand = true, animate = true)
                expandedModel = model
            } else if (expandedModel == model) {

                // collapse clicked view
                expandItem(holder, expand = false, animate = true)
                expandedModel = null
            } else {

                //если нужно не collapse-ить предыдущую при открытии следущей, то нужно завести
                // массив expandedModel

                // collapse previously expanded view
                val expandedModelPosition = adapterList.indexOf(expandedModel!!)
                val oldViewHolder =
                    recyclerView.findViewHolderForAdapterPosition(expandedModelPosition) as? ListViewHolder
                if (oldViewHolder != null) expandItem(oldViewHolder, expand = false, animate = true)


                // expand clicked view
                expandItem(holder, expand = true, animate = true)
                expandedModel = model
            }


        }
    }

    private fun expandItem(holder: ListViewHolder, expand: Boolean, animate: Boolean) {
        if (animate) {
            val animator = getValueAnimator(
                expand, listItemExpandDuration, AccelerateDecelerateInterpolator()
            ) { progress -> setExpandProgress(holder, progress) }

            if (expand) animator.doOnStart {
                holder.binding.expandView.isVisible = true
            }
            else animator.doOnEnd {
                holder.binding.expandView.isVisible = false
            }

            animator.start()
        } else {

            // show expandView only if we have expandedHeight (onViewAttached)
            holder.binding.expandView.isVisible = expand && expandedHeights[holder.layoutPosition] >= 0
            setExpandProgress(holder, if (expand) 1f else 0f)
        }
    }

    override fun onViewAttachedToWindow(holder: ListViewHolder) {
        super.onViewAttachedToWindow(holder)


        println(params[holder.layoutPosition].description)
        holder.binding.descriptionText.text = params[holder.layoutPosition].description
        println("text on ${holder.layoutPosition} ${holder.binding.descriptionText.text}")

        holder.binding.cardContainer.doOnLayout { view ->
            if(originalHeight < 0) originalHeight = view.height

            // show expandView and record expandedHeight in next layout pass
            // (doOnPreDraw) and hide it immediately. We use onPreDraw because
            // it's called after layout is done. doOnNextLayout is called during
            // layout phase which causes issues with hiding expandView.


            holder.binding.expandView.isVisible = true
            view.doOnPreDraw {
                if (view.height > expandedHeights[holder.layoutPosition])
                    expandedHeights[holder.layoutPosition] = view.height

                println("expanded height on pos ${holder.layoutPosition} is ${view.height}")

                holder.binding.expandView.isVisible = false
                expandItem(holder, adapterList[holder.layoutPosition] == expandedModel, animate = false)
                scaleDownItem(holder, holder.layoutPosition, isScaledDown)
            }
        }
    }

    private fun setExpandProgress(holder: ListViewHolder, progress: Float) {
        if (expandedHeights[holder.layoutPosition] > 0 && originalHeight > 0) {
            holder.binding.cardContainer.layoutParams.height =
                (originalHeight + (expandedHeights[holder.layoutPosition] - originalHeight) * progress).toInt()
        }
        holder.binding.cardContainer.layoutParams.width =
            (originalWidth + (expandedWidth - originalWidth) * progress).toInt()

        holder.binding.cardContainer.setBackgroundColor(blendColors(originalBg.data, expandedBg.data, progress))
        holder.binding.cardContainer.requestLayout()

        holder.binding.arrow.rotation = 90 * progress
    }

    ///////////////////////////////////////////////////////////////////////////
    // Scale Down Animation
    ///////////////////////////////////////////////////////////////////////////


    private fun setScaleDownProgress(holder: ListViewHolder, position: Int, progress: Float) {
        val itemExpanded = position >= 0 && adapterList[position] == expandedModel
        holder.binding.cardContainer.layoutParams.apply {
            width = ((if (itemExpanded) expandedWidth else originalWidth) * (1 - 0.1f * progress)).toInt()
            height = ((if (itemExpanded) expandedHeights[holder.layoutPosition] else originalHeight) * (1 - 0.1f * progress)).toInt()
//            log("width=$width, height=$height [${"%.2f".format(progress)}]")
        }
        holder.binding.cardContainer.requestLayout()

        holder.binding.scaleContainer.scaleX = 1 - 0.05f * progress
        holder.binding.scaleContainer.scaleY = 1 - 0.05f * progress

        /*
        holder.scaleContainer.setPadding(
            (listItemHorizontalPadding * (1 - 0.2f * progress)).toInt(),
            (listItemVerticalPadding * (1 - 0.2f * progress)).toInt(),
            (listItemHorizontalPadding * (1 - 0.2f * progress)).toInt(),
            (listItemVerticalPadding * (1 - 0.2f * progress)).toInt()
        )

         */

        holder.binding.listItemFg.alpha = progress
    }

    /** Convenience method for calling from onBindViewHolder */
    private fun scaleDownItem(holder: ListViewHolder, position: Int, isScaleDown: Boolean) {
        setScaleDownProgress(holder, position, if (isScaleDown) 1f else 0f)
    }

    ///////////////////////////////////////////////////////////////////////////
    // ViewHolder
    ///////////////////////////////////////////////////////////////////////////

    class ListViewHolder(val binding: RecyclerviewItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }
}