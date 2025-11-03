package com.example.proyectobase.api
import com.example.proyectobase.model.ChatRequest
import com.example.proyectobase.model.ChatResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
interface OpenAIApi {
    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    suspend fun getChatCompletion(@Body request: ChatRequest): ChatResponse
}