package com.rywent.langrid.presentation.screens.words.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FolderBreadcrumbRow(
    path: List<String>,
    modifier: Modifier = Modifier,
    onItemClick: (Int) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val scrollState = rememberScrollState()

    LaunchedEffect(path.size) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        path.forEachIndexed { index, folderName ->
            val isLast = index == path.size - 1

            Surface(
                shape = RoundedCornerShape(100.dp),
                color = if (isLast) scheme.primaryContainer else scheme.surfaceContainerHigh,
                contentColor = if (isLast) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
                modifier = Modifier
                    .height(32.dp)
                    .then(
                        if (!isLast) {
                            Modifier.clickable { onItemClick(index) }
                        } else {
                            Modifier
                        }
                    )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (index == 0) {
                        Icon(
                            imageVector = Icons.Rounded.Folder,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (isLast) scheme.primary else scheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = folderName,
                        fontSize = 12.sp,
                        fontWeight = if (isLast) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }

            if (!isLast) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = scheme.outline.copy(alpha = 0.7f)
                )
            }
        }
    }
}