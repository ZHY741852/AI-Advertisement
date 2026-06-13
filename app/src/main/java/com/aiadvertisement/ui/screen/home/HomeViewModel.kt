package com.aiadvertisement.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiadvertisement.core.model.AIAdEnhance
import com.aiadvertisement.core.storage.InteractionStateStore
import com.aiadvertisement.core.storage.ScrollPositionStore
import com.aiadvertisement.data.mock.MockDataProvider
import com.aiadvertisement.domain.model.AdChannel
import com.aiadvertisement.domain.model.AdFeed
import com.aiadvertisement.domain.usecase.GenerateAdSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val mockDataProvider: MockDataProvider,
    private val interactionStateStore: InteractionStateStore,
    private val scrollPositionStore: ScrollPositionStore,
    private val generateAdSummaryUseCase: GenerateAdSummaryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _aiEnhanceMap = MutableStateFlow<Map<String, AIAdEnhance>>(emptyMap())
    val aiEnhanceMap: StateFlow<Map<String, AIAdEnhance>> = _aiEnhanceMap.asStateFlow()

    private var currentPage = 1
    private val pageSize = 10

    init {
        loadInitial()
    }

    fun switchChannel(channel: AdChannel) {
        currentPage = 1
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                currentChannel = channel,
                adFeeds = emptyList(),
                filteredAdFeeds = emptyList(),
                selectedTags = emptySet(),
                allAvailableTags = emptySet(),
                isLoading = true,
                hasMore = true,
                error = null
            )
            try {
                val items = mockDataProvider.getAdFeeds(channel.key, currentPage, pageSize)
                val (scrollIndex, scrollOffset) = scrollPositionStore.getPosition(channel.key)
                val allTags = collectAllTags(items.items)
                _uiState.value = _uiState.value.copy(
                    adFeeds = items.items,
                    filteredAdFeeds = items.items,
                    allAvailableTags = allTags,
                    isLoading = false,
                    hasMore = items.hasMore,
                    scrollIndex = scrollIndex,
                    scrollOffset = scrollOffset
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "加载失败"
                )
            }
        }
    }

    private fun collectAllTags(feeds: List<AdFeed>): Set<String> {
        return feeds.flatMap { it.tags }.toSet()
    }

    fun toggleTagFilter(tag: String) {
        val currentSelected = _uiState.value.selectedTags
        val newSelected = if (currentSelected.contains(tag)) {
            currentSelected - tag
        } else {
            currentSelected + tag
        }
        val filtered = applyTagFilter(_uiState.value.adFeeds, newSelected)
        _uiState.value = _uiState.value.copy(
            selectedTags = newSelected,
            filteredAdFeeds = filtered
        )
    }

    fun clearTagFilter() {
        _uiState.value = _uiState.value.copy(
            selectedTags = emptySet(),
            filteredAdFeeds = _uiState.value.adFeeds
        )
    }

    private fun applyTagFilter(feeds: List<AdFeed>, tags: Set<String>): List<AdFeed> {
        return if (tags.isEmpty()) {
            feeds
        } else {
            feeds.filter { feed ->
                tags.all { tag -> feed.tags.contains(tag) }
            }
        }
    }

    private fun loadInitial() {
        val channel = _uiState.value.currentChannel
        switchChannel(channel)
    }

    fun refresh() {
        currentPage = 1
        val channel = _uiState.value.currentChannel
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRefreshing = true,
                error = null
            )
            val startTime = System.currentTimeMillis()
            val minRefreshDuration = 600L
            try {
                val items = mockDataProvider.getAdFeeds(channel.key, currentPage, pageSize)
                val allTags = collectAllTags(items.items)
                val elapsed = System.currentTimeMillis() - startTime
                if (elapsed < minRefreshDuration) {
                    kotlinx.coroutines.delay(minRefreshDuration - elapsed)
                }
                _uiState.value = _uiState.value.copy(
                    adFeeds = items.items,
                    filteredAdFeeds = applyTagFilter(items.items, _uiState.value.selectedTags),
                    allAvailableTags = allTags,
                    isRefreshing = false,
                    hasMore = items.hasMore
                )
            } catch (e: Exception) {
                val elapsed = System.currentTimeMillis() - startTime
                if (elapsed < minRefreshDuration) {
                    kotlinx.coroutines.delay(minRefreshDuration - elapsed)
                }
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    error = e.message ?: "刷新失败"
                )
            }
        }
    }

    fun loadMore() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMore) return
        currentPage++
        val channel = _uiState.value.currentChannel
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMore = true)
            try {
                val items = mockDataProvider.getAdFeeds(channel.key, currentPage, pageSize)
                val combined = _uiState.value.adFeeds + items.items
                val allTags = collectAllTags(combined)
                _uiState.value = _uiState.value.copy(
                    adFeeds = combined,
                    filteredAdFeeds = applyTagFilter(combined, _uiState.value.selectedTags),
                    allAvailableTags = allTags,
                    isLoadingMore = false,
                    hasMore = items.hasMore
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingMore = false)
            }
        }
    }

    fun generateAIEnhance(adFeed: AdFeed) {
        if (_aiEnhanceMap.value.containsKey(adFeed.id)) return

        viewModelScope.launch {
            try {
                val enhance = generateAdSummaryUseCase(adFeed.title, adFeed.description)
                val currentMap = _aiEnhanceMap.value.toMutableMap()
                currentMap[adFeed.id] = enhance
                _aiEnhanceMap.value = currentMap
            } catch (e: Exception) {
            }
        }
    }

    fun getAIEnhance(adId: String): AIAdEnhance? {
        return _aiEnhanceMap.value[adId]
    }

    fun recordExposure(adId: String) {
        interactionStateStore.incrementExposure(adId)
    }

    fun recordClick(adId: String) {
        interactionStateStore.incrementClick(adId)
    }

    fun getFeedById(adId: String): AdFeed? {
        return _uiState.value.adFeeds.find { it.id == adId }
    }

    fun saveScrollPosition(index: Int, offset: Int) {
        val channel = _uiState.value.currentChannel
        scrollPositionStore.savePosition(channel.key, index, offset)
    }
}

data class HomeUiState(
    val currentChannel: AdChannel = AdChannel.FEATURED,
    val adFeeds: List<AdFeed> = emptyList(),
    val filteredAdFeeds: List<AdFeed> = emptyList(),
    val selectedTags: Set<String> = emptySet(),
    val allAvailableTags: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null,
    val scrollIndex: Int = 0,
    val scrollOffset: Int = 0
)
