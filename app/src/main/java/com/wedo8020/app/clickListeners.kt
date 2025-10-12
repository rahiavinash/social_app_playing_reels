package com.wedo8020.app

interface clickListeners {
    fun itemClicked(position:Int)
}
interface postOptionListeners{
    fun ghostMode()
    fun notInterestedClick()
    fun userRestrictClick()
    fun userRepostClick()
    fun userReportClick()
    fun swapLayout()
    fun reportClick(reason : String, isPost: Boolean)
    fun downloadPost(position: Int)
    fun deletePost()
    fun archivePost()
    fun editPost()
    fun hashTagClick(text:String)
    fun mentionClick(text:String)
    fun postInfoClick(position: Int)
}