package com.rywent.langrid.presentation.screens.words.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.FormatListNumbered
import androidx.compose.material.icons.rounded.FormatListNumberedRtl
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SortByAlpha
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class SortCategory {
    THEMES,
    FOLDERS,
    WORDS
}

enum class GeneralSortOption {
    DATE_DESC,
    DATE_ASC,
    COUNT_DESC,
    COUNT_ASC,
    NAME_ASC,
    NAME_DESC,
    TRANSLATION_ASC
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortBottomSheet(
    category: SortCategory,
    currentOption: GeneralSortOption?,
    onDismiss: () -> Unit,
    onOptionSelected: (GeneralSortOption?) -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = scheme.surfaceContainerLow,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (category) {
                        SortCategory.THEMES -> "Sort themes"
                        SortCategory.FOLDERS -> "Sort folders"
                        SortCategory.WORDS -> "Sort words"
                    },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = scheme.onSurface
                )

                if (currentOption != null) {
                    TextButton(onClick = { onOptionSelected(null) }) {
                        Text("Reset")
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = scheme.outlineVariant.copy(alpha = 0.5f)
            )

            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                when (category) {
                    SortCategory.THEMES, SortCategory.FOLDERS -> {
                        SortSectionLabel("Date")
                        SortOptionItem(
                            icon = Icons.Rounded.Schedule,
                            title = "Newest first",
                            selected = currentOption == GeneralSortOption.DATE_DESC,
                            onClick = { onOptionSelected(GeneralSortOption.DATE_DESC) }
                        )
                        SortOptionItem(
                            icon = Icons.Rounded.History,
                            title = "Oldest first",
                            selected = currentOption == GeneralSortOption.DATE_ASC,
                            onClick = { onOptionSelected(GeneralSortOption.DATE_ASC) }
                        )

                        SortSectionLabel("Count")
                        SortOptionItem(
                            icon = Icons.Rounded.FormatListNumbered,
                            title = "Most words",
                            selected = currentOption == GeneralSortOption.COUNT_DESC,
                            onClick = { onOptionSelected(GeneralSortOption.COUNT_DESC) }
                        )
                        SortOptionItem(
                            icon = Icons.Rounded.FormatListNumberedRtl,
                            title = "Fewest words",
                            selected = currentOption == GeneralSortOption.COUNT_ASC,
                            onClick = { onOptionSelected(GeneralSortOption.COUNT_ASC) }
                        )

                        SortSectionLabel("Name")
                        SortOptionItem(
                            icon = Icons.Rounded.SortByAlpha,
                            title = "A – Z",
                            selected = currentOption == GeneralSortOption.NAME_ASC,
                            onClick = { onOptionSelected(GeneralSortOption.NAME_ASC) }
                        )
                        SortOptionItem(
                            icon = Icons.Rounded.SortByAlpha,
                            title = "Z – A",
                            selected = currentOption == GeneralSortOption.NAME_DESC,
                            onClick = { onOptionSelected(GeneralSortOption.NAME_DESC) }
                        )
                    }

                    SortCategory.WORDS -> {
                        SortSectionLabel("Date")
                        SortOptionItem(
                            icon = Icons.Rounded.Schedule,
                            title = "Newest added",
                            selected = currentOption == GeneralSortOption.DATE_DESC,
                            onClick = { onOptionSelected(GeneralSortOption.DATE_DESC) }
                        )

                        SortSectionLabel("Term")
                        SortOptionItem(
                            icon = Icons.Rounded.SortByAlpha,
                            title = "A – Z",
                            selected = currentOption == GeneralSortOption.NAME_ASC,
                            onClick = { onOptionSelected(GeneralSortOption.NAME_ASC) }
                        )
                        SortOptionItem(
                            icon = Icons.Rounded.SortByAlpha,
                            title = "Z – A",
                            selected = currentOption == GeneralSortOption.NAME_DESC,
                            onClick = { onOptionSelected(GeneralSortOption.NAME_DESC) }
                        )

                        SortSectionLabel("Translation")
                        SortOptionItem(
                            icon = Icons.Rounded.Translate,
                            title = "A – Z",
                            selected = currentOption == GeneralSortOption.TRANSLATION_ASC,
                            onClick = { onOptionSelected(GeneralSortOption.TRANSLATION_ASC) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SortSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp, start = 4.dp)
    )
}

@Composable
fun SortOptionItem(
    icon: ImageVector,
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) scheme.primary else scheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) scheme.primary else scheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = scheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}