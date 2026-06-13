package com.aiadvertisement.domain.usecase

import com.aiadvertisement.core.ai.QwenClient
import com.aiadvertisement.domain.model.AdFeed
import com.aiadvertisement.data.model.ChatCompletionRequest
import com.aiadvertisement.data.model.ChatMessage
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class SearchIntent(
    val keywords: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val category: String? = null,
    val styleHint: String? = null,
    val audienceHint: String? = null,
    val sceneHint: String? = null,
    val priceRange: String? = null,
    val summary: String = ""
)

@Singleton
class SearchWithAIUseCase @Inject constructor() {

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    suspend fun parseSearchIntent(userQuery: String): SearchIntent {
        val systemPrompt = """你是一位专业的搜索意图解析助手。请分析用户的搜索描述，提取出用于过滤广告的关键信息。

请以纯 JSON 格式输出，不要任何额外文字，格式如下：
{
  "keywords": ["关键词1", "关键词2"],
  "tags": ["标签1", "标签2"],
  "category": "品类描述或null",
  "styleHint": "风格描述或null",
  "audienceHint": "受众描述或null",
  "sceneHint": "场景描述或null",
  "priceRange": "价格范围描述或null",
  "summary": "一句话总结用户需求"
}

规则：
1. tags 必须是简短的标签词，如"运动"、"学生党"、"性价比"、"限时"、"包邮"、"精选"等
2. 如果用户描述中没有对应字段，填 null 或空数组
3. 不要编造不存在的信息"""

        val userPrompt = """用户搜索描述：$userQuery

请解析这个搜索意图并输出 JSON。"""

        return try {
            val request = ChatCompletionRequest(
                model = "qwen-turbo",
                messages = listOf(
                    ChatMessage("system", systemPrompt),
                    ChatMessage("user", userPrompt)
                ),
                temperature = 0.3,
                maxTokens = 500
            )

            val response = QwenClient.getService().chatCompletion(request)
            val content = response.choices?.firstOrNull()?.message?.content
                ?: throw Exception("模型返回空内容")

            val cleanContent = content
                .trim()
                .removePrefix("```json")
                .removePrefix("```JSON")
                .removeSuffix("```")
                .trim()

            jsonParser.decodeFromString<SearchIntent>(cleanContent)
        } catch (e: Exception) {
            SearchIntent(
                keywords = listOf(userQuery),
                tags = emptyList(),
                summary = "正在为您搜索: $userQuery"
            )
        }
    }

    fun matchAds(feeds: List<AdFeed>, intent: SearchIntent): List<AdFeed> {
        if (feeds.isEmpty()) return emptyList()

        val searchText = buildString {
            append(intent.keywords.joinToString(" "))
            append(" ")
            append(intent.tags.joinToString(" "))
            append(" ")
            append(intent.category ?: "")
            append(" ")
            append(intent.styleHint ?: "")
            append(" ")
            append(intent.audienceHint ?: "")
            append(" ")
            append(intent.sceneHint ?: "")
            append(" ")
            append(intent.priceRange ?: "")
        }.trim().lowercase()

        if (searchText.isEmpty()) return feeds

        return feeds.map { feed ->
            val feedText = buildString {
                append(feed.title.lowercase())
                append(" ")
                append(feed.description.lowercase())
                append(" ")
                append(feed.tags.joinToString(" ").lowercase())
                append(" ")
                append(feed.summary?.lowercase() ?: "")
            }
            var score = 0
            val terms = searchText.split(" ").filter { it.isNotBlank() }

            for (term in terms) {
                if (feedText.contains(term)) score += 10
                if (feed.title.lowercase().contains(term)) score += 5
                feed.tags.forEach { tag ->
                    if (tag.lowercase().contains(term) || term.contains(tag.lowercase())) {
                        score += 15
                    }
                }
            }
            feed to score
        }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .map { it.first }
    }
}
