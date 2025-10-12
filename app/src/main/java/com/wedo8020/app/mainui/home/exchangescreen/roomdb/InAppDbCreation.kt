package com.wedo8020.app.mainui.home.exchangescreen.roomdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [InAppDataEntity::class],
    version = 3,
    exportSchema = false
)

abstract class InAppDbCreation: RoomDatabase() {
    abstract fun getInAppData(): InAppDataDao

    companion object {
        private const val DB_NAME = "in_app_database.db"
        @Volatile private var instance: InAppDbCreation? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            InAppDbCreation::class.java,
            DB_NAME
        ).fallbackToDestructiveMigration().build()
    }
}