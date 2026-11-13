package com.ego.mobile.domain.repository

import com.ego.mobile.domain.model.WorkoutExercise
import com.ego.mobile.domain.model.WorkoutSession
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun getActiveSession(): Flow<WorkoutSession?>
    suspend fun getActiveSessionSync(): WorkoutSession?
    fun getCompletedSessions(): Flow<List<WorkoutSession>>
    fun getSessionById(sessionId: Long): Flow<WorkoutSession?>
    suspend fun startSession(title: String, startedAt: Long = System.currentTimeMillis()): Result<WorkoutSession>
    suspend fun pauseSession(sessionId: Long): Result<Unit>
    suspend fun resumeSession(sessionId: Long): Result<Unit>
    suspend fun completeSession(sessionId: Long, endedAt: Long = System.currentTimeMillis()): Result<Unit>
    suspend fun cancelSession(sessionId: Long): Result<Unit>
    suspend fun deleteSession(sessionId: Long): Result<Unit>
    suspend fun addExercise(exercise: WorkoutExercise): Result<Long>
    suspend fun updateExercise(exercise: WorkoutExercise): Result<Unit>
    suspend fun removeExercise(exerciseId: Long): Result<Unit>
}
