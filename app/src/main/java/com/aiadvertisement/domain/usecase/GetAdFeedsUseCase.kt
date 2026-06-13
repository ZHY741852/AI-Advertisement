package com.aiadvertisement.domain.usecase

import com.aiadvertisement.domain.model.AdFeedList
import com.aiadvertisement.domain.repository.AdRepository
import javax.inject.Inject

class GetAdFeedsUseCase @Inject constructor(
    private val adRepository: AdRepository
) {
    suspend operator fun invoke(channelKey: String, page: Int = 1, pageSize: Int = 10): Result<AdFeedList> {
        return adRepository.getAdFeeds(channelKey, page, pageSize)
    }
}
