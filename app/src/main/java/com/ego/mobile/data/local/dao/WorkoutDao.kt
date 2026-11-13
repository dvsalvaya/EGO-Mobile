package com.ego.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ego.mobile.data.local.entity.WorkoutExerciseEntity
import com.ego.mobile.data.local.entity.WorkoutSessionEntity
import com.ego.mobile.data.local.relation.WorkoutSessionWithExercises
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Update
    suspend fun updateSession(session: WorkoutSessionEntity)

    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: Long): WorkoutSessionEntity?

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE status IN ('IN_PROGRESS', 'PAUSED') ORDER BY startedAt DESC LIMIT 1")
    fun getActiveSession(): Flow<WorkoutSessionWithExercises?>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE status IN ('IN_PROGRESS', 'PAUSED') ORDER BY startedAt DESC LIMIT 1")
    suspend fun getActiveSessionSync(): WorkoutSessionWithExercises?

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId LIMIT 1")
    fun getSessionWithExercises(sessionId: Long): Flow<WorkoutSessionWithExercises?>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionWithExercisesSync(sessionId: Long): WorkoutSessionWithExercises?

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE status = 'COMPLETED' ORDER BY startedAt DESC")
    fun getCompletedSessions(): Flow<List<WorkoutSessionWithExercises>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: WorkoutExerciseEntity): Long

    @Update
    suspend fun updateExercise(exercise: WorkoutExerciseEntity)

    @Query("DELETE FROM workout_exercises WHERE id = :exerciseId")
    suspend fun deleteExercise(exerciseId: Long)

    @Query("SELECT * FROM workout_exercises WHERE sessionId = :sessionId ORDER BY orderIndex ASC")
    fun getExercisesBySessionId(sessionId: Long): Flow<List<WorkoutExerciseEntity>>

    @Query("DELETE FROM workout_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)
}
