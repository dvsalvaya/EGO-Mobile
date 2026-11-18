package com.ego.mobile.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ego.mobile.app.navigation.AppNavigation
import com.ego.mobile.core.ui.theme.EgoTheme

@Composable
fun EgoApp() {
    EgoTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            AppNavigation()
        }
    }
}
