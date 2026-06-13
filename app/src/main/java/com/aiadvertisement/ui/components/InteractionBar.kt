package com.aiadvertisement.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.aiadvertisement.R

@Composable
fun InteractionBar(
    adId: String,
    baseLikes: Int,
    baseComments: Int,
    baseShares: Int,
    isLiked: Boolean,
    isCollected: Boolean,
    onLikeClick: (Boolean) -> Unit,
    onCollectClick: (Boolean) -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val likeCount = baseLikes + if (isLiked) 1 else 0

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LikeButton(
            adId = adId,
            isLiked = isLiked,
            count = likeCount,
            onClick = onLikeClick
        )
        Spacer(modifier = Modifier.width(24.dp))

        InteractionButton(
            iconRes = R.drawable.ic_comment,
            contentDescription = "评论",
            count = baseComments,
            onClick = {}
        )
        Spacer(modifier = Modifier.width(24.dp))

        InteractionButton(
            iconRes = if (isCollected) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark,
            contentDescription = "收藏",
            count = null,
            tint = if (isCollected) Color(0xFFF39C12) else MaterialTheme.colorScheme.onSurfaceVariant,
            onClick = { onCollectClick(!isCollected) }
        )
        Spacer(modifier = Modifier.width(24.dp))

        InteractionButton(
            iconRes = R.drawable.ic_share,
            contentDescription = "分享",
            count = baseShares,
            onClick = onShareClick
        )
    }
}

@Composable
private fun LikeButton(
    adId: String,
    isLiked: Boolean,
    count: Int,
    onClick: (Boolean) -> Unit
) {
    val heartScale by animateFloatAsState(
        targetValue = if (isLiked) 1.2f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "heart_scale"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable {
            onClick(!isLiked)
        }
    ) {
        Icon(
            painter = painterResource(
                id = if (isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart
            ),
            contentDescription = "点赞",
            tint = if (isLiked) Color(0xFFE74C3C) else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(20.dp)
                .scale(heartScale)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = formatCount(count),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InteractionButton(
    iconRes: Int,
    contentDescription: String,
    count: Int?,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        if (count != null) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = formatCount(count),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun formatCount(count: Int): String {
    return when {
        count >= 10000 -> "${count / 10000}w"
        count >= 1000 -> "${count / 1000}k"
        else -> count.toString()
    }
}
