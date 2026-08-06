package com.rywent.langrid.presentation.navigation

internal fun isMainRoute(route: String?) : Boolean = when (route){
    Screen.Words .route,
    Screen.Diary.route,
    Screen.Cards.route,
    Screen.Speech.route -> true
    else -> false
}

internal fun mainRouteIndex(route: String?) : Int? = when(route){
    Screen.Words.route -> 0
    Screen.Diary.route -> 1
    Screen.Cards.route -> 2
    Screen.Speech.route -> 3
    else -> null
}