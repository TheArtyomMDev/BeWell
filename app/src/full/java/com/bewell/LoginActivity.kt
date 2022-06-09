package com.bewell

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.bewell.databinding.ActivityLoginBinding
import com.bewell.viewmodels.LoginViewModel
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val vm by viewModel<LoginViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        vm.isLogged.observe(this) { isLogged ->
            if(isLogged) {
                hideProgressDialog()
                val intent = Intent(this, MainActivity::class.java)
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

        binding.loginButton.setOnClickListener {
            showProgressDialog()
            val email = binding.emailLayout.editText!!.text.toString()
            val password = binding.passwordLayout.editText!!.text.toString()

            vm.login(email, password)
        }


        binding.signupText.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            this.finish()
        }
    }

    private fun showProgressDialog() {
        binding.loginButton.visibility = View.GONE
        binding.signupText.visibility = View.GONE
        binding.loginProgress.visibility = View.VISIBLE
    }

    private fun hideProgressDialog() {
        binding.loginButton.visibility = View.VISIBLE
        binding.signupText.visibility  = View.VISIBLE
        binding.loginProgress.visibility = View.GONE
    }

}