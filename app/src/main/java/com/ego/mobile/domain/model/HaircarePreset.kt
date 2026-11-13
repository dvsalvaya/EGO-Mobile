package com.ego.mobile.domain.model

import java.time.LocalDate

data class HaircarePresetStep(
    val stepType: HaircareStepType,
    val stepTitle: String,
    val dayOffset: Int,
)

object HaircarePreset {
    const val STANDARD_NAME = "Cronograma Básico Equilibrado"
    const val STANDARD_DESCRIPTION = "Ciclo equilibrado de 4 semanas com foco em hidratação, nutrição e reconstrução."

    fun standardSteps(): List<HaircarePresetStep> = listOf(
        // Semana 1
        HaircarePresetStep(HaircareStepType.HIDRATACAO, "Hidratação", 0),
        HaircarePresetStep(HaircareStepType.NUTRICAO, "Nutrição", 2),
        HaircarePresetStep(HaircareStepType.HIDRATACAO, "Hidratação", 4),

        // Semana 2
        HaircarePresetStep(HaircareStepType.HIDRATACAO, "Hidratação", 7),
        HaircarePresetStep(HaircareStepType.NUTRICAO, "Nutrição", 9),
        HaircarePresetStep(HaircareStepType.HIDRATACAO, "Hidratação", 11),

        // Semana 3
        HaircarePresetStep(HaircareStepType.HIDRATACAO, "Hidratação", 14),
        HaircarePresetStep(HaircareStepType.NUTRICAO, "Nutrição", 16),
        HaircarePresetStep(HaircareStepType.RECONSTRUCAO, "Reconstrução", 18),

        // Semana 4
        HaircarePresetStep(HaircareStepType.HIDRATACAO, "Hidratação", 21),
        HaircarePresetStep(HaircareStepType.NUTRICAO, "Nutrição", 23),
        HaircarePresetStep(HaircareStepType.HIDRATACAO, "Hidratação", 25),
    )

    fun createStandardOccurrences(routineId: Long, startDate: String): List<HaircareOccurrence> {
        val startLocalDate = LocalDate.parse(startDate)
        return standardSteps().mapIndexed { index, step ->
            val scheduledDate = startLocalDate.plusDays(step.dayOffset.toLong()).toString()
            HaircareOccurrence(
                routineId = routineId,
                stepType = step.stepType,
                stepTitle = step.stepTitle,
                scheduledDate = scheduledDate,
                status = HaircareOccurrenceStatus.PENDING,
                orderIndex = index,
            )
        }
    }
}
