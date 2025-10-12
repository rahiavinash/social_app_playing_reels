package com.wedo8020.app.response

import com.wedo8020.app.utils.AppConstant

data class AccessSetting(
    val collab: String = AppConstant.NO_ONE,
    val message: String = AppConstant.NO_ONE,
    val seeArchive: String = AppConstant.NO_ONE,
    val seeFollower: String = AppConstant.NO_ONE,
    val seeFollowing: String = AppConstant.NO_ONE,
    val seeFriend: String = AppConstant.NO_ONE,
    val seeLikeGiven: String = AppConstant.NO_ONE,
    val seeLikeReceived: String = AppConstant.NO_ONE,
    val seeMyBio: String = AppConstant.NO_ONE,
    val seeMyProfile: String = AppConstant.NO_ONE,
    val seeTree: String = AppConstant.NO_ONE,
    val tag: String = AppConstant.NO_ONE
)