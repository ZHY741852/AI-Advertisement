package com.aiadvertisement.data.model

import kotlinx.serialization.Serializable

@Serializable
data class MockAdFeedResponse(
    val code: Int = 0,
    val message: String = "success",
    val data: List<AdFeedDTO>
)
