package com.aiadvertisement.ui.screen.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiadvertisement.core.model.AdStats
import com.aiadvertisement.core.storage.InteractionStateStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val interactionStateStore: InteractionStateStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    fun loadStats() {
        viewModelScope.launch {
            val stats = interactionStateStore.getAllStats()
            var exposures: Int = 0
            var clicks: Int = 0
            var likes: Int = 0
            var shares: Int = 0
            stats.forEach { s ->
                exposures += s.exposureCount
                clicks += s.clickCount
                if (s.isLiked) likes++
                if (s.isShared) shares++
            }
            _uiState.value = _uiState.value.copy(
                stats = stats,
                totalExposures = exposures,
                totalClicks = clicks,
                totalLikes = likes,
                totalShares = shares
            )
        }
    }

    fun clearStats() {
        interactionStateStore.clearAll()
        loadStats()
    }
}

data class StatsUiState(
    val stats: List<AdStats> = emptyList(),
    val totalExposures: Int = 0,
    val totalClicks: Int = 0,
    val totalLikes: Int = 0,
    val totalShares: Int = 0
)
