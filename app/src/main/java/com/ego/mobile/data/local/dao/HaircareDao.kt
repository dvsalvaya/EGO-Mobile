package com.ego.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ego.mobile.data.local.entity.HaircareOccurrenceEntity
import com.ego.mobile.data.local.entity.HaircareRoutineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HaircareDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: HaircareRoutineEntity): Long

    @Update
    suspend fun updateRoutine(routine: HaircareRoutineEntity)

    @Query("SELECT * FROM haircare_routines ORDER BY id DESC LIMIT 1")
    fun getActiveRoutine(): Flow<HaircareRoutineEntity?>

    @Query("SELECT * FROM haircare_routines WHERE id = :routineId")
    suspend fun getRoutineById(routineId: Long): HaircareRoutineEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOccurrence(occurrence: HaircareOccurrenceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOccurrences(occurrences: List<HaircareOccurrenceEntity>): List<Long>

    @Transaction
    suspend fun insertRoutineWithOccurrences(
        routine: HaircareRoutineEntity,
        occurrences: List<HaircareOccurrenceEntity>,
    ): Long {
        val routineId = insertRoutine(routine)
        insertOccurrences(occurrences.map { it.copy(routineId = routineId) })
        return routineId
    }

    @Update
    suspend fun updateOccurrence(occurrence: HaircareOccurrenceEntity)

    @Query("SELECT * FROM haircare_occurrences WHERE id = :occurrenceId")
    suspend fun getOccurrenceById(occurrenceId: Long): HaircareOccurrenceEntity?

    @Query("SELECT * FROM haircare_occurrences WHERE routineId = :routineId ORDER BY orderIndex ASC, scheduledDate ASC")
    fun observeOccurrencesForRoutine(routineId: Long): Flow<List<HaircareOccurrenceEntity>>

    @Query("SELECT * FROM haircare_occurrences WHERE routineId = :routineId AND (status = 'PENDING' OR status = 'RESCHEDULED') ORDER BY scheduledDate ASC, orderIndex ASC LIMIT 1")
    fun observeNextPendingOccurrence(routineId: Long): Flow<HaircareOccurrenceEntity?>

    @Query("SELECT * FROM haircare_occurrences WHERE status = 'COMPLETED' ORDER BY executedAt DESC")
    fun observeCompletedOccurrences(): Flow<List<HaircareOccurrenceEntity>>

    @Query("DELETE FROM haircare_routines WHERE id = :routineId")
    suspend fun deleteRoutine(routineId: Long)
}
