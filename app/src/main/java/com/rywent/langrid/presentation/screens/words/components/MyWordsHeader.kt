package com.rywent.langrid.presentation.screens.words.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.rywent.langrid.R

@OptIn(ExperimentalTextApi::class)
@Composable
fun MyWordsHeader(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "My words",
            style = MaterialTheme.typography.displayLarge.copy(
                fontFamily = FontFamily(
                    Font(
                        resId = R.font.gflex_variable,
                        variationSettings = FontVariation.Settings(
                            FontVariation.weight(750),
                            FontVariation.width(155f)
                        )
                    )
                ),
                fontWeight = FontWeight.Black,
                fontSize = 28.sp,
                lineHeight = 10.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

    }
}