package com.bewell

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bewell.databinding.ActivityStartBinding
import com.bewell.viewmodels.StartMeasureViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class StartMeasureActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding
    private val vm by viewModel<StartMeasureViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        GlobalScope.launch(Dispatchers.Main) {
            vm.requestPermissions(listOf(Manifest.permission.CAMERA))
        }

        vm.permissionsRequestResult.observe(this) { isSuccessful ->
            if(!isSuccessful) {
                val intent = Intent(this, AskToGivePermissionsActivity::class.java)
                startActivity(intent)
                this.finish()
            }
        }

        binding.timesRadioGroup.setOnCheckedChangeListener{ _, checkedId ->
            binding.confirmButton.isEnabled = true
            val order = binding.timesRadioGroup.indexOfChild(
                binding.timesRadioGroup.findViewById(checkedId))
            vm.onTimeChosen(order)
        }

        binding.confirmButton.setOnClickListener {
            val intent = Intent(this, HRVMeasureActivity::class.java)
            startActivity(intent)
            this.finish()
        }

        binding.cancelButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            this.finish()
        }

    }
}

