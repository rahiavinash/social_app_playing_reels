package com.wedo8020.app.roomdb

class CreatePostDbRepo(
    private val createPostDb : CreatePostDbCreation) {

    suspend fun createPost(createPost : CreatePostEntity) = createPostDb.getCreatePostData().createPost(createPost)


    suspend fun deleteAll() = createPostDb.getCreatePostData().deleteAll()
    suspend fun deleteItem(postId:String) = createPostDb.getCreatePostData().deleteItem(postId)


}