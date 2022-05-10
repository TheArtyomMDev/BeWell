package com.bewell.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bewell.databinding.ActivityAskToGivePermissionsBinding
import com.bewell.presenter.GivePermissionsPresenter

class GivePermissionsView : AppCompatActivity() {

    private lateinit var binding: ActivityAskToGivePermissionsBinding
    private lateinit var presenter: GivePermissionsPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAskToGivePermissionsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        presenter = GivePermissionsPresenter()
        presenter.attachView(this)

        binding.givePermissionsButton.setOnClickListener {
            presenter.askPermissions(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        presenter.checkPermissions(requestCode, grantResults)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

}