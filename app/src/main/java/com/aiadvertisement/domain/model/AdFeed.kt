package com.aiadvertisement.domain.model

enum class AdChannel(val key: String, val display: String) {
    FEATURED("featured", "精选"),
    ECOMMERCE("ecommerce", "电商"),
    HANGZHOU("hangzhou", "杭州"),
    FOOD("food", "美食"),
    TECH("tech", "数码"),
    TRAVEL("travel", "旅游"),
    FASHION("fashion", "时尚");

    companion object {
        fun fromKey(key: String): AdChannel {
            return values().find { it.key == key } ?: FEATURED
        }
    }
}

enum class AdCardType(val key: String) {
    BIG_IMAGE("big_image"),
    SMALL_IMAGE("small_image"),
    VIDEO("video");

    companion object {
        fun fromKey(key: String): AdCardType {
            return values().find { it.key == key } ?: BIG_IMAGE
        }
    }
}

data class InteractionState(
    val adId: String,
    var isLiked: Boolean = false,
    var isCollected: Boolean = false,
    var isShared: Boolean = false,
    var likeCount: Int = 0,
    var exposureCount: Int = 0,
    var clickCount: Int = 0
)

data class AdFeed(
    val id: String,
    val title: String,
    val description: String,
    val summary: String?,
    val imageUrls: List<String>,
    val thumbnailUrl: String?,
    val videoUrl: String?,
    val advertiser: String,
    val channel: AdChannel,
    val cardType: AdCardType,
    val ctaText: String,
    val ctaLink: String,
    val isAI: Boolean,
    val aiPrompt: String?,
    val tags: List<String>,
    val likes: Int,
    val comments: Int,
    val shares: Int,
    val createTime: Long
) {
    val displayImageUrl: String
        get() = if (cardType == AdCardType.VIDEO && !thumbnailUrl.isNullOrEmpty()) {
            thumbnailUrl
        } else {
            imageUrls.firstOrNull() ?: ""
        }
}

data class AdFeedList(
    val items: List<AdFeed>,
    val total: Int,
    val page: Int,
    val pageSize: Int,
    val hasMore: Boolean
)
