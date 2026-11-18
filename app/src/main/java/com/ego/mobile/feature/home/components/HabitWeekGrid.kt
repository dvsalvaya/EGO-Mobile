package com.ego.mobile.feature.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ego.mobile.domain.model.HabitDayGrid
import com.ego.mobile.domain.model.HabitGridBand
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HabitWeekGrid(grid: HabitDayGrid, modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Consistência dos hábitos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                grid.days.forEach { day ->
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(day.date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.forLanguageTag("pt-BR")).take(1).uppercase())
                        val color = when (day.band) {
                            HabitGridBand.NO_PREDICTION -> MaterialTheme.colorScheme.surfaceVariant
                            HabitGridBand.IN_PROGRESS -> androidx.compose.ui.graphics.Color(0xFF6B7280)
                            HabitGridBand.MISSED -> MaterialTheme.colorScheme.error
                            HabitGridBand.LOW -> androidx.compose.ui.graphics.Color(0xFFF59E0B)
                            HabitGridBand.PARTIAL -> androidx.compose.ui.graphics.Color(0xFF166534)
                            HabitGridBand.COMPLETE -> androidx.compose.ui.graphics.Color(0xFF86EFAC)
                        }
                        Surface(
                            modifier = Modifier.size(30.dp).semantics {
                                contentDescription = buildString {
                                    append("${day.date.format(DATE_FORMAT)}: ${day.completed} de ${day.predicted} hábitos concluídos")
                                    if (day.isToday && day.isInProgress) append(", Em andamento")
                                    else append(", ${day.band.accessibleName}")
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = color,
                            border = if (day.isToday) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface) else null,
                        ) {}
                    }
                }
            }
        }
    }
}

private val DATE_FORMAT = DateTimeFormatter.ofPattern("d 'de' MMMM", Locale.forLanguageTag("pt-BR"))
private val HabitGridBand.accessibleName: String
    get() = when (this) {
        HabitGridBand.NO_PREDICTION -> "Sem previsão"
        HabitGridBand.IN_PROGRESS -> "Em andamento"
        HabitGridBand.MISSED -> "Nenhum concluído"
        HabitGridBand.LOW -> "Abaixo de 50 por cento"
        HabitGridBand.PARTIAL -> "Parcialmente concluído"
        HabitGridBand.COMPLETE -> "Tudo concluído"
    }
