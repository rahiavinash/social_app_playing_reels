package com.wedo8020.app.roomdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CreatePostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createPost(note: CreatePostEntity)


    @Query("SELECT * FROM create_post_table")
    fun fetchAllPost():List<CreatePostEntity>

    @Query("SELECT * FROM create_post_table WHERE id = :postId")
    fun fetchPost(postId: String):List<CreatePostEntity>

    @Query("DELETE FROM create_post_table")
    suspend fun deleteAll()

    @Query("DELETE FROM create_post_table WHERE id = :postId")
    suspend fun deleteItem(postId:String)



}