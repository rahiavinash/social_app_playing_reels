package com.wedo8020.app.response

data class UserExperienceResponse(
    var data: ExperienceData?=null,
    var message: String?=null,
    var error: Int?=null,
    var success: Boolean=false
)

data class ExperienceData(
    val _id: String,
    val experienceSetting: InterestSetting,
    val filterWords: String?=null,
    val isSelect: Boolean
)

data class InterestSetting(
    val breakReminder: Boolean,
    val filterWord: Boolean,
    val isRestrictedMode: Boolean
)


