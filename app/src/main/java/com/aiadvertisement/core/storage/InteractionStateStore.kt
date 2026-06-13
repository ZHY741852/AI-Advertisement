package com.aiadvertisement.core.storage

import android.content.Context
import com.tencent.mmkv.MMKV
import com.aiadvertisement.core.model.AdStats

class InteractionStateStore(private val context: Context) {

    private val kv: MMKV = MMKVHelper.getMMKV("interaction_stats")

    private fun exposureKey(adId: String) = "exposure_$adId"
    private fun clickKey(adId: String) = "click_$adId"
    private fun likeKey(adId: String) = "like_$adId"
    private fun collectKey(adId: String) = "collect_$adId"
    private fun shareKey(adId: String) = "share_$adId"
    private val adIdsKey = "all_ad_ids"

    private fun getAllAdIds(): Set<String> {
        return kv.decodeStringSet(adIdsKey, emptySet()) ?: emptySet()
    }

    private fun addAdId(adId: String) {
        val current = getAllAdIds().toMutableSet()
        if (current.add(adId)) {
            kv.encode(adIdsKey, current)
        }
    }

    fun incrementExposure(adId: String) {
        addAdId(adId)
        val key = exposureKey(adId)
        val current = kv.decodeInt(key, 0)
        kv.encode(key, current + 1)
    }

    fun incrementClick(adId: String) {
        addAdId(adId)
        val key = clickKey(adId)
        val current = kv.decodeInt(key, 0)
        kv.encode(key, current + 1)
    }

    fun getExposureCount(adId: String): Int = kv.decodeInt(exposureKey(adId), 0)
    fun getClickCount(adId: String): Int = kv.decodeInt(clickKey(adId), 0)

    fun setLiked(adId: String, liked: Boolean) {
        addAdId(adId)
        kv.encode(likeKey(adId), liked)
    }

    fun isLiked(adId: String): Boolean = kv.decodeBool(likeKey(adId), false)

    fun setCollected(adId: String, collected: Boolean) {
        addAdId(adId)
        kv.encode(collectKey(adId), collected)
    }

    fun isCollected(adId: String): Boolean = kv.decodeBool(collectKey(adId), false)

    fun setShared(adId: String, shared: Boolean) {
        addAdId(adId)
        kv.encode(shareKey(adId), shared)
    }

    fun isShared(adId: String): Boolean = kv.decodeBool(shareKey(adId), false)

    fun getAllStats(): List<AdStats> {
        val ids = getAllAdIds()
        return ids.map { adId ->
            AdStats(
                adId = adId,
                exposureCount = getExposureCount(adId),
                clickCount = getClickCount(adId),
                isLiked = isLiked(adId),
                isCollected = isCollected(adId),
                isShared = isShared(adId)
            )
        }.sortedByDescending { it.exposureCount }
    }

    fun clearAll() {
        kv.clearAll()
    }
}
