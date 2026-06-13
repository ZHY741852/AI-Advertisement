package com.aiadvertisement.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

@Composable
fun CachedImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    shape: Shape = RoundedCornerShape(0.dp),
    showPlaceholder: Boolean = true
) {
    val context = LocalContext.current
    var retryKey by remember { mutableStateOf(0) }

    val model = remember(imageUrl, retryKey) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .build()
    }

    val painter = rememberAsyncImagePainter(
        model = model,
        imageLoader = com.aiadvertisement.core.image.CoilConfig.getImageLoader(context),
        contentScale = contentScale
    )

    Box(modifier = modifier.clip(shape)) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = contentScale
        )

        when (val state = painter.state) {
            is AsyncImagePainter.State.Loading -> {
                if (showPlaceholder) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF0F0F0))
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
            is AsyncImagePainter.State.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF5F5F5))
                        .clickable { retryKey++ }
                ) {
                    ColumnForError(
                        modifier = Modifier.align(Alignment.Center),
                        onRetry = { retryKey++ }
                    )
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun ColumnForError(modifier: Modifier = Modifier, onRetry: () -> Unit) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.BrokenImage,
            contentDescription = null,
            tint = Color(0xFFBDBDBD),
            modifier = Modifier.size(32.dp)
        )
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "重试",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(24.dp)
                .clickable { onRetry() }
        )
    }
}
