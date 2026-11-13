package com.ego.mobile.feature.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ego.mobile.domain.model.ActivityType
import com.ego.mobile.domain.model.HomeSummary

@Composable
fun ActiveRoutineBanner(
    routine: HomeSummary.ActiveRoutine,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                if (routine.type == ActivityType.WORKOUT) "Treino em andamento" else "Rotina ativa",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(routine.title, style = MaterialTheme.typography.titleLarge)
            routine.subtitle?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            Button(onClick = onContinue) {
                Text(if (routine.type == ActivityType.WORKOUT) "Continuar treino" else "Continuar rotina")
            }
        }
    }
}
