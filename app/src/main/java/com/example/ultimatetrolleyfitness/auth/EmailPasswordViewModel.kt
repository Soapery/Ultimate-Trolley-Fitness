package com.example.ultimatetrolleyfitness.auth

import android.annotation.SuppressLint
import android.app.Application
import android.text.TextUtils.isEmpty
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class EmailPasswordViewModelFactory(
    private val activity: EmailPasswordActivity,
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmailPasswordViewModel::class.java)) {
            return EmailPasswordViewModel(activity, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@SuppressLint("StaticFieldLeak")
class EmailPasswordViewModel(
    private val activity: EmailPasswordActivity,
    private val application: Application
) : ViewModel() {
    lateinit var auth: FirebaseAuth

    private val _isSignInState = MutableLiveData(true)
    val isSignInState: LiveData<Boolean> get() = _isSignInState

    private val _isPasswordState = MutableLiveData(false)
    val isPasswordState: LiveData<Boolean> get() = _isPasswordState

    private val _isConfirmPasswordState = MutableLiveData(false)
    val isConfirmPasswordState: LiveData<Boolean> get() = _isConfirmPasswordState

    private var _email = MutableLiveData("")
    var email: LiveData<String> = _email

    private var _password = MutableLiveData("")
    var password: LiveData<String> = _password

    private var _confirmPassword = MutableLiveData("")
    var confirmPassword: LiveData<String> = _confirmPassword

    private val database = Firebase.database
    val myRef = database.getReference("users")


    fun toggleAuthState() {
        _isSignInState.value = !_isSignInState.value!!
    }

    fun togglePasswordState() {
        _isPasswordState.value = !_isPasswordState.value!!
    }

    fun toggleConfirmPasswordState() {
        _isConfirmPasswordState.value = !_isConfirmPasswordState.value!!
    }

    fun onEmailChanged(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChanged(newPassword: String) {
        _password.value = newPassword
    }

    fun onConfirmPasswordChanged(newConfirmPassword: String) {
        _confirmPassword.value = newConfirmPassword
    }

    fun createAccount(email: String, password: String, confirmPassword: String) {
        val isEmailValid = isValidEmail(email)
        val doesPasswordMatch = password == confirmPassword

        if (
            isEmailValid &&
            doesPasswordMatch &&
            !isEmpty(email) &&
            !isEmpty(password) &&
            !isEmpty(confirmPassword)
        ) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val user = authResult.user
                    if (user != null) {
                        myRef.setValue(user.email)
                            .addOnCompleteListener { databaseTask ->
                                if (databaseTask.isSuccessful) {
                                    // Database operation successful
                                    Log.d(TAG, "User added to database")
                                    activity.updateUI()
                                } else {
                                    // Database operation failed
                                    Log.e(
                                        TAG,
                                        "Error adding user to database",
                                        databaseTask.exception
                                    )
                                    Toast.makeText(
                                        activity.baseContext,
                                        "Error adding user to database",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    } else {
                        // User is null
                        Log.e(TAG, "User is null")
                        Toast.makeText(
                            activity.baseContext,
                            "User is null",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .addOnFailureListener { e ->
                    // User creation failed
                    Log.e(TAG, "createUserWithEmail:failure", e)
                    Toast.makeText(
                        activity.baseContext,
                        "Account creation failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            if (!isEmpty(password) || !isEmpty(confirmPassword)) {
                Toast.makeText(
                    activity.baseContext,
                    "Password fields must not be blank!",
                    Toast.LENGTH_SHORT,
                ).show()
            } else if (!doesPasswordMatch) {
                Toast.makeText(
                    activity.baseContext,
                    "Passwords do not match!",
                    Toast.LENGTH_SHORT,
                ).show()
            }
            if (isEmpty(email)) {
                Toast.makeText(
                    activity.baseContext,
                    "Email must not be blank!",
                    Toast.LENGTH_SHORT,
                ).show()
            } else if (!isEmailValid) {
                Toast.makeText(
                    activity.baseContext,
                    "Invalid email! Please try again.",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    fun signIn(email: String, password: String) {
        val isEmailValid = isValidEmail(email)

        if (isEmailValid && !isEmpty(email) && !isEmpty(password)) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "signInWithEmail:success")
                        val user = auth.currentUser
                        activity.updateUI()
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            activity.baseContext,
                            "Login failed.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
        } else {
            if (isEmpty(email)) {
                Toast.makeText(
                    activity.baseContext,
                    "Email must not be blank!",
                    Toast.LENGTH_SHORT,
                ).show()
            } else if (!isEmailValid) {
                Toast.makeText(
                    activity.baseContext,
                    "Invalid email! Please try again.",
                    Toast.LENGTH_SHORT,
                ).show()
            }

            if (isEmpty(password)) {
                Toast.makeText(
                    activity.baseContext,
                    "Password must not be blank!",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    fun sendEmailVerification() {
        val user = auth.currentUser!!
        user.sendEmailVerification()
            .addOnCompleteListener(activity) { task ->
                // Email Verification sent
            }
    }

    fun reload() {

    }

    private fun isValidEmail(target: CharSequence?): Boolean {
        return if (isEmpty(target)) {
            false
        } else {
            Patterns.EMAIL_ADDRESS.matcher(target).matches()
        }
    }

    companion object {
        private const val TAG = "EmailPassword"
    }

}
