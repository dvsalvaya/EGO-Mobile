package com.ego.mobile.feature.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity

@Composable
fun QuickActionHeader(
    onOpenHistory: () -> Unit = {},
    greeting: String = "Bom dia, Chefe",
    onEditName: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val fontScale = LocalDensity.current.fontScale
            val compact = fontScale >= 1.25f || maxWidth < 380.dp
            if (compact) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onEditName, modifier = Modifier.defaultMinSize(minHeight = 48.dp)) {
                            Text(greeting, style = MaterialTheme.typography.headlineSmall)
                        }
                        IconButton(onClick = onEditName, modifier = Modifier.semantics { contentDescription = "Editar nome" }) {
                            Text("✎")
                        }
                    }
                    TextButton(
                        onClick = onOpenHistory,
                        modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
                    ) { Text("Ver histórico") }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onEditName, modifier = Modifier.defaultMinSize(minHeight = 48.dp)) {
                            Text(greeting, style = MaterialTheme.typography.headlineSmall)
                        }
                        IconButton(onClick = onEditName, modifier = Modifier.semantics { contentDescription = "Editar nome" }) {
                            Text("✎")
                        }
                    }
                    TextButton(
                        onClick = onOpenHistory,
                        modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
                    ) { Text("Ver histórico") }
                }
            }
        }
    }
}
