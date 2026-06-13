package com.aiadvertisement.core.model

import kotlinx.serialization.Serializable

@Serializable
data class AIChatMessage(
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class AIAdEnhance(
    val summary: String,
    val tags: List<AITag>
)

@Serializable
data class AITag(
    val name: String,
    val category: TagCategory
)

enum class TagCategory(val display: String) {
    CATEGORY("品类"),
    STYLE("风格"),
    AUDIENCE("受众"),
    SCENE("场景")
}

object AIConfig {
    const val API_KEY = "sk-b0d9ad92320b4a05b09c83c3dc92b941"
    const val BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/"
    const val MODEL_SUMMARY = "qwen-turbo"
    const val MODEL_CHAT = "qwen-turbo"

    const val SYSTEM_PROMPT_SUMMARY = """你是一位专业的广告内容编辑助手。请根据给定的广告标题和描述，生成：
1. 一段精炼的广告摘要（30-60字，突出核心卖点和用户价值）
2. 4个智能标签，分别属于：品类、风格、受众、场景这四个类别

请以纯 JSON 格式输出，不要任何额外文字，格式如下：
{"summary":"这里是摘要内容","tags":[{"name":"标签名1","category":"CATEGORY"},{"name":"标签名2","category":"STYLE"},{"name":"标签名3","category":"AUDIENCE"},{"name":"标签名4","category":"SCENE"}]}"""

    const val SYSTEM_PROMPT_CHAT = """你是一位专业的广告营销助手，名为「智投AI助手」。你的职责是：
1. 根据用户的产品和需求，提供创意文案和营销建议
2. 分析广告内容的受众画像和投放场景
3. 回答关于电商、品牌营销、广告投放的相关问题
4. 保持友好、专业、有创意的沟通风格

请用简洁明了的中文回答用户的问题。如果涉及具体产品，请先了解产品信息后再给出建议。"""
}
