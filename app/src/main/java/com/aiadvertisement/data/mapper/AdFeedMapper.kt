package com.aiadvertisement.data.mapper

import com.aiadvertisement.data.model.AdFeedDTO
import com.aiadvertisement.domain.model.AdCardType
import com.aiadvertisement.domain.model.AdChannel
import com.aiadvertisement.domain.model.AdFeed

object AdFeedMapper {

    fun AdFeedDTO.toDomain(): AdFeed {
        val urls = imageUrls.ifEmpty { thumbnailUrl?.let { listOf(it) } ?: emptyList() }
        return AdFeed(
            id = id,
            title = title,
            description = description,
            summary = summary,
            imageUrls = urls,
            thumbnailUrl = thumbnailUrl,
            videoUrl = videoUrl,
            advertiser = advertiser,
            channel = AdChannel.fromKey(channel),
            cardType = AdCardType.fromKey(cardType),
            ctaText = ctaText,
            ctaLink = ctaLink,
            isAI = isAI,
            aiPrompt = aiPrompt,
            tags = tags,
            likes = likes,
            comments = comments,
            shares = shares,
            createTime = createTime
        )
    }

    fun List<AdFeedDTO>.toDomain(): List<AdFeed> {
        return this.map { it.toDomain() }
    }
}
