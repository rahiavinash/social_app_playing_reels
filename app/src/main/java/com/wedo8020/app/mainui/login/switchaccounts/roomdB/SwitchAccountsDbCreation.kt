package com.wedo8020.app.mainui.login.switchaccounts.roomdB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

private const val DB_NAME = "switch_account_database.db"

@Database(
    entities = [SwitchAccountsEntity::class],
    version = 3,
    exportSchema = false
)
abstract class SwitchAccountsDbCreation : RoomDatabase() {
    abstract fun addAccount(): SwitchAccountsDao

    companion object {
        @Volatile
        private var instance: SwitchAccountsDbCreation? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context): SwitchAccountsDbCreation {
            return Room.databaseBuilder(
                context.applicationContext,
                SwitchAccountsDbCreation::class.java,
                DB_NAME
            ).build()
        }


    }
}
