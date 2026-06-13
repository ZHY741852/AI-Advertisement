package com.aiadvertisement.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AdFeedDTO(
    val id: String,
    val title: String,
    val description: String,
    val summary: String? = null,
    val imageUrls: List<String> = emptyList(),
    val thumbnailUrl: String? = null,
    val videoUrl: String? = null,
    val advertiser: String,
    val channel: String = "featured",
    val cardType: String = "big_image",
    val ctaText: String,
    val ctaLink: String,
    val isAI: Boolean = false,
    val aiPrompt: String? = null,
    val tags: List<String> = emptyList(),
    val likes: Int = 0,
    val comments: Int = 0,
    val shares: Int = 0,
    val createTime: Long = 0
)
