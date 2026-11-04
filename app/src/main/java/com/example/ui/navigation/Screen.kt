package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Profile : Screen("profile")
    object Packages : Screen("packages")
    object Home : Screen("home")
    object Chats : Screen("chats")
    object Admin : Screen("admin")
}
