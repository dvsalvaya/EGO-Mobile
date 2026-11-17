package com.ego.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ego.mobile.data.local.entity.HabitOccurrenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitOccurrenceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(occurrence: HabitOccurrenceEntity): Long

    @Update
    suspend fun update(occurrence: HabitOccurrenceEntity)

    @Query("SELECT * FROM habit_occurrences WHERE habitId = :habitId AND dateCivil = :dateCivil LIMIT 1")
    suspend fun getByHabitAndDate(habitId: Long, dateCivil: String): HabitOccurrenceEntity?

    @Query("DELETE FROM habit_occurrences WHERE habitId = :habitId AND dateCivil = :dateCivil")
    suspend fun deleteByHabitAndDate(habitId: Long, dateCivil: String)

    @Query("SELECT * FROM habit_occurrences WHERE habitId = :habitId ORDER BY dateCivil DESC")
    fun observeByHabitId(habitId: Long): Flow<List<HabitOccurrenceEntity>>

    @Query("SELECT * FROM habit_occurrences WHERE habitId = :habitId AND dateCivil BETWEEN :startDate AND :endDate ORDER BY dateCivil ASC")
    suspend fun getByHabitAndDateRange(
        habitId: Long,
        startDate: String,
        endDate: String,
    ): List<HabitOccurrenceEntity>
}