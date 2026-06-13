package com.aiadvertisement.ui.screen.search

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.aiadvertisement.ui.components.AdFeedCard
import com.aiadvertisement.ui.components.EmptyView
import com.aiadvertisement.ui.components.LoadingView
import com.aiadvertisement.ui.navigation.navigateToDetail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val aiEnhanceMap by viewModel.aiEnhanceMap.collectAsState()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("AI 智能搜索", style = MaterialTheme.typography.titleLarge) },
                    actions = {
                        if (uiState.chatHistory.isNotEmpty()) {
                            IconButton(onClick = { viewModel.clearSearch() }) {
                                Icon(Icons.Default.Clear, contentDescription = "清除")
                            }
                        }
                    }
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.queryText,
                        onValueChange = { viewModel.updateQuery(it) },
                        placeholder = { Text("描述你想找的广告，例如：适合学生党的运动装备") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(28.dp),
                        trailingIcon = {
                            if (uiState.queryText.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "清除")
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { viewModel.executeSearch() },
                        enabled = uiState.queryText.isNotBlank() && !uiState.isAILoading
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "搜索")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isAILoading) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LoadingView()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "AI 正在分析您的搜索意图...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (uiState.searchRounds.isEmpty()) {
                EmptyView(
                    message = "用自然语言描述你想找的广告，例如：\n「性价比高的运动鞋」\n「适合送礼的精品」\n「学生党必备好物」"
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 每轮搜索：展开为 用户query -> AI回复 -> 该轮匹配广告卡片
                    uiState.searchRounds.forEachIndexed { roundIdx, round ->
                        val roundKeyPrefix = "round_$roundIdx"

                        item(key = roundKeyPrefix + "_user") {
                            ChatBubble(
                                message = SearchChatMessage(role = "user", content = round.query)
                            )
                        }

                        round.aiResponse?.let { resp ->
                            item(key = roundKeyPrefix + "_ai") {
                                ChatBubble(
                                    message = SearchChatMessage(role = "assistant", content = resp)
                                )
                            }
                        }

                        if (round.results.isNotEmpty()) {
                            item(key = roundKeyPrefix + "_header") {
                                Text(
                                    "匹配结果 (${round.results.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            items(
                                round.results,
                                key = { roundKeyPrefix + "_ad_" + it.id }) { adFeed ->
                                AdFeedCard(
                                    adFeed = adFeed,
                                    onCardClick = { feed, _ ->
                                        navController.navigateToDetail(feed.id)
                                    },
                                    onExposure = {},
                                    aiEnhance = aiEnhanceMap[adFeed.id],
                                    onGenerateAI = { feed ->
                                        viewModel.generateAIEnhance(feed)
                                    }
                                )
                            }
                        } else if (round.aiResponse != null) {
                            item(key = roundKeyPrefix + "_empty") {
                                Text(
                                    "没有找到匹配的广告",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }

                        item(key = roundKeyPrefix + "_sep") {
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(16.dp)
                            )
                        }
                    }
                }
            }

            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = Color(0xFFD32F2F),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ChatBubble(message: SearchChatMessage) {
    val isUser = message.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) MaterialTheme.colorScheme.primary else Color(0xFFF5F5F5)
            ),
            shape = RoundedCornerShape(
                topStart = if (isUser) 16.dp else 4.dp,
                topEnd = if (isUser) 4.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            )
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}
