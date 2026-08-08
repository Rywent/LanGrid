package com.rywent.langrid.presentation.screens.diary.components.blocks

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.rywent.langrid.presentation.screens.diary.components.editor.components.CroppedImage
import com.rywent.langrid.presentation.screens.diary.components.editor.components.VoiceBlockPlayer
import com.rywent.langrid.presentation.screens.diary.data.DiaryBlock
import com.rywent.langrid.presentation.screens.diary.data.SpanStyleType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun DiaryBlockView(block: DiaryBlock) {
    when (block) {
        is DiaryBlock.Heading -> HeadingBlockView(block)
        is DiaryBlock.Paragraph -> ParagraphBlockView(block)
        is DiaryBlock.ImageBlock -> ImageBlockView(block)
        is DiaryBlock.CarouselBlock -> CarouselBlockView(block)
        is DiaryBlock.TableBlock -> TableBlockView(block)
        is DiaryBlock.Divider -> DividerBlockView()
        is DiaryBlock.Quote -> QuoteBlockView(block)
        is DiaryBlock.VoiceBlock -> VoiceBlockView(block)
    }
}

@Composable
fun HeadingBlockView(block: DiaryBlock.Heading) {
    val size = when (block.level) {
        1 -> 28.sp
        2 -> 24.sp
        3 -> 20.sp
        4 -> 18.sp
        5 -> 16.sp
        else -> 14.sp
    }
    Text(
        text = block.text,
        fontSize = size,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    )
}

@Composable
fun ParagraphBlockView(block: DiaryBlock.Paragraph) {
    val scheme = MaterialTheme.colorScheme
    var revealed by remember(block.id) { mutableStateOf(false) }
    val hasSpoiler = block.spans.any { SpanStyleType.SPOILER in it.styles }

    val annotated = remember(block.text, block.spans, revealed) {
        buildAnnotatedString {
            append(block.text)
            block.spans.forEach { span ->
                val s = span.start.coerceIn(0, block.text.length)
                val e = span.end.coerceIn(0, block.text.length)
                if (s >= e) return@forEach

                val isSpoiler = SpanStyleType.SPOILER in span.styles
                val hide = isSpoiler && !revealed

                val hasU = SpanStyleType.UNDERLINE in span.styles
                val hasS = SpanStyleType.STRIKETHROUGH in span.styles
                val decoration = when {
                    hasU && hasS -> TextDecoration.Underline + TextDecoration.LineThrough
                    hasU -> TextDecoration.Underline
                    hasS -> TextDecoration.LineThrough
                    else -> null
                }

                addStyle(
                    SpanStyle(
                        fontWeight = if (SpanStyleType.BOLD in span.styles) FontWeight.Bold else null,
                        fontStyle = if (SpanStyleType.ITALIC in span.styles) FontStyle.Italic else null,
                        textDecoration = decoration,
                        color = if (hide) Color.Transparent else Color.Unspecified,
                        background = when {
                            hide -> scheme.surfaceVariant
                            isSpoiler && revealed -> Color(0xFF7C4DFF).copy(alpha = 0.12f)
                            else -> Color.Unspecified
                        }
                    ),
                    s,
                    e
                )
            }
        }
    }

    Text(
        text = annotated,
        style = MaterialTheme.typography.bodyLarge.copy(
            color = scheme.onSurface,
            lineHeight = 24.sp,
            hyphens = Hyphens.Auto,
            lineBreak = LineBreak.Paragraph
        ),
        softWrap = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .then(
                if (hasSpoiler) Modifier.clickable { revealed = !revealed }
                else Modifier
            )
    )
}

@Composable
fun ImageBlockView(block: DiaryBlock.ImageBlock) {
    val url = block.displayUrl ?: return
    CroppedImage(
        url = url,
        align = block.align,
        scale = block.cropScale,
        offsetX = block.cropOffsetX,
        offsetY = block.cropOffsetY,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(14.dp))
            .padding(vertical = 8.dp)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CarouselBlockView(block: DiaryBlock.CarouselBlock) {
    if (block.urls.isEmpty()) return
    val context = LocalContext.current
    var aspect by remember(block.urls) { mutableFloatStateOf(16f / 9f) }

    LaunchedEffect(block.urls) {
        var w = 0
        var h = 0
        withContext(Dispatchers.IO) {
            block.urls.forEach { url ->
                val result = context.imageLoader.execute(
                    ImageRequest.Builder(context).data(url).size(coil.size.Size.ORIGINAL).build()
                )
                val d = result.drawable ?: return@forEach
                if (d.intrinsicWidth > w) w = d.intrinsicWidth
                if (d.intrinsicHeight > h) h = d.intrinsicHeight
            }
        }
        if (w > 0 && h > 0) aspect = (w.toFloat() / h).coerceIn(0.5f, 2.5f)
    }

    val pagerState = rememberPagerState(pageCount = { block.urls.size })
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspect)
                .heightIn(min = 120.dp, max = 360.dp)
        ) { page ->
            AsyncImage(
                model = block.urls[page],
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp))
            )
        }
        Text(
            "${pagerState.currentPage + 1} / ${block.urls.size}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 6.dp)
        )
    }
}

@Composable
fun TableBlockView(block: DiaryBlock.TableBlock) {
    val scheme = MaterialTheme.colorScheme
    val cols = block.rows.firstOrNull()?.size ?: 0
    val widths = if (block.columnWidthsDp.size == cols && cols > 0) {
        block.columnWidthsDp
    } else {
        List(cols.coerceAtLeast(1)) { 140f }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (block.caption.isNotBlank()) {
            Text(
                text = block.caption,
                style = MaterialTheme.typography.labelLarge,
                color = scheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 6.dp, start = 2.dp)
            )
        }

        Column(
            modifier = Modifier
                .width(widths.sum().dp)
                .border(1.dp, scheme.outlineVariant, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
        ) {
            block.rows.forEachIndexed { index, row ->
                val isHeader = index == 0
                Row(modifier = Modifier.wrapContentWidth()) {
                    row.forEachIndexed { c, cell ->
                        Text(
                            text = cell.ifBlank { " " },
                            modifier = Modifier
                                .width(widths.getOrElse(c) { 140f }.dp)
                                .background(
                                    if (isHeader) scheme.surfaceContainerHighest
                                    else scheme.surface
                                )
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isHeader) FontWeight.SemiBold else FontWeight.Normal,
                            color = scheme.onSurface
                        )
                    }
                }
                if (index < block.rows.lastIndex) {
                    HorizontalDivider(color = scheme.outlineVariant)
                }
            }
        }
    }
}


@Composable
fun DividerBlockView() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 12.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
fun QuoteBlockView(block: DiaryBlock.Quote) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(scheme.surfaceContainerLow)
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(scheme.primary)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = block.text,
            style = MaterialTheme.typography.bodyLarge,
            fontStyle = FontStyle.Italic,
            color = scheme.onSurfaceVariant
        )
    }
}

@Composable
fun VoiceBlockView(block: DiaryBlock.VoiceBlock) {
    VoiceBlockPlayer(
        block = block,
        onRemove = null,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}