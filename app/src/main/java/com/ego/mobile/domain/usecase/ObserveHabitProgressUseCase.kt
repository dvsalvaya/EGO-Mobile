package com.ego.mobile.domain.usecase

import com.ego.mobile.domain.model.HabitProgress
import com.ego.mobile.domain.model.HabitProgressRules
import com.ego.mobile.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId

/** Observa o progresso de todos os hábitos a partir das ocorrências persistidas. */
class ObserveHabitProgressUseCase(
    private val habitRepository: HabitRepository,
    private val today: () -> LocalDate = { LocalDate.now(ZoneId.of("America/Sao_Paulo")) },
) {
    operator fun invoke(): Flow<Map<Long, HabitProgress>> =
        habitRepository.observeHabitOccurrenceSnapshot().map { snapshot ->
            snapshot.habits.associate { habit ->
                habit.id to HabitProgressRules.progressFor(
                    habit = habit,
                    occurrences = snapshot.occurrencesByHabit[habit.id].orEmpty(),
                    today = today(),
                )
            }
        }
}
