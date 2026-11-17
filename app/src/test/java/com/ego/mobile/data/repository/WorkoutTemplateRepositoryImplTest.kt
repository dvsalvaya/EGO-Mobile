package com.ego.mobile.data.repository

import com.ego.mobile.data.local.dao.TrainingModelDao
import com.ego.mobile.data.local.dao.TrainingModelExerciseDao
import com.ego.mobile.data.local.entity.TrainingModelEntity
import com.ego.mobile.data.local.entity.TrainingModelExerciseEntity
import com.ego.mobile.domain.model.WorkoutExercise
import com.ego.mobile.domain.model.WorkoutSession
import com.ego.mobile.domain.model.WorkoutTemplate
import com.ego.mobile.domain.model.WorkoutTemplateExercise
import com.ego.mobile.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutTemplateRepositoryImplTest {

    private val store = Store()
    private val workouts = FakeWorkoutRepository()
    private val repository = WorkoutTemplateRepositoryImpl(
        modelDao = FakeModelDao(store),
        exerciseDao = FakeExerciseDao(store),
        workoutRepository = workouts,
        clock = { 1_000L },
    )

    @Test
    fun `save and get preserve the exercise order and optional fields`() = runTest {
        val id = repository.saveTemplate(
            WorkoutTemplate(
                name = "Treino A",
                exercises = listOf(
                    templateExercise(name = "Tríceps", orderIndex = 2),
                    templateExercise(name = "Supino", orderIndex = 0, reps = "8-10", weightKg = 80.0),
                    templateExercise(name = "Agachamento", orderIndex = 1, notes = "cadência lenta"),
                ),
            ),
        ).getOrThrow()

        val template = repository.getTemplate(id)
        assertNotNull(template)
        assertEquals("Treino A", template!!.name)
        assertEquals(listOf("Supino", "Agachamento", "Tríceps"), template.exercises.map { it.name })
        assertEquals(listOf(0, 1, 2), template.exercises.map { it.orderIndex })
        assertEquals("8-10", template.exercises.first().reps)
        assertEquals("cadência lenta", template.exercises[1].notes)
        assertEquals(1_000L, template.createdAt)
    }

    @Test
    fun `update replaces exercises and keeps creation time`() = runTest {
        val id = repository.saveTemplate(
            WorkoutTemplate(name = "Antes", exercises = listOf(templateExercise(name = "Supino"))),
        ).getOrThrow()
        val original = repository.getTemplate(id)!!

        repository.updateTemplate(
            original.copy(
                name = "Depois",
                exercises = listOf(templateExercise(name = "Agachamento"), templateExercise(name = "Remada")),
            ),
        ).getOrThrow()

        val updated = repository.getTemplate(id)!!
        assertEquals("Depois", updated.name)
        assertEquals(listOf("Agachamento", "Remada"), updated.exercises.map { it.name })
        assertEquals(original.createdAt, updated.createdAt)
        assertTrue(updated.updatedAt >= original.updatedAt)
    }

    @Test
    fun `delete removes the template from storage`() = runTest {
        val id = repository.saveTemplate(WorkoutTemplate(name = "Temporário")).getOrThrow()

        repository.deleteTemplate(id).getOrThrow()

        assertEquals(null, repository.getTemplate(id))
    }

    @Test
    fun `observe templates emits persisted models`() = runTest {
        repository.saveTemplate(WorkoutTemplate(name = "Treino B")).getOrThrow()

        assertEquals(listOf("Treino B"), repository.observeTemplates().first().map { it.name })
    }

    @Test
    fun `apply template creates an independent session and does not mutate the model`() = runTest {
        val id = repository.saveTemplate(
            WorkoutTemplate(
                name = "Treino Costas",
                exercises = listOf(
                    templateExercise(name = "Remada", orderIndex = 0, reps = "10", weightKg = 50.0),
                    templateExercise(name = "Puxada", orderIndex = 1),
                ),
            ),
        ).getOrThrow()

        val session = repository.applyTemplate(id, startedAt = 5_000L).getOrThrow()

        assertEquals("Treino Costas", session.title)
        assertEquals(5_000L, session.startedAt)
        assertEquals(listOf("Remada", "Puxada"), session.exercises.map { it.name })
        assertEquals(50.0, session.exercises.first().weightKg!!, 0.01)
        assertEquals(2, repository.getTemplate(id)!!.exercises.size)

        workouts.removeExercise(session.exercises.first().id).getOrThrow()

        assertEquals(1, workouts.getActiveSessionSync()!!.exercises.size)
        assertEquals(2, repository.getTemplate(id)!!.exercises.size)
    }

    @Test
    fun `apply template reuses the active session without duplicating exercises`() = runTest {
        val id = repository.saveTemplate(
            WorkoutTemplate(name = "Treino A", exercises = listOf(templateExercise(name = "Supino"))),
        ).getOrThrow()

        val first = repository.applyTemplate(id, startedAt = 1L).getOrThrow()
        val second = repository.applyTemplate(id, startedAt = 9L).getOrThrow()

        assertEquals(first.id, second.id)
        assertEquals(1, second.exercises.size)
        assertEquals(1L, second.startedAt)
    }

    private fun templateExercise(
        name: String,
        orderIndex: Int = 0,
        reps: String? = null,
        weightKg: Double? = null,
        notes: String? = null,
    ) = WorkoutTemplateExercise(
        name = name,
        orderIndex = orderIndex,
        setsCount = 3,
        reps = reps,
        weightKg = weightKg,
        notes = notes,
    )

    private class Store {
        val models = linkedMapOf<Long, TrainingModelEntity>()
        val exercises = linkedMapOf<Long, TrainingModelExerciseEntity>()
        val modelFlow = MutableStateFlow<List<TrainingModelEntity>>(emptyList())
        val exerciseFlows = mutableMapOf<Long, MutableStateFlow<List<TrainingModelExerciseEntity>>>()

        fun emitModels() {
            modelFlow.value = models.values.toList()
        }

        fun emitExercises(modelId: Long) {
            exerciseFlows.getOrPut(modelId) { MutableStateFlow(emptyList()) }.value =
                exercises.values.filter { it.modelId == modelId }.toList()
        }
    }

    private class FakeModelDao(private val store: Store) : TrainingModelDao {
        private var nextId = 1L

        override suspend fun insertModel(model: TrainingModelEntity): Long {
            val id = if (model.id == 0L) nextId++ else model.id
            store.models[id] = model.copy(id = id)
            store.emitModels()
            return id
        }

        override suspend fun updateModel(model: TrainingModelEntity) {
            store.models[model.id] = model
            store.emitModels()
        }

        override suspend fun getModelById(id: Long): TrainingModelEntity? = store.models[id]

        override fun observeAllModels(): Flow<List<TrainingModelEntity>> = store.modelFlow

        override suspend fun deleteModelById(id: Long) {
            store.models.remove(id)
            store.exercises.entries.removeIf { it.value.modelId == id }
            store.emitModels()
            store.emitExercises(id)
        }

        override suspend fun insertExercises(exercises: List<TrainingModelExerciseEntity>) {
            exercises.forEach { exercise ->
                val id = if (exercise.id == 0L) nextId++ else exercise.id
                store.exercises[id] = exercise.copy(id = id)
            }
            exercises.map { it.modelId }.distinct().forEach(store::emitExercises)
        }

        override suspend fun deleteExercisesByModel(modelId: Long) {
            store.exercises.entries.removeIf { it.value.modelId == modelId }
            store.emitExercises(modelId)
        }
    }

    private class FakeExerciseDao(private val store: Store) : TrainingModelExerciseDao {
        override fun observeExercisesByModel(modelId: Long): Flow<List<TrainingModelExerciseEntity>> =
            store.exerciseFlows.getOrPut(modelId) { MutableStateFlow(emptyList()) }

        override suspend fun getExercisesByModel(modelId: Long): List<TrainingModelExerciseEntity> =
            store.exercises.values.filter { it.modelId == modelId }
    }

    private class FakeWorkoutRepository : WorkoutRepository {
        private val session = MutableStateFlow<WorkoutSession?>(null)
        private var nextExerciseId = 1L

        override fun getActiveSession(): Flow<WorkoutSession?> = session
        override suspend fun getActiveSessionSync(): WorkoutSession? = session.value
        override fun getCompletedSessions(): Flow<List<WorkoutSession>> = MutableStateFlow(emptyList())
        override fun getSessionById(sessionId: Long): Flow<WorkoutSession?> =
            session.map { if (it?.id == sessionId) it else null }

        override suspend fun startSession(title: String, startedAt: Long): Result<WorkoutSession> {
            session.value?.let { return Result.success(it) }
            val created = WorkoutSession(id = 1L, title = title, startedAt = startedAt)
            session.value = created
            return Result.success(created)
        }

        override suspend fun pauseSession(sessionId: Long): Result<Unit> = Result.success(Unit)
        override suspend fun resumeSession(sessionId: Long): Result<Unit> = Result.success(Unit)
        override suspend fun completeSession(sessionId: Long, endedAt: Long): Result<Unit> = Result.success(Unit)
        override suspend fun cancelSession(sessionId: Long): Result<Unit> = Result.success(Unit)
        override suspend fun deleteSession(sessionId: Long): Result<Unit> = Result.success(Unit)

        override suspend fun addExercise(exercise: WorkoutExercise): Result<Long> {
            val id = nextExerciseId++
            session.value = session.value?.copy(
                exercises = session.value!!.exercises + exercise.copy(id = id),
            )
            return Result.success(id)
        }

        override suspend fun updateExercise(exercise: WorkoutExercise): Result<Unit> = Result.success(Unit)

        override suspend fun removeExercise(exerciseId: Long): Result<Unit> {
            session.value = session.value?.copy(
                exercises = session.value!!.exercises.filterNot { it.id == exerciseId },
            )
            return Result.success(Unit)
        }
    }
}
