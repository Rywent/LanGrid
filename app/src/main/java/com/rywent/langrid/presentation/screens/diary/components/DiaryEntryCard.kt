package com.rywent.langrid.presentation.screens.diary.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rywent.langrid.presentation.screens.diary.data.DiaryEntry
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DiaryEntryCard(
    entry: DiaryEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val date = runCatching { LocalDate.parse(entry.date) }.getOrNull()
    val day = date?.dayOfMonth?.toString() ?: "—"
    val month = date
        ?.format(DateTimeFormatter.ofPattern("MMM", Locale.getDefault()))
        ?.replaceFirstChar { it.uppercase() }
        ?: ""

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = scheme.surfaceContainerHigh,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(44.dp)
            ) {
                Text(
                    text = day,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = scheme.primary
                )
                Text(
                    text = month,
                    fontSize = 12.sp,
                    color = scheme.onSurfaceVariant
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                if (!entry.title.isNullOrBlank()) {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                }
                Text(
                    text = entry.plainPreview(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (entry.imageCount() > 0) {
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            label = { Text("${entry.imageCount()} photo") },
                            leadingIcon = {
                                Icon(Icons.Rounded.Image, null, Modifier.size(16.dp))
                            }
                        )
                    }
                    if (entry.hasTable()) {
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            label = { Text("Table") },
                            leadingIcon = {
                                Icon(Icons.Rounded.GridOn, null, Modifier.size(16.dp))
                            }
                        )
                    }
                }
            }

            val imageUrl = entry.firstImageUrl()
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(scheme.surfaceContainerHighest)
                )
            }
        }
    }
}