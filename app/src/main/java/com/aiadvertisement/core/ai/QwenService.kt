package com.aiadvertisement.core.ai

import com.aiadvertisement.data.model.ChatCompletionRequest
import com.aiadvertisement.data.model.ChatCompletionResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface QwenService {

    @POST("chat/completions")
    suspend fun chatCompletion(@Body request: ChatCompletionRequest): ChatCompletionResponse
}
