package com.rywent.langrid.presentation.screens.diary.components.editor.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.rywent.langrid.presentation.screens.diary.data.DiaryBlock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun CarouselEditor(
    block: DiaryBlock.CarouselBlock,
    onRemove: () -> Unit,
    onAddImage: (String) -> Unit,
    onFocus: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.toString()?.let {
            onAddImage(it)
            onFocus()
        }
    }

    var maxW by remember(block.urls) { mutableIntStateOf(0) }
    var maxH by remember(block.urls) { mutableIntStateOf(0) }

    LaunchedEffect(block.urls) {
        if (block.urls.isEmpty()) {
            maxW = 0
            maxH = 0
            return@LaunchedEffect
        }
        var w = 0
        var h = 0
        withContext(Dispatchers.IO) {
            block.urls.forEach { url ->
                val result = context.imageLoader.execute(
                    ImageRequest.Builder(context)
                        .data(url)
                        .size(coil.size.Size.ORIGINAL)
                        .build()
                )
                val d = result.drawable ?: return@forEach
                if (d.intrinsicWidth > w) w = d.intrinsicWidth
                if (d.intrinsicHeight > h) h = d.intrinsicHeight
            }
        }
        maxW = w
        maxH = h
    }

    val density = LocalDensity.current
    val maxHdp = with(density) {
        if (maxH > 0) maxH.toDp().coerceIn(140.dp, 360.dp) else 180.dp
    }
    val aspect = if (maxW > 0 && maxH > 0) maxW.toFloat() / maxH.toFloat() else 16f / 9f

    val pageCount = block.urls.size.coerceAtLeast(1)
    val pagerState = rememberPagerState(pageCount = { pageCount })

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Carousel", style = MaterialTheme.typography.labelLarge, color = scheme.onSurfaceVariant)
            Row {
                IconButton(onClick = { galleryLauncher.launch("image/*"); onFocus() }) {
                    Icon(Icons.Rounded.Add, "Add photo")
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Rounded.Close, "Remove")
                }
            }
        }

        if (block.urls.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, scheme.outlineVariant, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Tap + to add photos", color = scheme.onSurfaceVariant.copy(alpha = 0.6f))
            }
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspect)
                    .heightIn(max = 360.dp)
                    .height(maxHdp)
            ) { page ->
                AsyncImage(
                    model = block.urls[page],
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(14.dp))
                )
            }
            Text(
                "${pagerState.currentPage + 1} / ${block.urls.size}",
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 6.dp)
            )
        }
    }
}