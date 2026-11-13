package com.ego.mobile.feature.routines.haircare.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ego.mobile.domain.model.HaircareOccurrence
import com.ego.mobile.domain.model.HaircareOccurrenceStatus
import com.ego.mobile.domain.model.HaircareStepType

@Composable
fun CurrentStepCard(
    occurrence: HaircareOccurrence,
    onRecordExecution: () -> Unit,
    onReschedule: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = when (occurrence.stepType) {
        HaircareStepType.HIDRATACAO -> MaterialTheme.colorScheme.primaryContainer to
            MaterialTheme.colorScheme.onPrimaryContainer
        HaircareStepType.NUTRICAO -> MaterialTheme.colorScheme.secondaryContainer to
            MaterialTheme.colorScheme.onSecondaryContainer
        HaircareStepType.RECONSTRUCAO -> MaterialTheme.colorScheme.tertiaryContainer to
            MaterialTheme.colorScheme.onTertiaryContainer
        HaircareStepType.UMECTACAO, HaircareStepType.OUTRO -> MaterialTheme.colorScheme.surfaceVariant to
            MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.first),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = occurrence.stepType.displayName,
                style = MaterialTheme.typography.labelLarge,
                color = colors.second,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = occurrence.stepTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.second,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Prevista para ${occurrence.scheduledDate}",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.second,
            )
            if (occurrence.status == HaircareOccurrenceStatus.RESCHEDULED) {
                Text(
                    text = "Reagendada",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.second,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onRecordExecution,
                ) {
                    Text("Registrar execução")
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onReschedule,
                ) {
                    Text("Reagendar")
                }
            }
        }
    }
}
