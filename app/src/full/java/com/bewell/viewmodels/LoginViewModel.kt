package com.bewell.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.bewell.utils.Constants.TAG
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.*

class LoginViewModel(application: Application, private val auth: FirebaseAuth):
    AndroidViewModel(application) {
    var isLogged = MutableLiveData<Boolean>().apply { postValue(false) }
    var authError = MutableLiveData<String>().apply { postValue("") }
    var emailInputError = MutableLiveData<String>().apply { postValue("") }
    var passwordInputError = MutableLiveData<String>().apply { postValue("") }

    init {
        val currentUser = auth.currentUser

        if(currentUser != null) {
            Log.d(TAG, "Logged as ${currentUser.email}")
            onLoginSuccess()
        }
    }

    fun login(email: String, password: String) {
        Log.d(TAG, "Login")

        if (!validate(email, password)) {
            return
        }

        //проверка по базе
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            Log.d(TAG, task.isSuccessful.toString())
            if (task.isSuccessful) {
                onLoginSuccess()
            } else {
                try {
                    throw task.exception!!
                } catch (e: FirebaseAuthInvalidUserException) {
                    authError.value = "No user found"
                } catch (e: FirebaseNetworkException) {
                    authError.value = "No internet connection"
                } catch (e: FirebaseAuthInvalidCredentialsException) {
                    authError.value = "Wrong password"
                } catch (e: Exception) {
                    Log.e(TAG, task.exception.toString())
                    authError.value = "Unknown error"
                }

            }
        }

    }

    private fun onLoginSuccess() {
        isLogged.value = true
    }

    private fun validate(email: String, password: String): Boolean {
        var valid = true

        //проверка на почту
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputError.value = getApplication<Application>().resources.getString(com.bewell.R.string.email_input_error)
            valid = false
        } else {
            emailInputError.value = null
        }

        //проверка пароля на длину
        if (password.isEmpty() || password.length < 6) {
            passwordInputError.value = getApplication<Application>().resources.getString(com.bewell.R.string.password_input_error)
            valid = false
        } else {
            passwordInputError.value = null
        }

        return valid
    }

}