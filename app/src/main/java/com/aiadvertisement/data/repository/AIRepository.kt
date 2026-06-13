package com.aiadvertisement.data.repository

import com.aiadvertisement.core.ai.QwenClient
import com.aiadvertisement.core.model.AIAdEnhance
import com.aiadvertisement.core.model.AIChatMessage
import com.aiadvertisement.core.model.AIConfig
import com.aiadvertisement.core.model.AITag
import com.aiadvertisement.core.model.TagCategory
import com.aiadvertisement.data.model.AIEnhanceResult
import com.aiadvertisement.data.model.AITagData
import com.aiadvertisement.data.model.ChatCompletionRequest
import com.aiadvertisement.data.model.ChatMessage
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIRepository @Inject constructor() {

    private val qwenService = QwenClient.getService()

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    suspend fun generateAdSummary(title: String, description: String): AIAdEnhance {
        return try {
            val userPrompt = """请为以下广告生成摘要和标签：
标题：$title
描述：$description

请严格按照之前约定的 JSON 格式输出，不要有任何额外的解释文字。"""

            val request = ChatCompletionRequest(
                model = AIConfig.MODEL_SUMMARY,
                messages = listOf(
                    ChatMessage("system", AIConfig.SYSTEM_PROMPT_SUMMARY),
                    ChatMessage("user", userPrompt)
                ),
                temperature = 0.7,
                maxTokens = 500
            )

            val response = qwenService.chatCompletion(request)
            val content = response.choices?.firstOrNull()?.message?.content
                ?: throw Exception("模型返回空内容")

            parseEnhanceResult(content)
        } catch (e: Exception) {
            println("AIRepository: 生成广告摘要失败: ${e.message}")
            AIAdEnhance(
                summary = generateFallbackSummary(title, description),
                tags = generateFallbackTags(title, description)
            )
        }
    }

    suspend fun chatWithAI(messages: List<AIChatMessage>): String {
        return try {
            val apiMessages = mutableListOf<ChatMessage>()
            apiMessages.add(ChatMessage("system", AIConfig.SYSTEM_PROMPT_CHAT))
            messages.forEach { msg ->
                apiMessages.add(ChatMessage(msg.role, msg.content))
            }

            val request = ChatCompletionRequest(
                model = AIConfig.MODEL_CHAT,
                messages = apiMessages,
                temperature = 0.8,
                maxTokens = 1500
            )

            val response = qwenService.chatCompletion(request)
            response.choices?.firstOrNull()?.message?.content
                ?: "抱歉，我暂时无法回答这个问题。"
        } catch (e: Exception) {
            println("AIRepository: AI 对话失败: ${e.message}")
            "抱歉，服务暂时不可用，请稍后再试。（错误：${e.message}）"
        }
    }

    private fun parseEnhanceResult(content: String): AIAdEnhance {
        val cleanContent = content
            .trim()
            .removePrefix("```json")
            .removePrefix("```JSON")
            .removeSuffix("```")
            .trim()

        return try {
            val result = jsonParser.decodeFromString<AIEnhanceResult>(cleanContent)
            val tagList = result.tags.mapNotNull { tagData ->
                val category = when (tagData.category.uppercase()) {
                    "CATEGORY" -> TagCategory.CATEGORY
                    "STYLE" -> TagCategory.STYLE
                    "AUDIENCE" -> TagCategory.AUDIENCE
                    "SCENE" -> TagCategory.SCENE
                    else -> null
                }
                category?.let { AITag(tagData.name, it) }
            }
            AIAdEnhance(summary = result.summary, tags = tagList)
        } catch (e: Exception) {
            println("AIRepository: 解析 AI 返回结果失败: ${e.message}, content=$content")
            AIAdEnhance(
                summary = content.take(80),
                tags = listOf(
                    AITag("精选推荐", TagCategory.CATEGORY),
                    AITag("优质内容", TagCategory.STYLE)
                )
            )
        }
    }

    private fun generateFallbackSummary(title: String, description: String): String {
        val fullText = "$title $description"
        return fullText.take(50).let { if (it.length < fullText.length) "$it..." else it }
    }

    private fun generateFallbackTags(title: String, description: String): List<AITag> {
        return listOf(
            AITag("精选推荐", TagCategory.CATEGORY),
            AITag("优质内容", TagCategory.STYLE),
            AITag("广泛受众", TagCategory.AUDIENCE),
            AITag("日常消费", TagCategory.SCENE)
        )
    }
}
