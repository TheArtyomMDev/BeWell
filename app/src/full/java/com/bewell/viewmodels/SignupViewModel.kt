package com.bewell.viewmodels

import com.bewell.R
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.bewell.utils.Constants.TAG
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.*
class SignupViewModel(application: Application, private val auth: FirebaseAuth):
    AndroidViewModel(application) {
    var isSigned = MutableLiveData<Boolean>().apply { postValue(false) }
    var authError = MutableLiveData<String>().apply { postValue("") }
    var emailInputError = MutableLiveData<String>().apply { postValue("") }
    var passwordInputError = MutableLiveData<String>().apply { postValue("") }
    var repeatPasswordInputError = MutableLiveData<String>().apply { postValue("") }

    fun signup(email: String, password: String, repeatPassword: String) {
        Log.d(TAG, "Signup started")

        //проверка почты и пароля на корректность
        if (!validate(email, password, repeatPassword)) {
            return
        }

        //проверка по базе
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            Log.d(TAG, task.isSuccessful.toString())
            if (task.isSuccessful) {
                onSignupSuccess()
            } else {
                try {
                    throw task.exception!!
                } catch (e: FirebaseAuthUserCollisionException) {
                    authError.value = "This email is already used"
                } catch (e: FirebaseNetworkException) {
                    authError.value = "No internet connection"
                } catch (e: Exception) {
                    Log.e(TAG, task.exception.toString())
                    authError.value = "Unknown error"
                }
            }
        }
    }

    private fun onSignupSuccess() {
        isSigned.value = true
    }

    //проверка почты и пароля на корректность
    private fun validate(email: String, password: String, repeatPassword: String): Boolean {
        var valid = true

        //проверка на почту
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputError.value = getApplication<Application>().resources.getString(R.string.email_input_error)
            valid = false
        } else {
            emailInputError.value = null
        }

        //проверка пароля на длину
        if (password.isEmpty() || password.length < 6) {
            passwordInputError.value = getApplication<Application>().resources.getString(R.string.password_input_error)
            valid = false
        } else {
            passwordInputError.value = null
        }

        if (repeatPassword != password) {
            repeatPasswordInputError.value = getApplication<Application>().resources.getString(R.string.passwords_not_match_error)
            valid = false
        } else {
            repeatPasswordInputError.value = null
        }

        return valid
    }

}