package com.rywent.langrid.presentation.screens.diary.components.editor

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.rywent.langrid.presentation.screens.diary.components.editor.components.formatMs
import kotlinx.coroutines.delay
import kotlin.math.max



@Composable
fun VoiceRecordBar(
    onRecorded: (path: String, durationMs: Long) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val recorder = remember { VoiceNoteRecorder(context) }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    var recording by remember { mutableStateOf(false) }
    var elapsedMs by remember { mutableLongStateOf(0L) }
    var levels by remember { mutableStateOf(List(28) { 0.15f }) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            if (recorder.start()) {
                recording = true
                elapsedMs = 0L
            }
        }
    }

    LaunchedEffect(recording) {
        if (!recording) return@LaunchedEffect
        val start = System.currentTimeMillis()
        while (recording) {
            elapsedMs = System.currentTimeMillis() - start
            val amp = recorder.amplitude().toFloat()
            val normalized = (amp / 20000f).coerceIn(0.12f, 1f)
            levels = levels.drop(1) + normalized
            delay(50)
        }
    }

    DisposableEffect(Unit) {
        onDispose { recorder.cancel() }
    }

    Surface(
        color = scheme.surfaceContainerHigh,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = {
                recording = false
                recorder.cancel()
                onCancel()
            }) {
                Icon(Icons.Rounded.Close, contentDescription = "Cancel")
            }

            if (!hasPermission) {
                Text(
                    "Microphone permission needed",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }) { Text("Allow") }
            } else if (!recording) {
                Text(
                    "Tap to start",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                FilledIconButton(
                    onClick = {
                        if (recorder.start()) {
                            recording = true
                            elapsedMs = 0L
                        }
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = scheme.error
                    )
                ) {
                    Icon(Icons.Rounded.Mic, contentDescription = "Record", tint = scheme.onError)
                }
            } else {

                val pulse = rememberInfiniteTransition(label = "pulse")
                val alpha by pulse.animateFloat(
                    initialValue = 1f,
                    targetValue = 0.35f,
                    animationSpec = infiniteRepeatable(
                        tween(600, easing = LinearEasing),
                        RepeatMode.Reverse
                    ),
                    label = "a"
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(scheme.error.copy(alpha = alpha))
                )
                Text(
                    formatMs(elapsedMs),
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )

                Waveform(
                    levels = levels,
                    color = scheme.primary,
                    modifier = Modifier
                        .weight(1f)
                        .height(28.dp)
                )

                FilledIconButton(
                    onClick = {
                        recording = false
                        val result = recorder.stop()
                        if (result != null) {
                            onRecorded(result.first, result.second)
                        } else {
                            onCancel()
                        }
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = scheme.primary
                    )
                ) {
                    Icon(Icons.Rounded.Stop, contentDescription = "Stop", tint = scheme.onPrimary)
                }
            }
        }
    }
}

@Composable
private fun Waveform(
    levels: List<Float>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val n = levels.size
        if (n == 0) return@Canvas
        val gap = 3.dp.toPx()
        val barW = ((size.width - gap * (n - 1)) / n).coerceAtLeast(2.dp.toPx())
        levels.forEachIndexed { i, level ->
            val h = max(4.dp.toPx(), size.height * level)
            val x = i * (barW + gap)
            val y = (size.height - h) / 2f
            drawRoundRect(
                color = color.copy(alpha = 0.85f),
                topLeft = Offset(x, y),
                size = Size(barW, h),
                cornerRadius = CornerRadius(barW / 2f, barW / 2f)
            )
        }
    }
}

