package com.wedo8020.app.utils

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.wedo8020.app.MainApplication

class Prefs {

    private var sharedPreferences: SharedPreferences = MainApplication.get().getSharedPreferences(APP_NAME,MODE_PRIVATE)
    init {
        instance = this
    }
    companion object {
        private const val APP_NAME = "WEDO80)"
        private const val IS_LOG_IN = "IS_LOG_IN"
        private const val IS_TEST = "IS_TEST"
        private const val ACCESS_TOKEN = "ACCESS_TOKEN"
        private const val REFRESH_TOKEN = "REFRESH_TOKEN"
        private const val FINAL_TAG_LISt = "FINAL_TAG_LISt"
        private const val PERMISSION_CLICK_GALLERY = "PERMISSION_CLICK_GALLERY"
        private const val USER_ID = "USER_ID"
        private const val NAME = "NAME"
        private const val USER_PROFILE = "USER_PROFILE"
        private const val USERNAME = "USERNAME"
        private const val PROFILE_FILE = "PROFILE_FILE"
        private const val REMOVE_POST_ID = "REMOVE_POST_ID"
        private const val REMOVE_POST_TYPE = "REMOVE_POST_TYPE"
        private const val REMOVE_SAVE_POST_ID = "REMOVE_SAVE_POST_ID"
        private const val REMOVE_ARCHIVE_POST_ID = "REMOVE_ARCHIVE_POST_ID"
        private const val AR_COUNT = "AR_COUNT"
        private const  val IS_LH_POST = "IS_LH_POST"
        private const  val  IS_GHOST_MODE = "IS_GHOST_MODE"
        private const  val  IS_APP_BACKGROUND = "IS_APP_BACKGROUND"
        private const val MESSAGE_SENDER_ID = "MESSAGE_SENDER_ID"
        private var instance: Prefs? = null
        fun init(): Prefs {
            if (instance == null) {
                instance = Prefs()
            }
            return instance!!
        }
    }
    fun clear() {
        sharedPreferences.edit().clear().apply()
    }

    var isLogIn: Boolean
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getBoolean(IS_LOG_IN, false)
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putBoolean(IS_LOG_IN, value).apply()
        }
    var arCount: Float
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getFloat(AR_COUNT, 0.0f)
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putFloat(AR_COUNT, value).apply()
        }
    var isTest: Boolean
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getBoolean(IS_TEST, true)
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putBoolean(IS_TEST, value).apply()
        }
    var tokenExpired: Boolean
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getBoolean(IS_LOG_IN, false)
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putBoolean(IS_LOG_IN, value).apply()
        }
    var accessToken: String
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getString(ACCESS_TOKEN, "") ?: ""
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putString(ACCESS_TOKEN, value).apply()
        }
    var refreshToken: String
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getString(REFRESH_TOKEN, "") ?: ""
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putString(REFRESH_TOKEN, value).apply()
        }
    var isLHPost: Boolean
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getBoolean(IS_LH_POST, false)
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putBoolean(IS_LH_POST, value).apply()
        }
    var isGhostMode: Boolean
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getBoolean(IS_GHOST_MODE, false)
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putBoolean(IS_GHOST_MODE, value).apply()
        }

    var TagList: String
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getString(FINAL_TAG_LISt, "") ?: ""
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putString(FINAL_TAG_LISt, value).apply()
        }

    var permissionClickGallery: Int
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getInt(PERMISSION_CLICK_GALLERY, 0) ?: 0
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putInt(PERMISSION_CLICK_GALLERY, value).apply()
        }
    var userId: String
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getString(USER_ID, "") ?: ""
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putString(USER_ID, value).apply()
        }

    var name: String
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getString(NAME, "") ?: ""
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putString(NAME, value).apply()
        }

    var username: String
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getString(USERNAME, "") ?: ""
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putString(USERNAME, value).apply()
        }

    var userProfile: String
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getString(USER_PROFILE, "") ?: ""
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putString(USER_PROFILE, value).apply()
        }


    var profileId: String
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getString(PROFILE_FILE, "") ?: ""
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putString(PROFILE_FILE, value).apply()
        }

    var removeActivePostId: String
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getString(REMOVE_POST_ID, "") ?: ""
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putString(REMOVE_POST_ID, value).apply()
        }
    var removePostType: String
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getString(REMOVE_POST_TYPE, "") ?: ""
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putString(REMOVE_POST_TYPE, value).apply()
        }

    var removeSavePostId: String
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getString(REMOVE_SAVE_POST_ID, "") ?: ""
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putString(REMOVE_SAVE_POST_ID, value).apply()
        }
    var removeArchivePostId: String
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getString(REMOVE_ARCHIVE_POST_ID, "") ?: ""
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putString(REMOVE_ARCHIVE_POST_ID, value).apply()
        }
    var isAppBackground: Boolean
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getBoolean(IS_APP_BACKGROUND, false)
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putBoolean(IS_APP_BACKGROUND, value).apply()
        }

    var messageSenderId: String
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getString(MESSAGE_SENDER_ID, "") ?: ""
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putString(MESSAGE_SENDER_ID, value).apply()
        }
}