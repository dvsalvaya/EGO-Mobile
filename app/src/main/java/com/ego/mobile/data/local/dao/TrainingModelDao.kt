package com.ego.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ego.mobile.data.local.entity.TrainingModelEntity
import com.ego.mobile.data.local.entity.TrainingModelExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingModelDao {

    @Insert
    suspend fun insertModel(model: TrainingModelEntity): Long

    @Update
    suspend fun updateModel(model: TrainingModelEntity)

    @Query("SELECT * FROM training_models WHERE id = :id LIMIT 1")
    suspend fun getModelById(id: Long): TrainingModelEntity?

    @Query("SELECT * FROM training_models ORDER BY name COLLATE NOCASE ASC, id ASC")
    fun observeAllModels(): Flow<List<TrainingModelEntity>>

    @Query("DELETE FROM training_models WHERE id = :id")
    suspend fun deleteModelById(id: Long)

    @Insert
    suspend fun insertExercises(exercises: List<TrainingModelExerciseEntity>)

    @Query("DELETE FROM training_models_exercises WHERE modelId = :modelId")
    suspend fun deleteExercisesByModel(modelId: Long)

    @Transaction
    suspend fun insertModelWithExercises(
        model: TrainingModelEntity,
        exercises: List<TrainingModelExerciseEntity>,
    ): Long {
        val modelId = insertModel(model)
        if (exercises.isNotEmpty()) {
            insertExercises(exercises.map { it.copy(modelId = modelId) })
        }
        return modelId
    }

    @Transaction
    suspend fun updateModelWithExercises(
        model: TrainingModelEntity,
        exercises: List<TrainingModelExerciseEntity>,
    ) {
        updateModel(model)
        deleteExercisesByModel(model.id)
        if (exercises.isNotEmpty()) {
            insertExercises(exercises.map { it.copy(modelId = model.id) })
        }
    }
}
