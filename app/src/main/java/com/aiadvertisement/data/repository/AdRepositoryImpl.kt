package com.aiadvertisement.data.repository

import com.aiadvertisement.core.exception.ExceptionHandler
import com.aiadvertisement.data.mock.MockDataProvider
import com.aiadvertisement.domain.model.AdFeedList
import com.aiadvertisement.domain.repository.AdRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdRepositoryImpl @Inject constructor(
    private val mockDataProvider: MockDataProvider
) : AdRepository {

    override suspend fun getAdFeeds(channelKey: String, page: Int, pageSize: Int): Result<AdFeedList> {
        return try {
            val result = mockDataProvider.getAdFeeds(channelKey, page, pageSize)
            Result.success(result)
        } catch (e: Exception) {
            val appException = ExceptionHandler.handle(e)
            Result.failure(appException)
        }
    }
}
