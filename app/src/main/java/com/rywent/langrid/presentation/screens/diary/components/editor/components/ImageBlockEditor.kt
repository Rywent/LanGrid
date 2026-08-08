package com.rywent.langrid.presentation.screens.diary.components.editor.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FormatAlignCenter
import androidx.compose.material.icons.rounded.FormatAlignLeft
import androidx.compose.material.icons.rounded.FormatAlignRight
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.rywent.langrid.presentation.screens.diary.data.DiaryBlock
import com.rywent.langrid.presentation.screens.diary.data.ImageAlign

@Composable
fun ImageBlockEditor(
    block: DiaryBlock.ImageBlock,
    onUpdate: (DiaryBlock) -> Unit,
    onRemove: () -> Unit,
    onFocus: () -> Unit
) {
    var showEditor by remember { mutableStateOf(false) }
    val url = block.displayUrl

    Box(modifier = Modifier.fillMaxWidth()) {
        if (url != null) {
            CroppedImage(
                url = url,
                align = block.align,
                scale = block.cropScale,
                offsetX = block.cropOffsetX,
                offsetY = block.cropOffsetY,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable {
                        onFocus()
                        showEditor = true
                    }
            )
        }
        IconButton(
            onClick = onRemove,
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(Icons.Rounded.Close, contentDescription = "Remove")
        }
    }

    if (showEditor && url != null) {
        GalleryCropDialog(
            url = url,
            initialAlign = block.align,
            initialScale = block.cropScale.coerceAtLeast(1f),
            initialOffsetX = block.cropOffsetX,
            initialOffsetY = block.cropOffsetY,
            onDismiss = { showEditor = false },
            onApply = { align, scale, ox, oy ->
                onUpdate(
                    block.copy(
                        align = align,
                        cropScale = scale,
                        cropOffsetX = ox,
                        cropOffsetY = oy
                    )
                )
                showEditor = false
            }
        )
    }
}

@Composable
public fun GalleryCropDialog(
    url: String,
    initialAlign: ImageAlign,
    initialScale: Float,
    initialOffsetX: Float,
    initialOffsetY: Float,
    onDismiss: () -> Unit,
    onApply: (ImageAlign, Float, Float, Float) -> Unit
) {
    var align by remember { mutableStateOf(initialAlign) }
    var scale by remember { mutableFloatStateOf(initialScale.coerceIn(1f, 4f)) }
    var offsetX by remember { mutableFloatStateOf(initialOffsetX.coerceIn(-1f, 1f)) }
    var offsetY by remember { mutableFloatStateOf(initialOffsetY.coerceIn(-1f, 1f)) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true
        )
    ) {
        Surface(
            color = Color.Black,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.White)
                    }
                    Text(
                        text = "Crop",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextButton(
                        onClick = { onApply(align, scale, offsetX, offsetY) }
                    ) {
                        Text("Done", color = MaterialTheme.colorScheme.primary)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .aspectRatio(4f / 3f)
                            .clip(RoundedCornerShape(2.dp))
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(1f, 4f)
                                    val panScale = 2.2f / scale
                                    offsetX = (offsetX + pan.x / size.width * panScale)
                                        .coerceIn(-1f, 1f)
                                    offsetY = (offsetY + pan.y / size.height * panScale)
                                        .coerceIn(-1f, 1f)
                                }
                            }
                    ) {
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    val s = scale.coerceIn(1f, 4f)
                                    scaleX = s
                                    scaleY = s
                                    translationX = offsetX * size.width * 0.4f
                                    translationY = offsetY * size.height * 0.4f
                                }
                        )
                        CropGridOverlay(modifier = Modifier.fillMaxSize())
                    }

                    CropDimOverlay(
                        modifier = Modifier.fillMaxSize(),
                        frameAspect = 4f / 3f
                    )
                }

                Text(
                    text = "Alignment in post",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(start = 20.dp, bottom = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .navigationBarsPadding()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
                ) {
                    AlignChip(
                        selected = align == ImageAlign.LEFT,
                        label = "Left",
                        icon = Icons.Rounded.FormatAlignLeft,
                        onClick = { align = ImageAlign.LEFT }
                    )
                    AlignChip(
                        selected = align == ImageAlign.CENTER,
                        label = "Center",
                        icon = Icons.Rounded.FormatAlignCenter,
                        onClick = { align = ImageAlign.CENTER }
                    )
                    AlignChip(
                        selected = align == ImageAlign.RIGHT,
                        label = "Right",
                        icon = Icons.Rounded.FormatAlignRight,
                        onClick = { align = ImageAlign.RIGHT }
                    )
                }
            }
        }
    }
}

