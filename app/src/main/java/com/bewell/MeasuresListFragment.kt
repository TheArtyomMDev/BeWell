package com.bewell

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.bewell.adapters.MeasuresRecyclerAdapter
import com.bewell.data.Measure
import com.bewell.databinding.FragmentHomeBinding
import com.bewell.utils.Constants.TAG
import com.bewell.viewmodels.MeasuresListViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.Serializable
import java.util.*

class MeasuresListFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val vm by viewModel<MeasuresListViewModel>()
    private var started = false
    private val calendar: Calendar = Calendar.getInstance()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        // restoring if going back

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val context = this.context

        binding.measuresRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.measuresRecyclerView.adapter = MeasuresRecyclerAdapter(context!!, viewLifecycleOwner, vm.measuresLD, ::deleteMeasure)

        vm.chosenTimeLD.observe(viewLifecycleOwner) { time ->
            calendar.time = time
            binding.datePickerText.text =
                "${calendar[Calendar.DATE]}.${calendar[Calendar.MONTH] + 1}.${calendar[Calendar.YEAR]}"

            println("Time changed to $time")
            vm.getMeasuresFromDate(time)
        }

        vm.measuresLD.observe(viewLifecycleOwner) {
            println("got new measures")
            //println(it)
        }

        vm.isDataSynced.observe(viewLifecycleOwner) { isDataSynced ->
            val resource = when(isDataSynced) {
                true -> R.drawable.ic_synced
                false -> R.drawable.ic_not_synced
            }
            binding.isSyncedImage.setImageResource(resource)
        }

        binding.motionContainer.setTransitionListener(
            object: MotionLayout.TransitionListener {
                override fun onTransitionStarted(
                    motionLayout: MotionLayout?,
                    startId: Int,
                    endId: Int
                ) {}

                override fun onTransitionChange(
                    motionLayout: MotionLayout?,
                    startId: Int,
                    endId: Int,
                    progress: Float
                ) {
                    if(progress > 0.06f && !started) {
                        Log.d(TAG, "starting StartMeasureActivity")
                        activity?.startActivity(Intent(context, StartMeasureActivity::class.java))
                        activity?.overridePendingTransition(R.anim.add_measure_anim, R.anim.exit_when_add_anim)
                        //parentFragmentManager.beginTransaction().remove(activity).commitAllowingStateLoss()
                        started = true
                    }

                }

                override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                    if(currentId == R.id.end) {
                        // Return to original constraint set
                        binding.fab.imageAlpha = 100
                        binding.motionContainer.transitionToStart()
                    }
                }

                override fun onTransitionTrigger(
                    motionLayout: MotionLayout?,
                    triggerId: Int,
                    positive: Boolean,
                    progress: Float
                ) {}
            }
        )

        binding.fab.setOnClickListener {
            binding.fab.imageAlpha = 0
            binding.motionContainer.transitionToEnd()
        }

        binding.datePickerText.setOnClickListener {
            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build()
            val supportFragmentManager = childFragmentManager

            datePicker.addOnPositiveButtonClickListener {
                println("date picked")
                vm.chosenTimeLD.value = Date(it)
            }

            datePicker.show(supportFragmentManager, "tag")
        }

        return root
    }

    fun deleteMeasure(id: String) {
        vm.deleteMeasure(id)

        /*
        val array = vm.measuresLD.value!!.toMutableList()

        for (i in array.indices) {
            if(array[i].id == id) {
                println("dropped $i")
                array.removeAt(i)
                vm.measuresLD.value = array
                //println(vm.measuresLD.value)
                return
            }
        }

         */
    }

    override fun onStop() {
        super.onStop()
        started = false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("measures", vm.measuresLD.value as Serializable)
        outState.putSerializable("choosedTime", vm.chosenTimeLD.value)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if(savedInstanceState != null) {
            println("restored")
            vm.measuresLD.value = savedInstanceState.getSerializable("measures") as List<Measure>
            vm.chosenTimeLD.value = savedInstanceState.getSerializable("choosedTime") as Date
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}