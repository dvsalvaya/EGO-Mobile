package com.ego.mobile.domain.usecase

import com.ego.mobile.domain.repository.HaircareRepository

class RescheduleHaircareStepUseCase(
    private val haircareRepository: HaircareRepository,
) {
    suspend operator fun invoke(occurrenceId: Long, newDateCivil: String) =
        haircareRepository.rescheduleStep(occurrenceId, newDateCivil)
}