@Composable
private fun AlignChip(
    selected: Boolean,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = {
            Icon(icon, null, Modifier.size(16.dp))
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color.White.copy(alpha = 0.12f),
            labelColor = Color.White,
            iconColor = Color.White,
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
private fun CropGridOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val lineColor = Color.White.copy(alpha = 0.55f)
        val stroke = 1.dp.toPx()
        val border = 2.dp.toPx()

        drawRect(
            color = Color.White,
            topLeft = Offset.Zero,
            size = size,
            style = Stroke(width = border)
        )


        drawLine(lineColor, Offset(w / 3f, 0f), Offset(w / 3f, h), stroke)
        drawLine(lineColor, Offset(2f * w / 3f, 0f), Offset(2f * w / 3f, h), stroke)

        drawLine(lineColor, Offset(0f, h / 3f), Offset(w, h / 3f), stroke)
        drawLine(lineColor, Offset(0f, 2f * h / 3f), Offset(w, 2f * h / 3f), stroke)


        val corner = 22.dp.toPx()
        val thick = 3.dp.toPx()
        val c = Color.White
        // TL
        drawLine(c, Offset(0f, 0f), Offset(corner, 0f), thick)
        drawLine(c, Offset(0f, 0f), Offset(0f, corner), thick)
        // TR
        drawLine(c, Offset(w, 0f), Offset(w - corner, 0f), thick)
        drawLine(c, Offset(w, 0f), Offset(w, corner), thick)
        // BL
        drawLine(c, Offset(0f, h), Offset(corner, h), thick)
        drawLine(c, Offset(0f, h), Offset(0f, h - corner), thick)
        // BR
        drawLine(c, Offset(w, h), Offset(w - corner, h), thick)
        drawLine(c, Offset(w, h), Offset(w, h - corner), thick)
    }
}


@Composable
public fun CropDimOverlay(
    modifier: Modifier = Modifier,
    frameAspect: Float
) {
    Canvas(modifier = modifier) {
        val canvasW = size.width
        val canvasH = size.height

        val frameW = canvasW
        val frameH = frameW / frameAspect
        val left = 0f
        val top = ((canvasH - frameH) / 2f).coerceAtLeast(0f)
        val right = canvasW
        val bottom = (top + frameH).coerceAtMost(canvasH)

        val dim = Color.Black.copy(alpha = 0.62f)

        drawRect(dim, Offset(0f, 0f), Size(canvasW, top))
        drawRect(dim, Offset(0f, bottom), Size(canvasW, canvasH - bottom))
        if (frameW < canvasW) {
            val side = (canvasW - frameW) / 2f
            drawRect(dim, Offset(0f, top), Size(side, frameH))
            drawRect(dim, Offset(right - side, top), Size(side, frameH))
        }
    }
}

@Composable
fun CroppedImage(
    url: String,
    align: ImageAlign,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    modifier: Modifier = Modifier
) {
    val alignment = when (align) {
        ImageAlign.LEFT -> Alignment.CenterStart
        ImageAlign.CENTER -> Alignment.Center
        ImageAlign.RIGHT -> Alignment.CenterEnd
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp, max = 320.dp)
            .clip(RoundedCornerShape(14.dp)),
        contentAlignment = alignment
    ) {
        AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    val s = scale.coerceIn(1f, 4f)
                    scaleX = s
                    scaleY = s
                    translationX = offsetX * size.width * 0.4f
                    translationY = offsetY * size.height * 0.4f
                }
        )
    }
}

@Composable
public fun ImageCropDialog(
    url: String,
    initialAlign: ImageAlign,
    initialScale: Float,
    initialOffsetX: Float,
    initialOffsetY: Float,
    onDismiss: () -> Unit,
    onApply: (ImageAlign, Float, Float, Float) -> Unit
) {
    var align by remember { mutableStateOf(initialAlign) }
    var scale by remember { mutableFloatStateOf(initialScale.coerceIn(1f, 3f)) }
    var ox by remember { mutableFloatStateOf(initialOffsetX) }
    var oy by remember { mutableFloatStateOf(initialOffsetY) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .wrapContentHeight(),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Edit photo", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 3f)
                                ox = (ox + pan.x / size.width * 2f).coerceIn(-1f, 1f)
                                oy = (oy + pan.y / size.height * 2f).coerceIn(-1f, 1f)
                            }
                        }
                ) {
                    CroppedImage(
                        url = url,
                        align = align,
                        scale = scale,
                        offsetX = ox,
                        offsetY = oy,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Text(
                    "Pinch to zoom · drag to pan",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(Modifier.height(12.dp))
                Text("Alignment", style = MaterialTheme.typography.labelLarge)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    FilterChip(
                        selected = align == ImageAlign.LEFT,
                        onClick = { align = ImageAlign.LEFT },
                        label = { Text("Left") },
                        leadingIcon = { Icon(Icons.Rounded.FormatAlignLeft, null, Modifier.size(16.dp)) }
                    )
                    FilterChip(
                        selected = align == ImageAlign.CENTER,
                        onClick = { align = ImageAlign.CENTER },
                        label = { Text("Center") },
                        leadingIcon = { Icon(Icons.Rounded.FormatAlignCenter, null, Modifier.size(16.dp)) }
                    )
                    FilterChip(
                        selected = align == ImageAlign.RIGHT,
                        onClick = { align = ImageAlign.RIGHT },
                        label = { Text("Right") },
                        leadingIcon = { Icon(Icons.Rounded.FormatAlignRight, null, Modifier.size(16.dp)) }
                    )
                }

                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { onApply(align, scale, ox, oy) }) { Text("Apply") }
                }
            }
        }
    }
}