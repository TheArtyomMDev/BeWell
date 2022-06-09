package com.bewell

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bewell.databinding.FragmentHomeBinding
import com.bewell.data.Measure
import com.bewell.adapters.MeasuresRecyclerAdapter
import com.bewell.viewmodels.MeasuresListViewModel
import com.google.firebase.ktx.Firebase

class MeasuresListFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    var started = false

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val measuresListViewModel =
            ViewModelProvider(this).get(MeasuresListViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val context = this.context

        binding.recyclerView.layoutManager = LinearLayoutManager(this.context)
        binding.recyclerView.adapter = MeasuresRecyclerAdapter(listOf(getMeasure()))

        /*
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
         */

        binding.motionContainer.setTransitionListener(
            object: MotionLayout.TransitionListener {
                override fun onTransitionStarted(
                    motionLayout: MotionLayout?,
                    startId: Int,
                    endId: Int,
                ) {

                }
                // More code here


                override fun onTransitionChange(
                    motionLayout: MotionLayout?,
                    startId: Int,
                    endId: Int,
                    progress: Float,
                ) {
                    if(progress > 0.38f && !started) {
                        activity?.startActivity(Intent(context, StartMeasureActivity::class.java))
                        activity?.overridePendingTransition(R.anim.add_measure_anim, R.anim.exit_when_add_anim)
                        //parentFragmentManager.beginTransaction().remove(activity).commitAllowingStateLoss()
                        started = true
                    }

                }

                override fun onTransitionCompleted(motionLayout: MotionLayout?,
                                                   currentId: Int) {
                    if(currentId == R.id.end) {
                        // Return to original constraint set
                        binding.motionContainer.transitionToStart()

                    }
                }

                override fun onTransitionTrigger(
                    motionLayout: MotionLayout?,
                    triggerId: Int,
                    positive: Boolean,
                    progress: Float,
                ) {

                }
            }
        )

        binding.fab.setOnClickListener {
            binding.fab.imageAlpha = 0
            binding.motionContainer.transitionToEnd()
            //startActivity(Intent(this.context, StartMeasureView::class.java))
        }

        return root
    }

    private fun getMeasure(): Measure {
        return Measure(
            200.0,
            200.0,
            0.0,
            200.0,
            200.0,
            200.0,
            200.0
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}