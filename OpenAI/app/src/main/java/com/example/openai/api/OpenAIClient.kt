package com.example.openai.api
import com.example.openai.BuildConfig
import com.example.proyectobase.api.OpenAIApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
object OpenAIClient {
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain: Interceptor.Chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer ${BuildConfig.OPENAI_API_KEY}")
                .build()
            chain.proceed(request)
        }
        .build()
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val retrofit = Retrofit.Builder ()
        .baseUrl("https://api.openai.com/")
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val api: OpenAIApi = retrofit.create(OpenAIApi::class.java)
}