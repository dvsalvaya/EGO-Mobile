package com.ego.mobile.feature.home

import androidx.compose.runtime.Composable
import com.ego.mobile.app.navigation.AppDestination
import com.ego.mobile.feature.common.PlaceholderScreen

@Composable
fun HomeScreen() {
    PlaceholderScreen(
        title = AppDestination.HOME.title,
        subtitle = "Resumo geral do EGO Mobile em construção.",
        helperText = "Esta área concentra o estado atual da aplicação e os acessos rápidos do MVP.",
    )
}
