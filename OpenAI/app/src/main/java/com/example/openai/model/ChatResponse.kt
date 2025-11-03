package com.example.proyectobase.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatResponse(
    val choices: List<Choice>
)
