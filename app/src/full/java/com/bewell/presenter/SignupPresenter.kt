package com.bewell.presenter

import com.bewell.R
import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bewell.base.MainContract
import com.bewell.utils.Constants
import com.bewell.utils.Constants.TAG
import com.bewell.view.LoginView
import com.bewell.view.SignupView
import com.bewell.view.StartMeasureView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignupPresenter: MainContract.Presenter<SignupView> {
    private var view: SignupView? = null
    private lateinit var auth: FirebaseAuth

    override fun attachView(signupView: SignupView) {
        view = signupView
    }

    override fun detachView() {
        view = null
    }


    fun signup(emailLayout: TextInputLayout, passwordLayout: TextInputLayout, repeatPasswordLayout: TextInputLayout,
               activity: Activity, signupButton: Button, loginText: TextView, signupProgress: ProgressBar) {
        Log.d(TAG, "Signup started")

        val email = emailLayout.editText!!.text.toString()
        val password = passwordLayout.editText!!.text.toString()

        //проверка почты и пароля на корректность
        if (!validate(emailLayout, passwordLayout, repeatPasswordLayout)) {
            return
        }

        //прогресс регистрации
        showProgressDialog(signupButton, loginText, signupProgress)

        val auth = Firebase.auth

        //проверка по базе
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            Log.d(TAG, task.isSuccessful.toString())
            hideProgressDialog(signupButton, loginText, signupProgress)
            if (task.isSuccessful) {
                onSignupSuccess(activity)
            } else {
                try {
                    throw task.exception!!
                } catch (e: FirebaseAuthUserCollisionException) {
                    onSignupFailed("This email is already used")
                } catch (e: FirebaseNetworkException) {
                    onSignupFailed("No internet connection")
                } catch (e: Exception) {
                    Log.e(TAG, task.exception.toString())
                    onSignupFailed("Unknown error")
                }
            }
        }
    }

    fun login(activity: Activity) {
        val intent = Intent(view!!.applicationContext, LoginView::class.java)
        ActivityCompat.startActivity(activity, intent, null)
        activity.finish()
    }

    fun showProgressDialog(signupButton: Button, loginText: TextView, signupProgress: ProgressBar) {
        signupButton.visibility = View.GONE
        loginText.visibility = View.GONE
        signupProgress.visibility = View.VISIBLE
    }

    fun hideProgressDialog(signupButton: Button, loginText: TextView, signupProgress: ProgressBar) {
        signupButton.visibility = View.VISIBLE
        loginText.visibility = View.VISIBLE
        signupProgress.visibility = View.GONE
    }

    fun onSignupSuccess(activity: Activity) {
        val intent = Intent(view!!.applicationContext, StartMeasureView::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        ContextCompat.startActivity(view!!.applicationContext, intent, null)

        activity.setResult(Activity.RESULT_OK, null)
        activity.finish()
    }

    fun onSignupFailed(error: String) {
        val myAwesomeSnackbar = Snackbar.make(
            view!!.findViewById(R.id.signup_button),
            error,
            Snackbar.LENGTH_SHORT
        );
        myAwesomeSnackbar.show()
    }

    //проверка почты и пароля на корректность
    fun validate(emailLayout: TextInputLayout, passwordLayout: TextInputLayout, repeatPasswordLayout: TextInputLayout): Boolean {
        var valid = true

        val email = emailLayout.editText!!.text.toString()
        val password = passwordLayout.editText!!.text.toString()
        val reEnterPassword = repeatPasswordLayout.editText!!.text.toString()

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = view!!.resources.getString(R.string.email_input_error)
            valid = false
        } else {
            emailLayout.error = null
            emailLayout.isErrorEnabled = false
        }

        if (password.isEmpty() || password.length < 6) {
            passwordLayout.error = view!!.resources.getString(R.string.password_input_error)
            valid = false
        } else {
            passwordLayout.error = null
            passwordLayout.isErrorEnabled = false
        }

        if (reEnterPassword != password) {
            repeatPasswordLayout.error = view!!.resources.getString(R.string.passwords_not_match_error)
            valid = false
        } else {
            repeatPasswordLayout.error = null
            repeatPasswordLayout.isErrorEnabled = false
        }

        return valid
    }

}