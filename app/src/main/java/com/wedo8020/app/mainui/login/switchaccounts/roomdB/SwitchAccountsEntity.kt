package com.wedo8020.app.mainui.login.switchaccounts.roomdB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "switch_account_table")
data class SwitchAccountsEntity(@PrimaryKey(autoGenerate = true)
                                 val id: Int = 0,
                                var username: String?=null,
                                var password: String?=null,
                                var profile: String?=null,
                                var deviceId : String?=null,
                                var isLogin : Boolean?=null,
                                var loginTimestamp: String)