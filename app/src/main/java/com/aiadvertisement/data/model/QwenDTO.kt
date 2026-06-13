package com.aiadvertisement.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double = 0.7,
    @SerialName("max_tokens")
    val maxTokens: Int = 1024,
    val stream: Boolean = false
)

@Serializable
data class ChatCompletionResponse(
    val id: String? = null,
    val `object`: String? = null,
    val created: Long? = null,
    val model: String? = null,
    val choices: List<Choice>? = null,
    val usage: Usage? = null,
    val error: ErrorInfo? = null
)

@Serializable
data class Choice(
    val index: Int? = null,
    val message: Message? = null,
    @SerialName("finish_reason")
    val finishReason: String? = null
)

@Serializable
data class Message(
    val role: String? = null,
    val content: String? = null
)

@Serializable
data class Usage(
    @SerialName("prompt_tokens")
    val promptTokens: Int? = null,
    @SerialName("completion_tokens")
    val completionTokens: Int? = null,
    @SerialName("total_tokens")
    val totalTokens: Int? = null
)

@Serializable
data class ErrorInfo(
    val code: String? = null,
    val message: String? = null,
    val type: String? = null
)

@Serializable
data class AIEnhanceResult(
    val summary: String,
    val tags: List<AITagData>
)

@Serializable
data class AITagData(
    val name: String,
    val category: String
)
