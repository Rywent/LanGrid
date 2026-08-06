package com.rywent.langrid.presentation.navigation


// all routes in the app
sealed class Screen(val route: String){

    object Words : Screen("words")
    object Diary : Screen("diary")
    object Cards : Screen("cards")
    object Speech : Screen("speech")

    object Settings : Screen("settings")
    object AboutVersions : Screen("about")
}