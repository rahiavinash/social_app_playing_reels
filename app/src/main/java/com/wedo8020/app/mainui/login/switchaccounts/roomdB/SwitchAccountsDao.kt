package com.wedo8020.app.mainui.login.switchaccounts.roomdB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update


@Dao
interface SwitchAccountsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAccountDetails(data: SwitchAccountsEntity)

    @Query("SELECT * FROM switch_account_table")
    suspend fun getAllAccounts(): List<SwitchAccountsEntity>


    @Query("SELECT COUNT(*) FROM switch_account_table")
    suspend fun getRowCount(): Int

    @Query("DELETE FROM switch_account_table")
    suspend fun deleteAll()

    @Update
    suspend fun updateRow(entity: SwitchAccountsEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM switch_account_table WHERE username = :username)")
    suspend fun doesUsernameExist(username: String): Int

    @Query("UPDATE switch_account_table SET profile = :newProfile WHERE username = :username")
    suspend fun updateProfileForUser(username: String, newProfile: String)

    @Query("UPDATE switch_account_table SET password = :password WHERE username = :username")
    suspend fun updatePassword(username: String, password: String)

    @Query("UPDATE switch_account_table SET username = :updatedUserName WHERE username = :username")
    suspend fun updateUsername(username: String, updatedUserName: String)

    @Query("SELECT * FROM switch_account_table WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): SwitchAccountsEntity?


}
