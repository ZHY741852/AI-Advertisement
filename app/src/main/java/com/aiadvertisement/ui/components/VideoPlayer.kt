package com.aiadvertisement.ui.components

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView

@Composable
fun ThumbnailVideoPlayer(
    videoUrl: String,
    thumbnailUrl: String?,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = true,
    useController: Boolean = true,
    startPositionMs: Long = 0L,
    onPositionChanged: ((Long) -> Unit)? = null
) {
    val context = LocalContext.current
    val hasValidUrl = videoUrl.isNotBlank()
        && videoUrl != "null"
        && android.net.Uri.parse(videoUrl).scheme != null

    if (hasValidUrl) {
        val playerHolder = remember(videoUrl) { mutableStateOf<ExoPlayer?>(null) }

        DisposableEffect(videoUrl) {
            val exoPlayer = ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(videoUrl))
                prepare()
                playWhenReady = autoPlay
                if (startPositionMs > 0) {
                    seekTo(startPositionMs)
                }
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        if (state == Player.STATE_READY) {
                            onPositionChanged?.invoke(currentPosition)
                        }
                    }
                })
            }
            playerHolder.value = exoPlayer
            onDispose {
                playerHolder.value = null
                exoPlayer.stop()
                exoPlayer.clearMediaItems()
                exoPlayer.release()
            }
        }

        AndroidView(
            modifier = modifier,
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.useController = useController
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { playerView ->
                playerView.player = playerHolder.value
            }
        )
    } else {
        if (thumbnailUrl != null) {
            CachedImage(
                imageUrl = thumbnailUrl,
                contentDescription = null,
                modifier = modifier,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        }
    }
}

@Composable
fun VideoPlayerView(
    videoUrl: String,
    thumbnailUrl: String?,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = false,
    useController: Boolean = true,
    startPositionMs: Long = 0L,
    onStateChange: ((Boolean) -> Unit)? = null
) {
    val context = LocalContext.current
    val hasValidUrl = videoUrl.isNotBlank()
        && videoUrl != "null"
        && android.net.Uri.parse(videoUrl).scheme != null

    if (hasValidUrl) {
        val playerHolder = remember(videoUrl) { mutableStateOf<ExoPlayer?>(null) }

        DisposableEffect(videoUrl) {
            val exoPlayer = ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(videoUrl))
                prepare()
                playWhenReady = autoPlay
                if (startPositionMs > 0) {
                    seekTo(startPositionMs)
                }
            }
            playerHolder.value = exoPlayer
            onStateChange?.invoke(true)
            onDispose {
                onStateChange?.invoke(false)
                playerHolder.value = null
                exoPlayer.stop()
                exoPlayer.clearMediaItems()
                exoPlayer.release()
            }
        }

        AndroidView(
            modifier = modifier,
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.useController = useController
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { playerView ->
                playerView.player = playerHolder.value
            }
        )
    } else {
        if (thumbnailUrl != null) {
            CachedImage(
                imageUrl = thumbnailUrl,
                contentDescription = null,
                modifier = modifier,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        }
    }
}