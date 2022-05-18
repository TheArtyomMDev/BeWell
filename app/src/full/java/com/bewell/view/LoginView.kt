package com.bewell.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bewell.databinding.ActivityLoginBinding
import com.bewell.presenter.LoginPresenter

class LoginView : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var presenter: LoginPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        presenter = LoginPresenter()
        presenter.attachView(this)

        presenter.setup(this)

        binding.loginButton.setOnClickListener {
            presenter.login(binding.emailLayout, binding.passwordLayout,
                this, binding.loginButton, binding.signupText, binding.loginProgress)
        }

        binding.signupText.setOnClickListener {
            presenter.signup(this)
        }
    }

}