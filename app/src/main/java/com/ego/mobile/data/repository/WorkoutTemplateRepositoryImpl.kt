package com.ego.mobile.data.repository

import com.ego.mobile.data.local.dao.TrainingModelDao
import com.ego.mobile.data.local.dao.TrainingModelExerciseDao
import com.ego.mobile.data.local.mapper.toDomain
import com.ego.mobile.data.local.mapper.toEntity
import com.ego.mobile.domain.model.WorkoutExercise
import com.ego.mobile.domain.model.WorkoutTemplate
import com.ego.mobile.domain.repository.WorkoutRepository
import com.ego.mobile.domain.repository.WorkoutTemplateRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutTemplateRepositoryImpl(
    private val modelDao: TrainingModelDao,
    private val exerciseDao: TrainingModelExerciseDao,
    private val workoutRepository: WorkoutRepository,
    private val clock: () -> Long = System::currentTimeMillis,
) : WorkoutTemplateRepository {

    override fun observeTemplates(): Flow<List<WorkoutTemplate>> =
        modelDao.observeAllModels().flatMapLatest { models ->
            if (models.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(
                    models.map { model ->
                        exerciseDao.observeExercisesByModel(model.id).map { exercises ->
                            model.toDomain(exercises)
                        }
                    },
                ) { templates -> templates.toList() }
            }
        }

    override suspend fun getTemplate(templateId: Long): WorkoutTemplate? {
        val model = modelDao.getModelById(templateId) ?: return null
        return model.toDomain(exerciseDao.getExercisesByModel(templateId))
    }

    override suspend fun saveTemplate(template: WorkoutTemplate): Result<Long> = runCatching {
        val now = clock()
        val model = template.copy(createdAt = now, updatedAt = now)
        modelDao.insertModelWithExercises(model.toEntity(), normalizedExercises(model))
    }

    override suspend fun updateTemplate(template: WorkoutTemplate): Result<Unit> = runCatching {
        val existing = modelDao.getModelById(template.id)
            ?: throw IllegalArgumentException("Workout template ${template.id} not found")
        val model = template.copy(createdAt = existing.createdAt, updatedAt = clock())
        modelDao.updateModelWithExercises(model.toEntity(), normalizedExercises(model))
    }

    override suspend fun deleteTemplate(templateId: Long): Result<Unit> = runCatching {
        modelDao.deleteModelById(templateId)
    }

    override suspend fun applyTemplate(
        templateId: Long,
        startedAt: Long,
    ): Result<com.ego.mobile.domain.model.WorkoutSession> = runCatching {
        val template = getTemplate(templateId)
            ?: throw IllegalArgumentException("Workout template $templateId not found")
        val session = workoutRepository.startSession(template.name, startedAt).getOrThrow()
        if (session.exercises.isNotEmpty()) {
            return@runCatching session
        }
        template.orderedExercises.forEach { exercise ->
            workoutRepository.addExercise(
                WorkoutExercise(
                    sessionId = session.id,
                    name = exercise.name,
                    orderIndex = exercise.orderIndex,
                    setsCount = exercise.setsCount,
                    reps = exercise.reps,
                    weightKg = exercise.weightKg,
                    notes = exercise.notes,
                ),
            ).getOrThrow()
        }
        workoutRepository.getSessionById(session.id).first() ?: session
    }

    private fun normalizedExercises(template: WorkoutTemplate) =
        template.orderedExercises.mapIndexed { index, exercise ->
            exercise.copy(orderIndex = index, templateId = template.id).toEntity()
        }
}
