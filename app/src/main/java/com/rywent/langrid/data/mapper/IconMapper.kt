package com.rywent.langrid.data.mapper

import androidx.compose.ui.graphics.vector.ImageVector

fun ImageVector.toPath(): String = AppIcon.fromIcon(this)

fun String.toIcon(): ImageVector = AppIcon.fromKey(this)