package com.ego.mobile.domain.usecase

import com.ego.mobile.domain.model.WorkoutSession
import com.ego.mobile.domain.model.WorkoutSessionStatus
import com.ego.mobile.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.first
import java.util.Locale

/** Estatísticas derivadas apenas de sessões concluídas, sem pontuação geral. */
class GetWorkoutStatisticsUseCase(
    private val workoutRepository: WorkoutRepository,
) {
    data class LoadPoint(
        val sessionId: Long,
        val endedAt: Long,
        val weightKg: Double,
    )

    data class ExerciseLoad(
        val exerciseName: String,
        val maxLoadKg: Double?,
        val sessionCount: Int,
        val points: List<LoadPoint> = emptyList(),
    )

    data class Statistics(
        val totalCompletedWorkouts: Int,
        val averageDurationMinutes: Double,
        val exerciseLoads: List<ExerciseLoad>,
    )

    suspend operator fun invoke(): Statistics = fromSessions(workoutRepository.getCompletedSessions().first())

    fun fromSessions(sessions: List<WorkoutSession>): Statistics {
        val completed = sessions.asSequence()
            .filter { it.status == WorkoutSessionStatus.COMPLETED && it.endedAt != null }
            .sortedWith(compareBy<WorkoutSession> { it.endedAt!! }.thenBy { it.id })
            .toList()

        val total = completed.size
        val averageDurationMinutes = if (total == 0) {
            0.0
        } else {
            completed.sumOf { it.durationMillis() } / total.toDouble() / MILLIS_PER_MINUTE
        }

        val displayNames = LinkedHashMap<String, String>()
        val sessionCounts = mutableMapOf<String, Int>()
        val points = mutableMapOf<String, MutableList<LoadPoint>>()

        completed.forEach { session ->
            val keysInSession = session.exercises
                .mapNotNull { exercise -> normalizedName(exercise.name)?.let { it to exercise.name.trim() } }
                .toMap()
            keysInSession.forEach { (key, displayName) ->
                displayNames.putIfAbsent(key, displayName)
                sessionCounts[key] = (sessionCounts[key] ?: 0) + 1
            }

            val bestWeightByExercise = mutableMapOf<String, Double>()
            session.exercises.forEach { exercise ->
                val key = normalizedName(exercise.name) ?: return@forEach
                val weight = exercise.weightKg
                if (weight != null && weight > 0.0) {
                    bestWeightByExercise[key] = maxOf(bestWeightByExercise[key] ?: 0.0, weight)
                }
            }
            bestWeightByExercise.forEach { (key, weight) ->
                points.getOrPut(key) { mutableListOf() }.add(
                    LoadPoint(sessionId = session.id, endedAt = session.endedAt!!, weightKg = weight),
                )
            }
        }

        val exerciseLoads = displayNames.map { (key, displayName) ->
            val exercisePoints = points[key].orEmpty()
            ExerciseLoad(
                exerciseName = displayName,
                maxLoadKg = exercisePoints.maxOfOrNull { it.weightKg },
                sessionCount = sessionCounts[key] ?: 0,
                points = exercisePoints,
            )
        }

        return Statistics(
            totalCompletedWorkouts = total,
            averageDurationMinutes = averageDurationMinutes,
            exerciseLoads = exerciseLoads,
        )
    }

    private fun normalizedName(name: String): String? =
        name.trim().takeIf { it.isNotEmpty() }?.lowercase(Locale.ROOT)

    private companion object {
        const val MILLIS_PER_MINUTE = 60_000.0
    }
}
