package com.wedo8020.app.mainui.login.switchaccounts.roomdB

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.util.Locale

class SwitchAccountsRepo(
    private val switchAccountDb : SwitchAccountsDbCreation
) {

    suspend fun addAccount(switchAccount : SwitchAccountsEntity) = switchAccountDb.addAccount().addAccountDetails(switchAccount)

    suspend fun getAllAccounts() = switchAccountDb.addAccount().getAllAccounts()

    suspend fun getRows() = switchAccountDb.addAccount().getRowCount()

    suspend fun deleteAll() = switchAccountDb.addAccount().deleteAll()

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateRowIfNeeded(newData: SwitchAccountsEntity) {
        val allRows = switchAccountDb.addAccount().getAllAccounts()

        if (allRows.size >= 2) {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val sortedRows = allRows.sortedBy { row ->
                sdf.parse(row.loginTimestamp.toString())
            }

            val oldestInactiveRow = sortedRows[0]
            val secondOldestInactiveRow = sortedRows[1]
            Log.d("TimeDifference", "$oldestInactiveRow $secondOldestInactiveRow")

                // Update the oldest inactive row
                oldestInactiveRow.username = newData.username
                oldestInactiveRow.password = newData.password
                oldestInactiveRow.deviceId = newData.deviceId
                oldestInactiveRow.profile = newData.profile
                oldestInactiveRow.isLogin = newData.isLogin
                oldestInactiveRow.loginTimestamp = newData.loginTimestamp

                switchAccountDb.addAccount().updateRow(oldestInactiveRow)

        }
    }

    suspend fun userExists(userName : String): Int {
        return  switchAccountDb.addAccount().doesUsernameExist(userName)
    }

    suspend fun updateProfile(userName: String, profile : String){
        switchAccountDb.addAccount().updateProfileForUser(userName,profile)
    }
    suspend fun updatePassword(userName: String, password: String){
        switchAccountDb.addAccount().updatePassword(userName, password)
    }
    suspend fun updateUsername(userName: String, updatedUserName: String){
        switchAccountDb.addAccount().updateUsername(userName, updatedUserName)
    }

    suspend fun getUserByUsername(username : String) : SwitchAccountsEntity?{
       return switchAccountDb.addAccount().getUserByUsername(username)
    }

}