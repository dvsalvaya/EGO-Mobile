package com.ego.mobile.data.local

import android.content.Context
import androidx.room.Room

object EgoDatabaseProvider {

    private const val DEFAULT_DATABASE_NAME = "ego_mobile.db"

    @Volatile
    private var instance: EgoDatabase? = null

    @Volatile
    private var currentDatabaseName: String? = null

    fun getDatabase(context: Context): EgoDatabase = getDatabase(context, DEFAULT_DATABASE_NAME)

    fun getDatabase(context: Context, databaseName: String): EgoDatabase {
        val cached = instance
        if (cached != null && currentDatabaseName == databaseName) {
            return cached
        }

        return synchronized(this) {
            if (instance == null || currentDatabaseName != databaseName) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    EgoDatabase::class.java,
                    databaseName,
                )
                    .addMigrations(MIGRATION_1_2)
                    .addMigrations(MIGRATION_2_3)
                    .addMigrations(MIGRATION_3_4)
                    .addMigrations(MIGRATION_4_5)
                    .addMigrations(MIGRATION_5_6)
                    .addMigrations(MIGRATION_6_7)
                    .addMigrations(MIGRATION_7_8)
                    .build()
                currentDatabaseName = databaseName
            }
            instance!!
        }
    }

    fun resetForTesting() {
        synchronized(this) {
            instance?.close()
            instance = null
            currentDatabaseName = null
        }
    }
}
