package com.aiadvertisement.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import coil.compose.AsyncImage
import com.aiadvertisement.R
import com.aiadvertisement.core.model.AIAdEnhance
import com.aiadvertisement.core.model.AITag
import com.aiadvertisement.core.model.TagCategory
import com.aiadvertisement.domain.model.AdCardType
import com.aiadvertisement.domain.model.AdFeed

@Composable
fun AdFeedCard(
    adFeed: AdFeed,
    onCardClick: (AdFeed, Long) -> Unit,
    onExposure: (String) -> Unit,
    aiEnhance: AIAdEnhance? = null,
    onGenerateAI: (AdFeed) -> Unit = {},
    onTagClick: (String) -> Unit = {},
    selectedTags: Set<String> = emptySet(),
    modifier: Modifier = Modifier
) {
    var isLiked by remember(adFeed.id) { mutableStateOf(false) }
    var isCollected by remember(adFeed.id) { mutableStateOf(false) }
    var currentPosition by remember(adFeed.id) { mutableStateOf(0L) }

    LaunchedEffect(adFeed.id) {
        onExposure(adFeed.id)
        if (aiEnhance == null) {
            onGenerateAI(adFeed)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick(adFeed, currentPosition) },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            when (adFeed.cardType) {
                AdCardType.BIG_IMAGE -> BigImageCardContent(adFeed)
                AdCardType.SMALL_IMAGE -> SmallImageCardContent(adFeed)
                AdCardType.VIDEO -> VideoCardContent(adFeed, onPositionChanged = { pos -> currentPosition = pos })
            }

            AISummaryRow(summary = aiEnhance?.summary ?: adFeed.summary, isLoading = aiEnhance == null && adFeed.summary == null)

            AITagRow(
                tags = aiEnhance?.tags,
                fallbackTags = adFeed.tags,
                onTagClick = onTagClick,
                selectedTags = selectedTags
            )

            InteractionBar(
                adId = adFeed.id,
                baseLikes = adFeed.likes,
                baseComments = adFeed.comments,
                baseShares = adFeed.shares,
                isLiked = isLiked,
                isCollected = isCollected,
                onLikeClick = { liked ->
                    isLiked = liked
                },
                onCollectClick = { collected ->
                    isCollected = collected
                },
                onShareClick = { }
            )
        }
    }
}

@Composable
private fun BigImageCardContent(adFeed: AdFeed) {
    Column {
        CachedImage(
            imageUrl = adFeed.imageUrls.firstOrNull(),
            contentDescription = adFeed.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = adFeed.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = adFeed.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        AdvertiserRow(advertiser = adFeed.advertiser, isAI = adFeed.isAI)
    }
}

@Composable
private fun SmallImageCardContent(adFeed: AdFeed) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = adFeed.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = adFeed.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            AdvertiserRow(advertiser = adFeed.advertiser, isAI = adFeed.isAI)
        }
        Spacer(modifier = Modifier.width(12.dp))
        CachedImage(
            imageUrl = adFeed.imageUrls.firstOrNull(),
            contentDescription = adFeed.title,
            modifier = Modifier
                .size(110.dp, 110.dp),
            shape = RoundedCornerShape(8.dp),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun VideoCardContent(adFeed: AdFeed, onPositionChanged: ((Long) -> Unit)? = null) {
    var isPlaying by remember(adFeed.id) { mutableStateOf(false) }
    val hasValidVideo = !adFeed.videoUrl.isNullOrBlank() && android.net.Uri.parse(adFeed.videoUrl).scheme != null

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Color.Black)
        ) {
            if (!isPlaying || !hasValidVideo) {
                CachedImage(
                    imageUrl = adFeed.thumbnailUrl ?: adFeed.imageUrls.firstOrNull(),
                    contentDescription = adFeed.title,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
                if (hasValidVideo) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .clickable { isPlaying = true }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_play),
                            contentDescription = "播放",
                            tint = Color.White,
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.Center)
                        )
                    }
                }
            } else {
                ThumbnailVideoPlayer(
                    videoUrl = adFeed.videoUrl ?: "",
                    thumbnailUrl = adFeed.thumbnailUrl,
                    modifier = Modifier.fillMaxWidth(),
                    onPositionChanged = onPositionChanged
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = adFeed.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = adFeed.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        AdvertiserRow(advertiser = adFeed.advertiser, isAI = adFeed.isAI)
    }
}

@Composable
private fun AdvertiserRow(advertiser: String, isAI: Boolean) {
    Row(
        modifier = Modifier.padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = advertiser,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
        if (isAI) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = Color(0xFFE8D5FC),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_smart_toy),
                        contentDescription = null,
                        tint = Color(0xFF9C27B0),
                        modifier = Modifier.size(12.dp)
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
}

@Composable
private fun AISummaryRow(summary: String?, isLoading: Boolean = false) {
    if (summary == null && !isLoading) return
    Spacer(modifier = Modifier.height(6.dp))
    Surface(
        color = Color(0xFFFFF8E1),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_smart_toy),
                    contentDescription = null,
                    tint = Color(0xFFFF8F00),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "AI智能摘要",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFF8F00)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            if (isLoading) {
                Text(
                    text = "正在生成中...",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9E9E9E)
                )
            } else {
                Text(
                    text = summary ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF5D4037)
                )
            }
        }
    }
}

@Composable
private fun AITagRow(
    tags: List<AITag>?,
    fallbackTags: List<String>,
    onTagClick: (String) -> Unit = {},
    selectedTags: Set<String> = emptySet()
) {
    val hasTags = !tags.isNullOrEmpty() || fallbackTags.isNotEmpty()
    if (!hasTags) return

    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (!tags.isNullOrEmpty()) {
            tags.take(4).forEach { tag ->
                CategoryTag(
                    tag = tag,
                    isSelected = selectedTags.contains(tag.name),
                    onClick = { onTagClick(tag.name) }
                )
            }
        } else {
            fallbackTags.take(4).forEach { tag ->
                SimpleTag(
                    tag = tag,
                    isSelected = selectedTags.contains(tag),
                    onClick = { onTagClick(tag) }
                )
            }
        }
    }
}

@Composable
private fun CategoryTag(tag: AITag, isSelected: Boolean = false, onClick: () -> Unit = {}) {
    val colorPair = when (tag.category) {
        TagCategory.CATEGORY -> Color(0xFFE3F2FD) to Color(0xFF1976D2)
        TagCategory.STYLE -> Color(0xFFFCE4EC) to Color(0xFFC2185B)
        TagCategory.AUDIENCE -> Color(0xFFE8F5E9) to Color(0xFF388E3C)
        TagCategory.SCENE -> Color(0xFFFFF3E0) to Color(0xFFF57C00)
    }
    val bgColor = if (isSelected) colorPair.second else colorPair.first
    val textColor = if (isSelected) Color.White else colorPair.second
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = tag.category.display,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = androidx.compose.ui.unit.TextUnit(9f, androidx.compose.ui.unit.TextUnitType.Sp)
                ),
                color = textColor.copy(alpha = if (isSelected) 0.85f else 0.7f)
            )
            Text(
                text = tag.name,
                style = MaterialTheme.typography.labelSmall,
                color = textColor
            )
        }
    }
}

@Composable
private fun SimpleTag(tag: String, isSelected: Boolean = false, onClick: () -> Unit = {}) {
    val bgColor = if (isSelected) Color(0xFF1976D2) else Color(0xFFE3F2FD)
    val textColor = if (isSelected) Color.White else Color(0xFF1976D2)
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = tag,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}
