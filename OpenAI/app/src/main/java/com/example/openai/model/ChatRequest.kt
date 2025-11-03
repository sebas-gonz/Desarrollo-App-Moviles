package com.example.proyectobase.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatRequest(
    val model: String = "gpt-4o-mini",
    val messages: List<Message>,
    val max_tokens: Int = 150
)
