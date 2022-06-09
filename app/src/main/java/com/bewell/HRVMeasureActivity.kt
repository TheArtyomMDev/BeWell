package com.bewell

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.PowerManager
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.bewell.databinding.MainBinding
import com.bewell.utils.Constants
import com.bewell.viewmodels.HRVMeasureViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.context.GlobalContext.get


class HRVMeasureActivity : AppCompatActivity() {

    private lateinit var binding: MainBinding
    private val vm by viewModel<HRVMeasureViewModel>()
    private lateinit var wakeLock: PowerManager.WakeLock

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val mIntent = Intent(this, MeasureResultActivity::class.java)

        vm.previewHolder = binding.preview.holder
        vm.intent = mIntent

        vm.bpmText.observe(this) { text ->
            binding.bpm.text = text
        }
        vm.isMeasureFinished.observe(this) { isMeasureFinished ->
            if(isMeasureFinished) {
                startActivity(mIntent)
                this.finish()
            }
        }

        val pm = getSystemService(Activity.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, Constants.WAKELOCK_TAG)
        binding.preview.holder.addCallback(vm.surfaceCallback)
    }

    public override fun onResume() {
        super.onResume()
        wakeLock.acquire(Constants.WAKELOCK_TIME * 1000L)
        vm.onResume()
    }

    public override fun onPause() {
        super.onPause()
        wakeLock.release()
        vm.onPause()
    }
}