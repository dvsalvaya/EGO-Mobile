package com.ego.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.ego.mobile.data.local.entity.TrainingModelExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingModelExerciseDao {

    @Query("SELECT * FROM training_models_exercises WHERE modelId = :modelId ORDER BY orderIndex ASC, id ASC")
    fun observeExercisesByModel(modelId: Long): Flow<List<TrainingModelExerciseEntity>>

    @Query("SELECT * FROM training_models_exercises WHERE modelId = :modelId ORDER BY orderIndex ASC, id ASC")
    suspend fun getExercisesByModel(modelId: Long): List<TrainingModelExerciseEntity>
}
