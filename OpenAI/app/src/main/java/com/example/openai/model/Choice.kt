package com.example.proyectobase.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Choice(
    val message: Message
)
