package com.bewell

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bewell.databinding.ActivityAskToGivePermissionsBinding
import com.bewell.utils.Constants
import com.bewell.viewmodels.AskToGivePermissionsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class AskToGivePermissionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAskToGivePermissionsBinding
    private val vm by viewModel<AskToGivePermissionsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAskToGivePermissionsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        vm.requestResult.observe(this) { requestResult ->
            if(requestResult) {
                val intent = Intent(this, StartMeasureActivity::class.java)
                startActivity(intent)
                this.finish()
            }
        }

        binding.givePermissionsButton.setOnClickListener {
            Log.d(Constants.TAG, "Asking for permissions")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                Constants.REQUEST_CODE_CAMERA
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        vm.checkPermissions(requestCode, grantResults)
    }


}