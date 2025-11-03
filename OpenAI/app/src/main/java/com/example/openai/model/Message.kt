package com.example.proyectobase.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Message(
    val role: String,
    val content: String
)
