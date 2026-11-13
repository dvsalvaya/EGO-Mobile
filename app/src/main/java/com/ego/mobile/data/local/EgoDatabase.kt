package com.ego.mobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ego.mobile.data.local.dao.HabitDao
import com.ego.mobile.data.local.dao.HabitOccurrenceDao
import com.ego.mobile.data.local.dao.TaskDao
import com.ego.mobile.data.local.dao.WorkoutDao
import com.ego.mobile.data.local.entity.HabitEntity
import com.ego.mobile.data.local.entity.HabitOccurrenceEntity
import com.ego.mobile.data.local.entity.TaskEntity
import com.ego.mobile.data.local.entity.WorkoutExerciseEntity
import com.ego.mobile.data.local.entity.WorkoutSessionEntity

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `habit_occurrences` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`habitId` INTEGER NOT NULL, " +
                "`dateCivil` TEXT NOT NULL, " +
                "`status` TEXT NOT NULL, " +
                "`completedAt` INTEGER, " +
                "`notes` TEXT, " +
                "`createdAt` INTEGER NOT NULL, " +
                "`updatedAt` INTEGER NOT NULL, " +
                "FOREIGN KEY(`habitId`) REFERENCES `habits`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_habit_occurrences_habitId_dateCivil` ON `habit_occurrences` (`habitId`, `dateCivil`)"
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `tasks` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`title` TEXT NOT NULL, " +
                "`description` TEXT, " +
                "`dueAt` INTEGER, " +
                "`status` TEXT NOT NULL, " +
                "`createdAt` INTEGER NOT NULL, " +
                "`updatedAt` INTEGER NOT NULL, " +
                "`completedAt` INTEGER)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_status` ON `tasks` (`status`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_dueAt` ON `tasks` (`dueAt`)")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `workout_sessions` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`title` TEXT NOT NULL, " +
                "`startedAt` INTEGER NOT NULL, " +
                "`endedAt` INTEGER, " +
                "`status` TEXT NOT NULL, " +
                "`notes` TEXT)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_sessions_status` ON `workout_sessions` (`status`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_sessions_startedAt` ON `workout_sessions` (`startedAt`)")

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `workout_exercises` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`sessionId` INTEGER NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`orderIndex` INTEGER NOT NULL, " +
                "`setsCount` INTEGER NOT NULL, " +
                "`reps` TEXT, " +
                "`weightKg` REAL, " +
                "`notes` TEXT, " +
                "FOREIGN KEY(`sessionId`) REFERENCES `workout_sessions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_exercises_sessionId` ON `workout_exercises` (`sessionId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_exercises_sessionId_orderIndex` ON `workout_exercises` (`sessionId`, `orderIndex`)")
    }
}

@Database(
    entities = [
        HabitEntity::class,
        HabitOccurrenceEntity::class,
        TaskEntity::class,
        WorkoutSessionEntity::class,
        WorkoutExerciseEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
abstract class EgoDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitOccurrenceDao(): HabitOccurrenceDao
    abstract fun taskDao(): TaskDao
    abstract fun workoutDao(): WorkoutDao
}
