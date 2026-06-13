package com.aiadvertisement.core.network

import com.aiadvertisement.data.model.ApiResponse
import com.aiadvertisement.data.model.AdFeedDTO
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("v1/feed/ads")
    suspend fun getAdFeed(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int = 20
    ): ApiResponse<List<AdFeedDTO>>
}
