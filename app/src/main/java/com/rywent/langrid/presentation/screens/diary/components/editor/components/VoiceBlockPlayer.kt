package com.rywent.langrid.presentation.screens.diary.components.editor.components

import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.rywent.langrid.presentation.screens.diary.data.DiaryBlock
import kotlinx.coroutines.delay

public fun formatMs(ms: Long): String {
    val totalSec = (ms / 1000).toInt().coerceAtLeast(0)
    val m = totalSec / 60
    val s = totalSec % 60
    return "%d:%02d".format(m, s)
}
public @Composable
fun VoiceBlockPlayer(
    block: DiaryBlock.VoiceBlock,
    onRemove: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    var playing by remember { mutableStateOf(false) }
    var positionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(block.durationMs) }
    var player by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(block.localPath) {
        onDispose {
            player?.release()
            player = null
            playing = false
        }
    }

    fun toggle() {
        if (playing) {
            player?.pause()
            playing = false
            return
        }
        try {
            if (player == null) {
                player = MediaPlayer().apply {
                    setDataSource(block.localPath)
                    setOnPreparedListener {
                        durationMs = it.duration.toLong().coerceAtLeast(block.durationMs)
                        it.start()
                        playing = true
                    }
                    setOnCompletionListener {
                        playing = false
                        positionMs = 0L
                        seekTo(0)
                    }
                    prepareAsync()
                }
            } else {
                player?.start()
                playing = true
            }
        } catch (_: Exception) {
            playing = false
        }
    }

    LaunchedEffect(playing) {
        while (playing) {
            positionMs = player?.currentPosition?.toLong() ?: 0L
            delay(80)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(scheme.primaryContainer.copy(alpha = 0.45f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilledIconButton(
            onClick = { toggle() },
            modifier = Modifier.size(40.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = scheme.primary
            )
        ) {
            Icon(
                if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = null,
                tint = scheme.onPrimary
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            val progress = if (durationMs > 0) {
                (positionMs.toFloat() / durationMs).coerceIn(0f, 1f)
            } else 0f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = scheme.primary,
                trackColor = scheme.onPrimaryContainer.copy(alpha = 0.2f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (playing || positionMs > 0) {
                    "${formatMs(positionMs)} / ${formatMs(durationMs)}"
                } else {
                    formatMs(durationMs)
                },
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onPrimaryContainer
            )
        }

        if (onRemove != null) {
            IconButton(onClick = onRemove) {
                Icon(Icons.Rounded.Close, contentDescription = "Remove")
            }
        }
    }
}