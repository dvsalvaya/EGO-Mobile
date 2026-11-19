package com.ego.mobile.feature.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    iconContentDescription: String? = "Ícone de estado vazio",
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    isCard: Boolean = false,
) {
    val content: @Composable () -> Unit = {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (iconContentDescription != null || icon != null) {
                Box(
                    modifier = Modifier.size(40.dp).semantics {
                        iconContentDescription?.let { contentDescription = it }
                    },
                    contentAlignment = Alignment.Center,
                ) {
                    if (icon != null) icon() else EmptyStateGlyph()
                }
            }
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!actionLabel.isNullOrBlank() && onActionClick != null) {
                Button(
                    onClick = onActionClick,
                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
                ) { Text(actionLabel) }
            }
        }
    }
    if (isCard) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) { content() }
    } else {
        Box(modifier = modifier, contentAlignment = Alignment.Center) { content() }
    }
}

@Composable
private fun EmptyStateGlyph() {
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    Canvas(Modifier.size(24.dp).clip(RoundedCornerShape(12.dp))) {
        drawCircle(color = color, radius = size.minDimension * .38f, style = Stroke(width = 2.dp.toPx()))
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(size.width * .5f, size.height * .3f),
            end = androidx.compose.ui.geometry.Offset(size.width * .5f, size.height * .55f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawCircle(color = color, radius = 1.dp.toPx(), center = androidx.compose.ui.geometry.Offset(size.width * .5f, size.height * .72f))
    }
}
