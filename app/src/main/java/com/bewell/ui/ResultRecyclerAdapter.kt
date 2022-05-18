package com.bewell.ui

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.cardview.widget.CardView
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bewell.R
import com.bewell.utils.Constants.TAG
import com.bewell.utils.blendColors
import com.bewell.utils.dp
import com.bewell.utils.getValueAnimator
import com.bewell.utils.screenWidth
import com.google.android.material.card.MaterialCardView


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

    var  expandedHeights = MutableList(20) {300}

    var animationPlaybackSpeed: Double = 0.8

    // filteredItems is a static field to simulate filtering of random items
    private val filteredItems = intArrayOf(2, 5, 6, 8, 12)
    private val modelList = List(20) { MainListModel(it) }
    private val adapterList: List<MainListModel> get() = modelList

    /** Variable used to filter adapter items. 'true' if filtered and 'false' if not */


    private val listItemExpandDuration: Long get() = (300L / animationPlaybackSpeed).toLong()
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private lateinit var recyclerView: RecyclerView
    private var expandedModel: MainListModel? = null
    private var isScaledDown = false

    val params = mutableListOf<Array<String>>()


    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    init {
        context.theme.resolveAttribute(com.google.android.material.R.attr.backgroundColor, originalBg, false)
        context.theme.resolveAttribute(com.google.android.material.R.attr.colorSecondaryContainer, expandedBg, true)
    }

    fun addData(intent: Intent) {
        Log.d(TAG, "data adding started")

        val bundle = intent.extras
        if (bundle != null) {
            for (key in bundle.keySet()) {
                val list = intent.getStringArrayListExtra(key)
                params.add(arrayOf(key, list!![0], list[1], list[2], list[3]))
            }
        }

        Log.d(TAG, "data adding finished")
    }

    override fun getItemCount(): Int = params.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.recyclerview_item, parent, false)
        return ListViewHolder(itemView)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val model = adapterList[position]
        val param = params[position]

        println(position)

        holder.textViewLarge.text = param[0]
        holder.textViewMiddle.text = param[1]
        holder.textViewSmall.text = param[2]
        holder.descriptionText.text = param[4]

        val color = when (param[3]) {
            "green" -> R.color.soft_green
            "yellow" -> R.color.soft_yellow
            "red" -> R.color.soft_red
            else -> throw Exception("Unknown color of text on Card")
        }

        holder.textViewMiddle.background.setTint(ContextCompat.getColor(context, color))

        //expandItem(holder, model == expandedModel, animate = false)
        //scaleDownItem(holder, position, isScaledDown)


        holder.cardContainer.setOnClickListener {
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
                holder.expandView.isVisible = true
            }
            else animator.doOnEnd {
                holder.expandView.isVisible = false
            }

            animator.start()
        } else {

            // show expandView only if we have expandedHeight (onViewAttached)
            holder.expandView.isVisible = expand && expandedHeights[holder.layoutPosition] >= 0
            setExpandProgress(holder, if (expand) 1f else 0f)
        }
    }

    override fun onViewAttachedToWindow(holder: ListViewHolder) {
        super.onViewAttachedToWindow(holder)

        //val pos = holder.layoutPosition

        /*
        // get originalHeight & expandedHeight if not gotten before
        if (expandedHeight < 0) {
            //Log.d(TAG, "View at layout position $pos attached")
            expandedHeight = 0 // so that this block is only called once


            holder.cardContainer.doOnLayout { view ->
                originalHeight = view.height

                // show expandView and record expandedHeight in next layout pass
                // (doOnPreDraw) and hide it immediately. We use onPreDraw because
                // it's called after layout is done. doOnNextLayout is called during
                // layout phase which causes issues with hiding expandView.
                holder.expandView.isVisible = true
                view.doOnPreDraw {
                    expandedHeight = view.height
                    holder.expandView.isVisible = false
                }
            }
        }


         */

        //Log.d(TAG, "View at layout position $pos attached")


        holder.cardContainer.doOnLayout { view ->
            if(originalHeight < 0) originalHeight = view.height

            // show expandView and record expandedHeight in next layout pass
            // (doOnPreDraw) and hide it immediately. We use onPreDraw because
            // it's called after layout is done. doOnNextLayout is called during
            // layout phase which causes issues with hiding expandView.
            holder.expandView.isVisible = true
            view.doOnPreDraw {
                if (expandedHeights[holder.layoutPosition] == 300)
                    expandedHeights[holder.layoutPosition]  = view.height

                println("expanded view ${holder.layoutPosition} height is ${view.height}")
                holder.expandView.isVisible = false
                expandItem(holder, adapterList[holder.layoutPosition] == expandedModel, animate = false)
                scaleDownItem(holder, holder.layoutPosition, isScaledDown)
            }
        }
    }

    private fun setExpandProgress(holder: ListViewHolder, progress: Float) {
        if (expandedHeights[holder.layoutPosition] > 0 && originalHeight > 0) {
            holder.cardContainer.layoutParams.height =
                (originalHeight + (expandedHeights[holder.layoutPosition] - originalHeight) * progress).toInt()
        }
        holder.cardContainer.layoutParams.width =
            (originalWidth + (expandedWidth - originalWidth) * progress).toInt()

        holder.cardContainer.setBackgroundColor(blendColors(originalBg.data, expandedBg.data, progress))
        holder.cardContainer.requestLayout()

        holder.chevron.rotation = 90 * progress
    }

    ///////////////////////////////////////////////////////////////////////////
    // Scale Down Animation
    ///////////////////////////////////////////////////////////////////////////

    private inline val LinearLayoutManager.visibleItemsRange: IntRange
        get() = findFirstVisibleItemPosition()..findLastVisibleItemPosition()

    fun setScaleDownAnimator(isScaledDown: Boolean): ValueAnimator {
        val lm = recyclerView.layoutManager as LinearLayoutManager

        val animator = getValueAnimator(isScaledDown,
            duration = 300L, interpolator = AccelerateDecelerateInterpolator()
        ) { progress ->

            // Get viewHolder for all visible items and animate attributes
            for (i in lm.visibleItemsRange) {
                val holder = recyclerView.findViewHolderForLayoutPosition(i) as ListViewHolder
                setScaleDownProgress(holder, i, progress)
            }
        }

        // Set adapter variable when animation starts so that newly binded views in
        // onBindViewHolder will respect the new size when they come into the screen
        animator.doOnStart { this.isScaledDown = isScaledDown }

        // For all the non visible items in the layout manager, notify them to adjust the
        // view to the new size
        animator.doOnEnd {
            repeat(lm.itemCount) { if (it !in lm.visibleItemsRange) notifyItemChanged(it) }
        }
        return animator
    }

    private fun setScaleDownProgress(holder: ListViewHolder, position: Int, progress: Float) {
        val itemExpanded = position >= 0 && adapterList[position] == expandedModel
        holder.cardContainer.layoutParams.apply {
            width = ((if (itemExpanded) expandedWidth else originalWidth) * (1 - 0.1f * progress)).toInt()
            height = ((if (itemExpanded) expandedHeights[holder.layoutPosition] else originalHeight) * (1 - 0.1f * progress)).toInt()
//            log("width=$width, height=$height [${"%.2f".format(progress)}]")
        }
        holder.cardContainer.requestLayout()

        holder.scaleContainer.scaleX = 1 - 0.05f * progress
        holder.scaleContainer.scaleY = 1 - 0.05f * progress

        /*
        holder.scaleContainer.setPadding(
            (listItemHorizontalPadding * (1 - 0.2f * progress)).toInt(),
            (listItemVerticalPadding * (1 - 0.2f * progress)).toInt(),
            (listItemHorizontalPadding * (1 - 0.2f * progress)).toInt(),
            (listItemVerticalPadding * (1 - 0.2f * progress)).toInt()
        )

         */

        holder.listItemFg.alpha = progress
    }

    /** Convenience method for calling from onBindViewHolder */
    private fun scaleDownItem(holder: ListViewHolder, position: Int, isScaleDown: Boolean) {
        setScaleDownProgress(holder, position, if (isScaleDown) 1f else 0f)
    }

    ///////////////////////////////////////////////////////////////////////////
    // ViewHolder
    ///////////////////////////////////////////////////////////////////////////

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val expandView: View = itemView.findViewById(R.id.expand_view)
        val chevron: View = itemView.findViewById(R.id.arrow)
        val cardContainer: View = itemView.findViewById(R.id.card_container)
        val scaleContainer: View = itemView.findViewById(R.id.scale_container)
        val listItemFg: View = itemView.findViewById(R.id.list_item_fg)
        val card: MaterialCardView = itemView.findViewById(R.id.card)

        val textViewLarge: TextView = itemView.findViewById(R.id.textViewLarge)
        val textViewMiddle: TextView = itemView.findViewById(R.id.textViewMiddle)
        val textViewSmall: TextView = itemView.findViewById(R.id.textViewSmall)
        val descriptionText: TextView = itemView.findViewById(R.id.description_text)
    }
}