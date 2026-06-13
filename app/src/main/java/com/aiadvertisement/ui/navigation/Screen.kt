package com.aiadvertisement.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Detail : Screen("detail/{adId}?startPositionMs={startPositionMs}") {
        fun createRoute(adId: String, startPositionMs: Long = 0L) =
            "detail/$adId?startPositionMs=$startPositionMs"
    }
    object Stats : Screen("stats")
    object Chat : Screen("chat")
    object Search : Screen("search")
}

fun androidx.navigation.NavController.navigateToDetail(adId: String, startPositionMs: Long = 0L) {
    navigate(Screen.Detail.createRoute(adId, startPositionMs))
}

fun androidx.navigation.NavController.navigateToChat() {
    navigate("chat")
}

fun androidx.navigation.NavController.navigateToSearch() {
    navigate("search")
}