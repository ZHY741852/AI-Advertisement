package com.aiadvertisement.domain.repository

import com.aiadvertisement.domain.model.AdFeedList

interface AdRepository {
    suspend fun getAdFeeds(channelKey: String, page: Int, pageSize: Int): Result<AdFeedList>
}
