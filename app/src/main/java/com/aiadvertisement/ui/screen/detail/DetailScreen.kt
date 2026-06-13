package com.aiadvertisement.ui.screen.detail

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.aiadvertisement.R
import com.aiadvertisement.domain.model.AdCardType
import com.aiadvertisement.ui.components.ThumbnailVideoPlayer
import com.aiadvertisement.ui.components.formatCount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    navController: NavController,
    adId: String,
    startPositionMs: Long = 0L,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(adId) {
        viewModel.loadAd(adId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("广告详情", style = MaterialTheme.typography.titleLarge)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        bottomBar = {
            if (uiState.adFeed != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ActionButton(
                        iconRes = if (uiState.isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart,
                        iconTint = if (uiState.isLiked) Color(0xFFE74C3C) else MaterialTheme.colorScheme.onSurfaceVariant,
                        label = formatCount(uiState.likeCount),
                        onClick = { uiState.adFeed?.let { viewModel.toggleLike(it.id) } }
                    )
                    ActionButton(
                        iconRes = if (uiState.isCollected) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark,
                        iconTint = if (uiState.isCollected) Color(0xFFF39C12) else MaterialTheme.colorScheme.onSurfaceVariant,
                        label = if (uiState.isCollected) "已收藏" else "收藏",
                        onClick = { uiState.adFeed?.let { viewModel.toggleCollect(it.id) } }
                    )
                    ActionButton(
                        iconRes = R.drawable.ic_share,
                        label = "分享",
                        onClick = { uiState.adFeed?.let { viewModel.toggleShare(it.id) } }
                    )
                }
            }
        }
    ) { paddingValues ->
        val ad = uiState.adFeed
        if (ad != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // 主视觉区（图片/视频）
                        when (ad.cardType) {
                            AdCardType.VIDEO -> {
                                VideoPlayerSection(
                                    videoUrl = ad.videoUrl,
                                    thumbnailUrl = ad.thumbnailUrl,
                                    startPositionMs = startPositionMs
                                )
                            }
                            else -> {
                                MainImageSection(imageUrls = ad.imageUrls)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 标题
                        Text(
                            text = ad.title,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 广告主
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ad.advertiser,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (ad.isAI) {
                                Spacer(modifier = Modifier.width(12.dp))
                                Surface(
                                    color = Color(0xFFE8D5FC),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_smart_toy),
                                            contentDescription = null,
                                            tint = Color(0xFF9C27B0),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "AI推荐",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF9C27B0)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // AI 智能标签
                        val aiTags = uiState.aiEnhance?.tags
                        if (aiTags != null && aiTags.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                aiTags.take(4).forEach { tag ->
                                    val colorPair = when (tag.category) {
                                        com.aiadvertisement.core.model.TagCategory.CATEGORY -> Color(0xFFE3F2FD) to Color(0xFF1976D2)
                                        com.aiadvertisement.core.model.TagCategory.STYLE -> Color(0xFFFCE4EC) to Color(0xFFC2185B)
                                        com.aiadvertisement.core.model.TagCategory.AUDIENCE -> Color(0xFFE8F5E9) to Color(0xFF388E3C)
                                        com.aiadvertisement.core.model.TagCategory.SCENE -> Color(0xFFFFF3E0) to Color(0xFFF57C00)
                                    }
                                    Surface(
                                        color = colorPair.first,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = tag.category.display,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = colorPair.second.copy(alpha = 0.7f),
                                                fontSize = androidx.compose.ui.unit.TextUnit(9f, androidx.compose.ui.unit.TextUnitType.Sp)
                                            )
                                            Text(
                                                text = tag.name,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = colorPair.second
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        } else if (ad.tags.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ad.tags.take(6).forEach { tag ->
                                    Surface(
                                        color = Color(0xFFE3F2FD),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Text(
                                            text = tag,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF1976D2),
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // AI 摘要
                        val aiSummary = uiState.aiEnhance?.summary
                        if (aiSummary != null || uiState.isAILoading || ad.summary != null) {
                            Surface(
                                color = Color(0xFFFFF8E1),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_smart_toy),
                                            contentDescription = null,
                                            tint = Color(0xFFFF8F00),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "AI智能摘要",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Color(0xFFFF8F00)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    when {
                                        uiState.isAILoading && aiSummary == null -> {
                                            Text(
                                                text = "AI正在生成摘要...",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color(0xFF9E9E9E)
                                            )
                                        }
                                        aiSummary != null -> {
                                            Text(
                                                text = aiSummary,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color(0xFF5D4037)
                                            )
                                        }
                                        else -> {
                                            Text(
                                                text = ad.summary ?: "",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color(0xFF5D4037)
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // 正文描述
                        Text(
                            text = ad.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // CTA 按钮
                        Surface(
                            onClick = { /* TODO: 打开链接 */ },
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = ad.ctaText,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                modifier = Modifier.padding(vertical = 14.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("加载中...", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun MainImageSection(imageUrls: List<String>) {
    val url = imageUrls.firstOrNull() ?: ""
    Box(modifier = Modifier.fillMaxWidth().height(320.dp)) {
        AsyncImage(
            model = url,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
    if (imageUrls.size > 1) {
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            imageUrls.drop(1).take(3).forEach { imgUrl ->
                AsyncImage(
                    model = imgUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
private fun VideoPlayerSection(videoUrl: String?, thumbnailUrl: String?, startPositionMs: Long = 0L) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .background(Color.Black)
    ) {
        if (!videoUrl.isNullOrEmpty()) {
            ThumbnailVideoPlayer(
                videoUrl = videoUrl,
                thumbnailUrl = thumbnailUrl,
                modifier = Modifier.fillMaxSize(),
                autoPlay = true,
                useController = true,
                startPositionMs = startPositionMs
            )
        } else if (!thumbnailUrl.isNullOrEmpty()) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun ActionButton(
    iconRes: Int,
    label: String,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
