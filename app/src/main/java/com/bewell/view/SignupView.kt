package com.bewell.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bewell.databinding.ActivityLoginBinding
import com.bewell.databinding.ActivitySignupBinding
import com.bewell.presenter.LoginPresenter
import com.bewell.presenter.SignupPresenter

class SignupView : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var presenter: SignupPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        presenter = SignupPresenter()
        presenter.attachView(this)

        binding.signupButton.setOnClickListener {
            presenter.signup(binding.emailInput, binding.passwordInput, binding.repeatPasswordInput,
                this, binding.signupButton, binding.loginText, binding.signupProgress)
        }

        binding.loginText.setOnClickListener {
            presenter.login(this)
        }
    }
}