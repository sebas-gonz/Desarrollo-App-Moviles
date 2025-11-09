package com.example.proyectobase.repository

import com.example.openai.model.ForecastResponse
import com.example.openai.model.IntencionResponse
import com.example.proyectobase.api.OpenAIApi
import com.example.proyectobase.model.ChatRequest
import com.example.proyectobase.model.Message
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class OpenAIRepository(private val api: OpenAIApi) {
    suspend fun getCompletion(userText: String): String {
        val response = api.getChatCompletion(
            ChatRequest(
                messages = listOf(Message("user", userText))
            )
        )
        return response.choices.firstOrNull()?.message?.content ?: "Sin respuesta"
    }

    suspend fun getCityNameFromPrompt(userText: String): String {
        val systemMessage = Message(
            role = "system",
            content = "Eres un asistente que extrae nombres de ciudades. Responde SÓLO con el nombre de la ciudad. Si no hay ciudad, responde 'None'."
        )
        val userMessage = Message(
            role = "user",
            content = userText
        )

        val response = api.getChatCompletion(
            ChatRequest(
                messages = listOf(systemMessage, userMessage)
            )
        )
        return response.choices.firstOrNull()?.message?.content ?: "None"
    }

    suspend fun getForecastSummary(prompt: String, forecastData: ForecastResponse): String {

        // Convertimos el objeto de datos a un String JSON
        // para enviárselo a ChatGPT, ya que entiende JSON.
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter(ForecastResponse::class.java)
        val forecastJson = adapter.toJson(forecastData)

        val systemMessage = Message(
            role = "system",
            content = "Eres un asistente del clima. Responde amigablemente en español a la pregunta del usuario basándote SÓLO en los siguientes datos JSON del pronóstico. Sé conciso y responde solo con el resumen."
        )

        // Juntamos la pregunta y los datos para el usuario
        val userMessage = Message(
            role = "user",
            content = "Pregunta del usuario: '$prompt'. \n\n Datos JSON del pronóstico: $forecastJson"
        )

        val response = api.getChatCompletion(
            ChatRequest(
                messages = listOf(systemMessage, userMessage)
            )
        )
        return response.choices.firstOrNull()?.message?.content ?: "No pude procesar el pronóstico."
    }

    // Promp para que la ia decida basado en la intencion de la pregunta si ver el mapa, consultar el pronostico para los proximos dias o si no es otra pregunta.
    suspend fun analizarIntencion(userText: String): IntencionResponse {
        val systemMessage = Message(
            role = "system",
            content = """
            Eres un clasificador de intenciones. Analiza el texto del usuario.
            Responde SÓLAMENTE con un objeto JSON.
            El JSON debe tener dos claves: "intencion" y "ciudad".

            Las opciones para "intencion" son:
            - "ver_mapa": Si el usuario pide el clima actual, la ubicación o "dónde está".
            - "escuchar_pronostico": Si el usuario pregunta por el futuro ("mañana", "siguiente día", "va a llover", "pronóstico").
            - "desconocido": Si no es una pregunta sobre el clima o ubicación.

            Las opciones para "ciudad" es el nombre de la ciudad extraída, o "None" si no se menciona.

            Ejemplo 1:
            Usuario: "qué tiempo hace en londres"
            Respuesta: {"intencion": "ver_mapa", "ciudad": "Londres"}

            Ejemplo 2:
            Usuario: "lloverá mañana en parís"
            Respuesta: {"intencion": "escuchar_pronostico", "ciudad": "París"}
            
            Ejemplo 3:
            Usuario: "hola cómo estás"
            Respuesta: {"intencion": "desconocido", "ciudad": "None"}
            """.trimIndent()
        )
        val userMessage = Message(
            role = "user",
            content = userText
        )

        // Respuesta de chatgpt en json
        val jsonResponseString = api.getChatCompletion(
            ChatRequest(messages = listOf(systemMessage, userMessage))
        ).choices.firstOrNull()?.message?.content ?: "{\"intencion\": \"desconocido\", \"ciudad\": \"None\"}"

        // Json a Data class
        return try {
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter(IntencionResponse::class.java)
            adapter.fromJson(jsonResponseString) ?: IntencionResponse("desconocido", "None")
        } catch (e: Exception) {
            // Si el JSON de ChatGPT está mal formado
            IntencionResponse("desconocido", "None")
        }
    }

}