package com.ego.mobile.feature.history

import androidx.compose.runtime.Composable
import com.ego.mobile.app.navigation.AppDestination
import com.ego.mobile.feature.common.PlaceholderScreen

@Composable
fun HistoryScreen() {
    PlaceholderScreen(
        title = AppDestination.HISTORY.title,
        subtitle = "Área de histórico em desenvolvimento.",
        helperText = "As execuções, estados e consistência do usuário serão listados nesta área.",
    )
}
