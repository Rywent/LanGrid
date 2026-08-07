package com.rywent.langrid.presentation.screens.words.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rywent.langrid.presentation.screens.words.WordItem

@Composable
fun SearchResultWordCard(
    word: WordItem,
    path: String,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = scheme.surfaceContainerHigh,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(scheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                if (!word.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = word.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    val letter = word.term.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(scheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = letter,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = scheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = word.term,
                        style = MaterialTheme.typography.titleMedium,
                        color = scheme.onSurface
                    )
                    if (!word.transcription.isNullOrBlank()) {
                        Text(
                            text = word.transcription,
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.primary
                        )
                    }
                }

                val displaySentence = word.exampleSentence ?: word.translation
                if (!displaySentence.isNullOrBlank()) {
                    Text(
                        text = displaySentence,
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }

                if (path.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = path,
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.outline
                    )
                }
            }
        }
    }
}