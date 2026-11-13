package com.ego.mobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ego.mobile.data.local.dao.HabitDao
import com.ego.mobile.data.local.dao.HabitOccurrenceDao
import com.ego.mobile.data.local.entity.HabitEntity
import com.ego.mobile.data.local.entity.HabitOccurrenceEntity

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
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
        database.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_habit_occurrences_habitId_dateCivil` ON `habit_occurrences` (`habitId`, `dateCivil`)"
        )
    }
}

@Database(
    entities = [HabitEntity::class, HabitOccurrenceEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class EgoDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitOccurrenceDao(): HabitOccurrenceDao
}
