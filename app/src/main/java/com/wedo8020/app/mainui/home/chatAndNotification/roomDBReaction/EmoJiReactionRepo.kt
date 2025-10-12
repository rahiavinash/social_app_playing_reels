package com.wedo8020.app.mainui.home.chatAndNotification.roomDBReaction

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.util.Locale


class EmoJiReactionRepo(
    private val emoJiReactionDbCreation: EmoJiReactionDbCreation
) {

    suspend fun addReaction(reactionEntity: ReactionEntity) = emoJiReactionDbCreation.addReaction().emoJiReaction(reactionEntity)

    suspend fun getAllEmoJi() = emoJiReactionDbCreation.addReaction().fetchAllEmoJi()



    suspend fun getRows() = emoJiReactionDbCreation.addReaction().getRowEmoJiCount()

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateRowIfNeeded(newData: ReactionEntity) {
        val allRows = emoJiReactionDbCreation.addReaction().fetchAllEmoJi()

        if (allRows.size >= 6) {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val sortedRows = allRows.sortedBy { row ->
                sdf.parse(row.loginTimestamp)
            }

            val oldestInactiveRow = sortedRows[0]


            // Update the oldest inactive row
            oldestInactiveRow.reaction = newData.reaction
            oldestInactiveRow.loginTimestamp = newData.loginTimestamp


            emoJiReactionDbCreation.addReaction().updateRow(oldestInactiveRow)

        }
    }



}