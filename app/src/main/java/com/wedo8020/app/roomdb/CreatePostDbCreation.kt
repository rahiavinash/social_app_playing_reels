package com.wedo8020.app.roomdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

private const val DB_NAME = "create_post_database.db"

@Database(
    entities = [CreatePostEntity::class],
    version = 9,
    exportSchema = false
)
abstract class CreatePostDbCreation : RoomDatabase() {
    abstract fun getCreatePostData(): CreatePostDao

    companion object {
        @Volatile
        private var instance: CreatePostDbCreation? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context): CreatePostDbCreation {
            return Room.databaseBuilder(
                context.applicationContext,
                CreatePostDbCreation::class.java,
                DB_NAME
            )
//                .addMigrations(MIGRATION_4_5)
                .fallbackToDestructiveMigration()
                .build()
        }

        private val MIGRATION_4_5 = object : Migration(5,7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Your migration code here to add the new column
                database.execSQL("ALTER TABLE create_post_table ADD COLUMN seekTime INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
