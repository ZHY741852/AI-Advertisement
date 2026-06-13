package com.aiadvertisement.core.ai

import com.aiadvertisement.core.model.AIConfig
import com.aiadvertisement.data.model.ChatCompletionRequest
import com.aiadvertisement.data.model.ChatCompletionResponse
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import java.util.concurrent.TimeUnit

object QwenClient {

    private const val TIMEOUT_CONNECT = 30L
    private const val TIMEOUT_READ = 60L
    private const val TIMEOUT_WRITE = 60L

    private val json by lazy {
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            encodeDefaults = true
            isLenient = true
        }
    }

    private val authInterceptor by lazy {
        Interceptor { chain ->
            val originalRequest = chain.request()
            val requestWithAuth = originalRequest.newBuilder()
                .header("Authorization", "Bearer ${AIConfig.API_KEY}")
                .header("Content-Type", "application/json")
                .build()
            chain.proceed(requestWithAuth)
        }
    }

    private val loggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(TIMEOUT_CONNECT, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_READ, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_WRITE, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(AIConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    private val qwenService by lazy {
        retrofit.create(QwenService::class.java)
    }

    fun getService(): QwenService = qwenService
}
