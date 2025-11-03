package com.example.proyectobase.repository

import com.example.proyectobase.api.OpenAIApi
import com.example.proyectobase.model.ChatRequest
import com.example.proyectobase.model.Message

class OpenAIRepository(private val api: OpenAIApi) {
    suspend fun getCompletion(userText: String): String {
        val response = api.getChatCompletion(
            ChatRequest(
                messages = listOf(Message("user", userText))
            )
        )
        return response.choices.firstOrNull()?.message?.content ?: "Sin respuesta"
    }
}