package com.univalle.picobotella.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.univalle.picobotella.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    // LiveData para el Login
    private val _loginStatus = MutableLiveData<Boolean?>()
    val loginStatus: LiveData<Boolean?> = _loginStatus

    // LiveData para el Registro
    private val _registerStatus = MutableLiveData<Boolean?>()
    val registerStatus: LiveData<Boolean?> = _registerStatus

    fun login(email: String, pass: String) {
        repository.login(email, pass) { success ->
            _loginStatus.value = success
        }
    }

    fun register(email: String, pass: String) {
        repository.register(email, pass) { success ->
            _registerStatus.value = success
        }
    }

    // Función para resetear estados
    fun resetStatus() {
        _loginStatus.value = null
        _registerStatus.value = null
    }
}