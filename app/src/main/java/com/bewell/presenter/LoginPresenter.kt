package com.bewell.presenter

import android.R
import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import com.bewell.base.MainContract
import com.bewell.utils.Constants.REQUEST_SIGNUP
import com.bewell.utils.Constants.TAG
import com.bewell.view.LoginView
import com.bewell.view.SignupView
import com.bewell.view.StartMeasureView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class LoginPresenter: MainContract.Presenter<LoginView> {
    private var view: LoginView? = null
    private lateinit var auth: FirebaseAuth

    override fun attachView(loginView: LoginView) {
        view = loginView
    }

    override fun detachView() {
        view = null
    }

    fun setup(activity: Activity) {
        auth = Firebase.auth
        val currentUser = auth.currentUser

        if(currentUser != null) {
            onLoginSuccess(activity)
        }
    }

    fun login(emailInput: EditText, passwordInput: EditText, activity: Activity,
              loginButton: Button, signupText: TextView, loginProgress: ProgressBar) {
        Log.d(TAG, "Login")

        if (!validate(emailInput, passwordInput)) {
            return
        }

        //прогресс логина
        showProgressDialog(loginButton, signupText, loginProgress)

        val email = emailInput.text.toString()
        val password = passwordInput.text.toString()

        //проверка по базе
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            Log.d(TAG, task.isSuccessful.toString())
            hideProgressDialog(loginButton, signupText, loginProgress)
            if (task.isSuccessful) {
                onLoginSuccess(activity)
            } else {
                try {
                    throw task.exception!!
                } catch (e: FirebaseAuthInvalidUserException) {
                    onLoginFailed("No user found")
                } catch (e: FirebaseNetworkException) {
                    onLoginFailed("No internet connection")
                } catch (e: FirebaseAuthInvalidCredentialsException) {
                    onLoginFailed("Wrong password")
                } catch (e: Exception) {
                    Log.e(TAG, task.exception.toString())
                    onLoginFailed("Unknown error")
                }

            }
        }

    }

    fun signup(activity: Activity) {
        val intent = Intent(view!!.applicationContext, SignupView::class.java)
        startActivityForResult(activity, intent, REQUEST_SIGNUP, null)
        activity.finish()
    }

    fun showProgressDialog(loginButton: Button, signupText: TextView, loginProgress: ProgressBar) {
        loginButton.visibility = View.GONE
        signupText.visibility = View.GONE
        loginProgress.visibility = View.VISIBLE
    }

    fun hideProgressDialog(loginButton: Button, signupText: TextView, loginProgress: ProgressBar) {
        loginButton.visibility = View.VISIBLE
        signupText.visibility = View.VISIBLE
        loginProgress.visibility = View.GONE
    }

    fun onLoginSuccess(activity: Activity) {
        val intent = Intent(view!!.applicationContext, StartMeasureView::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        ContextCompat.startActivity(view!!.applicationContext, intent, null)
        activity.finish()
    }

    fun onLoginFailed(error: String) {
        val myAwesomeSnackbar = Snackbar.make(
            view!!.findViewById(R.id.content),
        error,
            Snackbar.LENGTH_SHORT
        );
        myAwesomeSnackbar.show()

    }

    fun validate(emailInput: EditText, passwordInput: EditText): Boolean {
        var valid = true
        var tried = false

        val email = emailInput.text.toString()
        val password = passwordInput.text.toString()

        //проверка на почту
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.error = view!!.resources.getString(com.bewell.R.string.email_input_error)
            valid = false
        } else {
            emailInput.error = null
        }

        //проверка пароля на длину
        if (password.isEmpty() || password.length < 6) {
            passwordInput.error = view!!.resources.getString(com.bewell.R.string.password_input_error)
            valid = false
        } else {
            passwordInput.error = null
        }

        return valid
    }

}