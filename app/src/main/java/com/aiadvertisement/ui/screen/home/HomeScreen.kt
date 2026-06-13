package com.aiadvertisement.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.aiadvertisement.domain.model.AdChannel
import com.aiadvertisement.ui.components.AdFeedCard
import com.aiadvertisement.ui.components.EmptyView
import com.aiadvertisement.ui.components.ErrorView
import com.aiadvertisement.ui.components.LoadingView
import com.aiadvertisement.ui.navigation.navigateToDetail
import com.aiadvertisement.ui.navigation.navigateToSearch
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val aiEnhanceMap by viewModel.aiEnhanceMap.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var scrollRestored = androidx.compose.runtime.remember { mutableMapOf<String, Boolean>() }

    LaunchedEffect(uiState.currentChannel.key, uiState.adFeeds.isNotEmpty()) {
        if (uiState.adFeeds.isNotEmpty() && scrollRestored[uiState.currentChannel.key] != true) {
            if (uiState.scrollIndex > 0) {
                listState.scrollToItem(uiState.scrollIndex, uiState.scrollOffset)
            }
            scrollRestored[uiState.currentChannel.key] = true
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            Triple(
                listState.firstVisibleItemIndex,
                listState.firstVisibleItemScrollOffset,
                listState.isScrollInProgress
            )
        }.collect { (index, offset, inProgress) ->
            if (!inProgress) {
                viewModel.saveScrollPosition(index, offset)
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("广告信息流", style = MaterialTheme.typography.titleLarge) },
                    actions = {
                        IconButton(onClick = { navController.navigateToSearch() }) {
                            Icon(Icons.Default.Search, contentDescription = "搜索")
                        }
                    }
                )
                ChannelTabs(
                    currentChannel = uiState.currentChannel,
                    onChannelSelected = { channel ->
                        coroutineScope.launch {
                            viewModel.switchChannel(channel)
                            listState.scrollToItem(0)
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = uiState.isRefreshing)
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading && uiState.adFeeds.isEmpty() -> {
                    LoadingView()
                }
                uiState.error != null && uiState.adFeeds.isEmpty() -> {
                    ErrorView(
                        message = uiState.error!!,
                        onRetry = { viewModel.refresh() }
                    )
                }
                uiState.adFeeds.isEmpty() -> {
                    EmptyView(message = "暂无广告内容")
                }
                else -> {
                    Column {
                        TagFilterBar(
                            selectedTags = uiState.selectedTags,
                            allTags = uiState.allAvailableTags,
                            onTagClick = { viewModel.toggleTagFilter(it) },
                            onClear = { viewModel.clearTagFilter() }
                        )
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.filteredAdFeeds, key = { it.id }) { adFeed ->
                                AdFeedCard(
                                    adFeed = adFeed,
                                    onCardClick = { feed, position ->
                                        viewModel.recordClick(feed.id)
                                        navController.navigateToDetail(feed.id, position)
                                    },
                                    onExposure = { adId ->
                                        viewModel.recordExposure(adId)
                                    },
                                    aiEnhance = aiEnhanceMap[adFeed.id],
                                    onGenerateAI = { feed ->
                                        viewModel.generateAIEnhance(feed)
                                    },
                                    onTagClick = { tag ->
                                        viewModel.toggleTagFilter(tag)
                                    },
                                    selectedTags = uiState.selectedTags
                                )
                            }

                            if (uiState.isLoadingMore) {
                                item {
                                    LoadingView(modifier = Modifier.padding(16.dp))
                                }
                            }
                            if (!uiState.hasMore && uiState.filteredAdFeeds.isNotEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "— 已加载全部内容 —",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            if (uiState.selectedTags.isNotEmpty() && uiState.filteredAdFeeds.isEmpty()) {
                                item {
                                    EmptyView(message = "没有匹配「${uiState.selectedTags.joinToString("、")}」的广告")
                                }
                            }
                        }
                    }

                    LaunchedEffect(listState) {
                        snapshotFlow {
                            val layoutInfo = listState.layoutInfo
                            val totalItems = layoutInfo.totalItemsCount
                            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                            lastVisibleItemIndex >= totalItems - 3
                        }.collect { shouldLoadMore ->
                            if (shouldLoadMore && !uiState.isLoadingMore && uiState.hasMore) {
                                viewModel.loadMore()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChannelTabs(
    currentChannel: AdChannel,
    onChannelSelected: (AdChannel) -> Unit
) {
    val channels = listOf(
        AdChannel.FEATURED to "精选",
        AdChannel.ECOMMERCE to "电商",
        AdChannel.HANGZHOU to "杭州",
        AdChannel.FOOD to "美食",
        AdChannel.TECH to "数码",
        AdChannel.TRAVEL to "旅游",
        AdChannel.FASHION to "时尚"
    )
    val selectedIndex = channels.indexOfFirst { it.first == currentChannel }.coerceAtLeast(0)

    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        containerColor = MaterialTheme.colorScheme.surface,
        divider = {},
        indicator = { tabPositions ->
            if (selectedIndex < tabPositions.size) {
                val tabPos = tabPositions[selectedIndex]
                val tabHeight = 48.dp
                Box(
                    modifier = Modifier
                        .padding(
                            start = tabPos.left + 20.dp,
                            top = tabHeight - 3.dp
                        )
                        .width(tabPos.width - 40.dp)
                        .height(3.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    ) {
        channels.forEach { (channel, display) ->
            Tab(
                selected = channel == currentChannel,
                onClick = { onChannelSelected(channel) },
                text = {
                    Text(
                        text = display,
                        style = if (channel == currentChannel)
                            MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.primary
                            )
                        else
                            MaterialTheme.typography.bodyLarge
                    )
                }
            )
        }
    }
}

@Composable
private fun TagFilterBar(
    selectedTags: Set<String>,
    allTags: Set<String>,
    onTagClick: (String) -> Unit,
    onClear: () -> Unit
) {
    if (allTags.isEmpty()) return
    val hasSelection = selectedTags.isNotEmpty()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface)
    ) {
        if (hasSelection) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "已筛选: ${selectedTags.joinToString("、")}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "清除",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { onClear() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(allTags.toList()) { tag ->
                val isSelected = selectedTags.contains(tag)
                androidx.compose.material3.SuggestionChip(
                    onClick = { onTagClick(tag) },
                    label = {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )
            }
        }
    }
}
