package com.ego.mobile.domain.repository

import com.ego.mobile.domain.model.WorkoutSession
import com.ego.mobile.domain.model.WorkoutTemplate
import kotlinx.coroutines.flow.Flow

interface WorkoutTemplateRepository {
    fun observeTemplates(): Flow<List<WorkoutTemplate>>
    suspend fun getTemplate(templateId: Long): WorkoutTemplate?
    suspend fun saveTemplate(template: WorkoutTemplate): Result<Long>
    suspend fun updateTemplate(template: WorkoutTemplate): Result<Unit>
    suspend fun deleteTemplate(templateId: Long): Result<Unit>

    /** Cria uma sessão independente pré-preenchida; não altera o modelo. */
    suspend fun applyTemplate(
        templateId: Long,
        startedAt: Long = System.currentTimeMillis(),
    ): Result<WorkoutSession>
}
