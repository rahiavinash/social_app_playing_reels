package com.wedo8020.app.mainui.home.chatAndNotification.roomDBReaction

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "reaction_table")
data class ReactionEntity(@PrimaryKey(autoGenerate = true)
                                val id: Int = 0,
                                var reaction: String?=null,
                                var loginTimestamp: String)