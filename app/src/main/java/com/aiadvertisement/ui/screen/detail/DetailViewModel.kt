package com.aiadvertisement.ui.screen.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiadvertisement.core.model.AIAdEnhance
import com.aiadvertisement.core.storage.InteractionStateStore
import com.aiadvertisement.data.mock.MockDataProvider
import com.aiadvertisement.domain.model.AdFeed
import com.aiadvertisement.domain.usecase.GenerateAdSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val mockDataProvider: MockDataProvider,
    private val interactionStateStore: InteractionStateStore,
    private val generateAdSummaryUseCase: GenerateAdSummaryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun loadAd(adId: String) {
        viewModelScope.launch {
            val ad = mockDataProvider.findAdById(adId)
            val isLiked = interactionStateStore.isLiked(adId)
            val isCollected = interactionStateStore.isCollected(adId)
            val isShared = interactionStateStore.isShared(adId)
            interactionStateStore.incrementClick(adId)
            _uiState.value = _uiState.value.copy(
                adFeed = ad,
                isLiked = isLiked,
                isCollected = isCollected,
                isShared = isShared
            )
            ad?.let {
                generateAIEnhance(it.title, it.description)
            }
        }
    }

    private fun generateAIEnhance(title: String, description: String) {
        viewModelScope.launch {
            try {
                val enhance = generateAdSummaryUseCase(title, description)
                _uiState.value = _uiState.value.copy(
                    aiEnhance = enhance,
                    isAILoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isAILoading = false)
            }
        }
    }

    fun toggleLike(adId: String) {
        val current = _uiState.value.isLiked
        val newState = !current
        interactionStateStore.setLiked(adId, newState)
        val baseLikes = _uiState.value.adFeed?.likes ?: 0
        val likeCount = baseLikes + if (newState) 1 else 0
        _uiState.value = _uiState.value.copy(isLiked = newState, likeCount = likeCount)
    }

    fun toggleCollect(adId: String) {
        val current = _uiState.value.isCollected
        val newState = !current
        interactionStateStore.setCollected(adId, newState)
        _uiState.value = _uiState.value.copy(isCollected = newState)
    }

    fun toggleShare(adId: String) {
        interactionStateStore.setShared(adId, true)
        _uiState.value = _uiState.value.copy(isShared = true)
    }
}

data class DetailUiState(
    val adFeed: AdFeed? = null,
    val isLiked: Boolean = false,
    val isCollected: Boolean = false,
    val isShared: Boolean = false,
    val likeCount: Int = 0,
    val aiEnhance: AIAdEnhance? = null,
    val isAILoading: Boolean = true
)
