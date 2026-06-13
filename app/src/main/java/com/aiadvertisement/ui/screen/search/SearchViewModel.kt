package com.aiadvertisement.ui.screen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiadvertisement.core.model.AIAdEnhance
import com.aiadvertisement.data.mock.MockDataProvider
import com.aiadvertisement.domain.model.AdFeed
import com.aiadvertisement.domain.usecase.GenerateAdSummaryUseCase
import com.aiadvertisement.domain.usecase.SearchIntent
import com.aiadvertisement.domain.usecase.SearchWithAIUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val mockDataProvider: MockDataProvider,
    private val searchWithAIUseCase: SearchWithAIUseCase,
    private val generateAdSummaryUseCase: GenerateAdSummaryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _aiEnhanceMap = MutableStateFlow<Map<String, AIAdEnhance>>(emptyMap())
    val aiEnhanceMap: StateFlow<Map<String, AIAdEnhance>> = _aiEnhanceMap.asStateFlow()

    private val chatHistory = mutableListOf<SearchChatMessage>()

    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(queryText = query)
    }

    fun executeSearch() {
        val query = _uiState.value.queryText.trim()
        if (query.isEmpty()) return

        chatHistory.add(SearchChatMessage(role = "user", content = query))
        _uiState.value = _uiState.value.copy(
            isAILoading = true,
            searchRounds = _uiState.value.searchRounds + SearchRound(
                query = query,
                results = emptyList(),
                intent = null,
                aiResponse = null
            ),
            error = null,
            chatHistory = chatHistory.toList()
        )

        viewModelScope.launch {
            try {
                val intent = searchWithAIUseCase.parseSearchIntent(query)
                val allFeeds = loadAllFeeds()
                val matched = searchWithAIUseCase.matchAds(allFeeds, intent)

                val aiResponse = buildAIResponse(query, intent, matched.size)
                chatHistory.add(SearchChatMessage(role = "assistant", content = aiResponse))

                // 更新最后一轮的搜索结果，保留之前所有轮次
                val currentRounds = _uiState.value.searchRounds.toMutableList()
                if (currentRounds.isNotEmpty()) {
                    currentRounds[currentRounds.size - 1] = SearchRound(
                        query = query,
                        results = matched,
                        intent = intent,
                        aiResponse = aiResponse
                    )
                }

                _uiState.value = _uiState.value.copy(
                    isAILoading = false,
                    searchRounds = currentRounds.toList(),
                    chatHistory = chatHistory.toList(),
                    queryText = ""
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isAILoading = false,
                    error = e.message ?: "搜索失败"
                )
            }
        }
    }

    private fun loadAllFeeds(): List<AdFeed> {
        return try {
            val featured = mockDataProvider.getAdFeeds("featured", 1, 20).items
            val ecommerce = mockDataProvider.getAdFeeds("ecommerce", 1, 20).items
            val local = mockDataProvider.getAdFeeds("local", 1, 20).items
            (featured + ecommerce + local).distinctBy { it.id }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun buildAIResponse(query: String, intent: SearchIntent, resultCount: Int): String {
        return buildString {
            if (intent.summary.isNotEmpty()) {
                append("${intent.summary}\n\n")
            }
            if (intent.tags.isNotEmpty()) {
                append("识别标签: ${intent.tags.joinToString("、")}\n")
            }
            if (intent.keywords.isNotEmpty()) {
                append("关键词: ${intent.keywords.joinToString("、")}\n")
            }
            intent.category?.let { append("品类: $it\n") }
            intent.styleHint?.let { append("风格: $it\n") }
            intent.audienceHint?.let { append("受众: $it\n") }
            intent.sceneHint?.let { append("场景: $it\n") }
            intent.priceRange?.let { append("价格: $it\n") }
            append("\n为您找到 $resultCount 个相关广告")
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

    fun clearSearch() {
        chatHistory.clear()
        _uiState.value = SearchUiState()
    }
}

data class SearchUiState(
    val queryText: String = "",
    val searchRounds: List<SearchRound> = emptyList(),
    val chatHistory: List<SearchChatMessage> = emptyList(),
    val isAILoading: Boolean = false,
    val error: String? = null
)

data class SearchRound(
    val query: String,
    val results: List<AdFeed>,
    val intent: SearchIntent?,
    val aiResponse: String?
)

data class SearchChatMessage(
    val role: String,
    val content: String
)
