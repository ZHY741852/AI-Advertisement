package com.aiadvertisement.core.network.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import javax.inject.Inject

class LoggingInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        Log.d("HttpLog", "→ ${request.method} ${request.url}")
        request.headers.forEach { (name, value) ->
            Log.d("HttpLog", "  $name: $value")
        }

        val response = chain.proceed(request)
        Log.d("HttpLog", "← ${response.code} ${request.url}")

        val responseBody = response.body
        val contentType = responseBody?.contentType()
        val bodyString = responseBody?.string() ?: ""
        Log.d("HttpLog", "  Body: ${bodyString.take(500)}")

        return response.newBuilder()
            .body(bodyString.toResponseBody(contentType))
            .build()
    }
}
