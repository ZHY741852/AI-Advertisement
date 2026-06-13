package com.aiadvertisement.core.model

data class AdStats(
    val adId: String,
    val exposureCount: Int,
    val clickCount: Int,
    val isLiked: Boolean,
    val isCollected: Boolean,
    val isShared: Boolean
)
