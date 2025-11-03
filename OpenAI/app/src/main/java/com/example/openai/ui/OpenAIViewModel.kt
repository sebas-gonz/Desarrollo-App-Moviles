package com.example.proyectobase.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.openai.api.OpenAIClient
import com.example.proyectobase.repository.OpenAIRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OpenAIViewModel( private val repository: OpenAIRepository = OpenAIRepository(OpenAIClient.api)
) : ViewModel() {
    private val _response = MutableStateFlow<String>("")
    val response: StateFlow<String> = _response

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun enviarMensaje(userText: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val reply = repository.getCompletion(userText)
                _response.value = reply
            } catch (e: Exception) {
                _response.value = "Error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
}