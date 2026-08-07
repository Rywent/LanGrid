package com.rywent.langrid.presentation.screens.words.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class IconCategory(
    val title: String,
    val icons: List<ImageVector>
)

@Composable
fun FullIconPicker(
    selectedIcon: ImageVector,
    onIconSelected: (ImageVector) -> Unit,
    onDismiss: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val accent = scheme.primary

    val categories = remember {
        listOf(
            IconCategory(
                "Learning & Language",
                listOf(
                    Icons.Default.School, Icons.Default.Language, Icons.Default.Translate,
                    Icons.AutoMirrored.Filled.MenuBook, Icons.Default.AutoStories, Icons.Default.Quiz,
                    Icons.Default.HistoryEdu, Icons.Default.LocalLibrary, Icons.Default.RecordVoiceOver,
                    Icons.Default.Headphones, Icons.Default.Mic, Icons.Default.Forum
                )
            ),
            IconCategory(
                "Home & Daily",
                listOf(
                    Icons.Default.Home, Icons.Default.House, Icons.Default.Apartment,
                    Icons.Default.Kitchen, Icons.Default.Bed, Icons.Default.Weekend,
                    Icons.Default.Chair, Icons.Default.Light, Icons.Default.DoorFront,
                    Icons.Default.Garage, Icons.Default.Yard, Icons.Default.Pets
                )
            ),
            IconCategory(
                "Food & Cooking",
                listOf(
                    Icons.Default.Restaurant, Icons.Default.LocalCafe, Icons.Default.Coffee,
                    Icons.Default.RamenDining, Icons.Default.LunchDining, Icons.Default.DinnerDining,
                    Icons.Default.BakeryDining, Icons.Default.Fastfood, Icons.Default.Icecream,
                    Icons.Default.Egg, Icons.Default.Cookie, Icons.Default.LocalPizza
                )
            ),
            IconCategory(
                "Travel & Places",
                listOf(
                    Icons.Default.Flight, Icons.Default.Train, Icons.Default.DirectionsCar,
                    Icons.Default.DirectionsBus, Icons.Default.Map, Icons.Default.Place,
                    Icons.Default.Hotel, Icons.Default.Luggage, Icons.Default.BeachAccess,
                    Icons.Default.Hiking, Icons.Default.Public, Icons.Default.Explore
                )
            ),
            IconCategory(
                "Work & Study",
                listOf(
                    Icons.Default.Work, Icons.Default.Laptop, Icons.Default.Code,
                    Icons.Default.Science, Icons.Default.Calculate, Icons.Default.Brush,
                    Icons.Default.Palette, Icons.Default.MusicNote, Icons.Default.Movie,
                    Icons.Default.CameraAlt, Icons.Default.Article, Icons.Default.Edit
                )
            ),
            IconCategory(
                "Nature & Sport",
                listOf(
                    Icons.Default.Park, Icons.Default.Forest, Icons.Default.Water,
                    Icons.Default.WbSunny, Icons.Default.Spa, Icons.Default.FitnessCenter,
                    Icons.AutoMirrored.Filled.DirectionsRun, Icons.Default.Pool, Icons.Default.SportsSoccer,
                    Icons.Default.SportsBasketball, Icons.Default.PedalBike, Icons.Default.Surfing
                )
            ),
            IconCategory(
                "People & Life",
                listOf(
                    Icons.Default.Person, Icons.Default.Group, Icons.Default.FamilyRestroom,
                    Icons.Default.ChildCare, Icons.Default.Favorite, Icons.Default.Star,
                    Icons.Default.Celebration, Icons.Default.Cake, Icons.Default.CardGiftcard,
                    Icons.AutoMirrored.Filled.Chat, Icons.Default.Phone, Icons.Default.Email
                )
            )
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Choose Icon",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            categories.forEach { category ->
                item(key = "${category.title}_header") {
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                category.icons.chunked(5).forEachIndexed { rowIndex, rowIcons ->
                    item(key = "${category.title}_row_$rowIndex") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            repeat(5) { index ->
                                if (index < rowIcons.size) {
                                    val icon = rowIcons[index]
                                    val isSelected = icon == selectedIcon
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) accent.copy(alpha = 0.15f)
                                                else scheme.surfaceVariant.copy(alpha = 0.6f)
                                            )
                                            .border(
                                                width = if (isSelected) 3.dp else 0.dp,
                                                color = if (isSelected) accent else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .clickable { onIconSelected(icon) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (isSelected) accent else accent.copy(alpha = 0.7f),
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.size(64.dp))
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}