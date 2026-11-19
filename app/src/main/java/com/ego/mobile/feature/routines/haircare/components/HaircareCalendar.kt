package com.ego.mobile.feature.routines.haircare.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ego.mobile.domain.model.HaircareOccurrence
import com.ego.mobile.domain.model.HaircareOccurrenceStatus
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HaircareCalendar(
    month: YearMonth,
    selectedDate: LocalDate,
    occurrences: List<HaircareOccurrence>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = Locale.forLanguageTag("pt-BR")
    val monthName = month.month.getDisplayName(TextStyle.FULL, locale)
        .replaceFirstChar { it.titlecase(locale) }
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onPreviousMonth, modifier = Modifier.semantics { contentDescription = "Mês anterior" }) { Text("‹") }
            Text("$monthName ${month.year}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            TextButton(onClick = onNextMonth, modifier = Modifier.semantics { contentDescription = "Próximo mês" }) { Text("›") }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("S", "T", "Q", "Q", "S", "S", "D").forEachIndexed { index, label ->
                Text(label, modifier = Modifier.weight(1f).semantics { contentDescription = listOf("Segunda-feira", "Terça-feira", "Quarta-feira", "Quinta-feira", "Sexta-feira", "Sábado", "Domingo")[index] }, textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        val firstCell = month.atDay(1).dayOfWeek.value - 1
        val cells = ((firstCell + month.lengthOfMonth() + 6) / 7) * 7
        repeat(cells / 7) { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { weekday ->
                    val dayNumber = week * 7 + weekday - firstCell + 1
                    if (dayNumber in 1..month.lengthOfMonth()) {
                        val date = month.atDay(dayNumber)
                        val dayOccurrences = occurrences.filter { it.scheduledDate == date.toString() }
                        val pending = dayOccurrences.count { it.status == HaircareOccurrenceStatus.PENDING || it.status == HaircareOccurrenceStatus.RESCHEDULED }
                        val completed = dayOccurrences.count { it.status == HaircareOccurrenceStatus.COMPLETED }
                        val description = buildString {
                            append("${date.dayOfMonth} de $monthName de ${month.year}")
                            if (pending > 0) append(", $pending pendente${if (pending == 1) "" else "s"}")
                            if (completed > 0) append(", $completed concluída${if (completed == 1) "" else "s"}")
                        }
                        Column(
                            modifier = Modifier.weight(1f).aspectRatio(0.9f).padding(1.dp)
                                .background(if (date == selectedDate) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface, CircleShape)
                                .clickable { onSelectDate(date) }
                                .semantics { contentDescription = description },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text("$dayNumber", style = MaterialTheme.typography.bodyMedium, color = if (date == selectedDate) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                            if (dayOccurrences.isNotEmpty()) {
                                Text("${if (pending > 0) "•" else "✓"} ${dayOccurrences.size}", style = MaterialTheme.typography.labelSmall, color = if (pending > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        Text("", modifier = Modifier.weight(1f).aspectRatio(0.9f))
                    }
                }
            }
        }
    }
}
