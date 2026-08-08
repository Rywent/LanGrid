package com.rywent.langrid.presentation.screens.diary.subScreens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rywent.langrid.presentation.screens.diary.components.blocks.DiaryBlockView
import com.rywent.langrid.presentation.screens.diary.data.DiaryEntry
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryEntryDetailScreen(
    entry: DiaryEntry,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val dateLabel by remember {
        derivedStateOf {
            runCatching {
                LocalDate.parse(entry.date)
                    .format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", configuration.locales[0]))
            }.getOrElse { entry.date }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text(dateLabel, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Rounded.Edit, "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Rounded.Delete, "Delete")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (!entry.title.isNullOrBlank()) {
                Text(
                    text = entry.title,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            entry.blocks.forEachIndexed { index, block ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(280, delayMillis = index * 35)) +
                            slideInVertically(
                                initialOffsetY = { it / 6 },
                                animationSpec = tween(280, delayMillis = index * 35)
                            )
                ) {
                    DiaryBlockView(block)
                }
            }
            Spacer(Modifier.height(48.dp))
        }
    }
}