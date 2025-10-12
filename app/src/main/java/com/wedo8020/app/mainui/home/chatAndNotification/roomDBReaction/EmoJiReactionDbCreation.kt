package com.wedo8020.app.mainui.home.chatAndNotification.roomDBReaction

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

private const val DB_NAME = "emoji_reaction_dataBase.db"

@Database(
    entities = [ReactionEntity::class],
    version = 1,
    exportSchema = false
)

abstract class EmoJiReactionDbCreation : RoomDatabase() {
    abstract fun addReaction(): ReactionDao

    companion object {
        @Volatile
        private var instance: EmoJiReactionDbCreation? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context): EmoJiReactionDbCreation {
            return Room.databaseBuilder(
                context.applicationContext,
                EmoJiReactionDbCreation::class.java,
                DB_NAME
            ).build()
        }


    }
}