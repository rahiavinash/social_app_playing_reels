package com.wedo8020.app.mainui.home.chatAndNotification.roomDBReaction

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ReactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun emoJiReaction(reaction: ReactionEntity)


    @Query("SELECT * FROM reaction_table")
    fun fetchAllEmoJi():List<ReactionEntity>


    @Query("SELECT COUNT(*) FROM reaction_table")
    suspend fun getRowEmoJiCount(): Int


    @Update
    suspend fun updateRow(entity: ReactionEntity)

}