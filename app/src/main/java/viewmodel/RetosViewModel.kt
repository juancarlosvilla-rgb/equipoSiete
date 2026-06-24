package com.univalle.picobotella.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.univalle.picobotella.RetoModel
import com.univalle.picobotella.data.RetosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RetosViewModel @Inject constructor(private val repo: RetosRepository) : ViewModel() {
    private val _retos = MutableLiveData<List<RetoModel>>()
    val retos: LiveData<List<RetoModel>> = _retos

    fun cargar() {
        viewModelScope.launch { _retos.value = repo.getRetos() }
    }

    fun agregar(d: String) {
        viewModelScope.launch {
            repo.addReto(d)
            cargar()
        }
    }

    fun editar(id: String, d: String) {
        viewModelScope.launch {
            repo.updateReto(id, d)
            cargar()
        }
    }

    fun eliminar(id: String) {
        viewModelScope.launch {
            repo.deleteReto(id)
            cargar()
        }
    }
}