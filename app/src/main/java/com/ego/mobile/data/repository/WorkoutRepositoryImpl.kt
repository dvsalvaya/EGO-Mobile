package com.ego.mobile.data.repository

import com.ego.mobile.data.local.dao.WorkoutDao
import com.ego.mobile.data.local.entity.WorkoutSessionEntity
import com.ego.mobile.data.local.mapper.toDomain
import com.ego.mobile.data.local.mapper.toEntity
import com.ego.mobile.data.local.relation.WorkoutSessionWithExercises
import com.ego.mobile.domain.model.WorkoutExercise
import com.ego.mobile.domain.model.WorkoutSession
import com.ego.mobile.domain.model.WorkoutSessionStatus
import com.ego.mobile.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WorkoutRepositoryImpl(
    private val workoutDao: WorkoutDao,
) : WorkoutRepository {

    override fun getActiveSession(): Flow<WorkoutSession?> {
        return workoutDao.getActiveSession().map { it?.toDomain() }
    }

    override suspend fun getActiveSessionSync(): WorkoutSession? {
        return workoutDao.getActiveSessionSync()?.toDomain()
    }

    override fun getCompletedSessions(): Flow<List<WorkoutSession>> {
        return workoutDao.getCompletedSessions().map { list -> list.map { it.toDomain() } }
    }

    override fun getSessionById(sessionId: Long): Flow<WorkoutSession?> {
        return workoutDao.getSessionWithExercises(sessionId).map { it?.toDomain() }
    }

    override suspend fun startSession(title: String, startedAt: Long): Result<WorkoutSession> = runCatching {
        // Idempotency: Se já existe sessão ativa, retorna a existente
        val existing = workoutDao.getActiveSessionSync()
        if (existing != null) {
            return@runCatching existing.toDomain()
        }

        require(title.isNotBlank()) { "Workout session title must not be blank" }

        val entity = WorkoutSessionEntity(
            title = title.trim(),
            startedAt = startedAt,
            status = WorkoutSessionStatus.IN_PROGRESS.name,
        )
        val id = workoutDao.insertSession(entity)
        val created = workoutDao.getSessionWithExercisesSync(id)
            ?: WorkoutSessionWithExercises(workoutDao.getSessionById(id)!!)
        created.toDomain()
    }

    override suspend fun pauseSession(sessionId: Long): Result<Unit> = runCatching {
        val sessionEntity = workoutDao.getSessionById(sessionId)
            ?: throw IllegalArgumentException("Workout session with id $sessionId not found")
        val domain = sessionEntity.toDomain()
        val updated = domain.transitionTo(WorkoutSessionStatus.PAUSED)
        workoutDao.updateSession(updated.toEntity())
    }

    override suspend fun resumeSession(sessionId: Long): Result<Unit> = runCatching {
        val sessionEntity = workoutDao.getSessionById(sessionId)
            ?: throw IllegalArgumentException("Workout session with id $sessionId not found")
        val domain = sessionEntity.toDomain()
        val updated = domain.transitionTo(WorkoutSessionStatus.IN_PROGRESS)
        workoutDao.updateSession(updated.toEntity())
    }

    override suspend fun completeSession(sessionId: Long, endedAt: Long): Result<Unit> = runCatching {
        val sessionEntity = workoutDao.getSessionById(sessionId)
            ?: throw IllegalArgumentException("Workout session with id $sessionId not found")
        val domain = sessionEntity.toDomain()
        val updated = domain.transitionTo(WorkoutSessionStatus.COMPLETED, timestamp = endedAt)
        workoutDao.updateSession(updated.toEntity())
    }

    override suspend fun cancelSession(sessionId: Long): Result<Unit> = runCatching {
        val sessionEntity = workoutDao.getSessionById(sessionId)
            ?: throw IllegalArgumentException("Workout session with id $sessionId not found")
        val domain = sessionEntity.toDomain()
        val updated = domain.transitionTo(WorkoutSessionStatus.CANCELLED)
        workoutDao.updateSession(updated.toEntity())
    }

    override suspend fun deleteSession(sessionId: Long): Result<Unit> = runCatching {
        workoutDao.deleteSession(sessionId)
    }

    override suspend fun addExercise(exercise: WorkoutExercise): Result<Long> = runCatching {
        workoutDao.insertExercise(exercise.toEntity())
    }

    override suspend fun updateExercise(exercise: WorkoutExercise): Result<Unit> = runCatching {
        workoutDao.updateExercise(exercise.toEntity())
    }

    override suspend fun removeExercise(exerciseId: Long): Result<Unit> = runCatching {
        workoutDao.deleteExercise(exerciseId)
    }
}
