package com.example.ultimatetrolleyfitness

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class EmailPasswordViewModel : ViewModel() {
    private val _isSignInState = MutableLiveData(true) // Default value: sign-in state
    val isSignInState: LiveData<Boolean> get() = _isSignInState

    fun toggleState() {
        _isSignInState.value = !_isSignInState.value!!
    }
}
