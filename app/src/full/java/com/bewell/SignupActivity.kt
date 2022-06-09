package com.bewell

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.bewell.databinding.ActivitySignupBinding
import com.bewell.viewmodels.SignupViewModel
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private val vm by viewModel<SignupViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        vm.isSigned.observe(this) { isSigned ->
            if(isSigned) {
                hideProgressDialog()
                val intent = Intent(this, StartMeasureActivity::class.java)
                startActivity(intent)
                this.finish()
            }
        }

        vm.authError.observe(this) { error ->
            if(error.isNotEmpty()) {
                hideProgressDialog()
                Snackbar.make(
                    binding.root,
                    error,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

        vm.emailInputError.observe(this) { error ->
            if(error == null) binding.emailLayout.isErrorEnabled = false
            else {
                hideProgressDialog()
                binding.emailLayout.error = error
            }
        }

        vm.passwordInputError.observe(this) { error ->
            if(error == null) binding.passwordLayout.isErrorEnabled = false
            else {
                hideProgressDialog()
                binding.passwordLayout.error = error
            }
        }

        vm.repeatPasswordInputError.observe(this) { error ->
            if(error == null) binding.repeatPasswordLayout.isErrorEnabled = false
            else {
                hideProgressDialog()
                binding.repeatPasswordLayout.error = error
            }
        }


        binding.signupButton.setOnClickListener {
            showProgressDialog()
            val email = binding.emailLayout.editText!!.text.toString()
            val password = binding.passwordLayout.editText!!.text.toString()
            val repeatPassword = binding.repeatPasswordLayout.editText!!.text.toString()
            vm.signup(email, password, repeatPassword)
        }

        binding.loginText.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            this.finish()
        }
    }

    private fun showProgressDialog() {
        binding.signupButton.visibility = View.GONE
        binding.loginText.visibility = View.GONE
        binding.signupProgress.visibility = View.VISIBLE
    }

    private fun hideProgressDialog() {
        binding.signupButton.visibility = View.VISIBLE
        binding.loginText.visibility  = View.VISIBLE
        binding.signupProgress.visibility = View.GONE
    }
}