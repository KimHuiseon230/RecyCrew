package com.piooda.data.repository

import com.piooda.data.model.Comment
import com.piooda.data.model.PostData
import kotlinx.coroutines.flow.Flow

interface PostDataRepository {
    fun createPost(postData: PostData): Flow<Boolean>
    fun deletePost(postId: String): Flow<Boolean>
    fun updatePost(postData: PostData): Flow<Boolean>
    fun getPostById(postId: String): Flow<PostData>
    fun getAllPosts(): Flow<List<PostData>>
    fun getPostsByTitle(title: String): Flow<List<PostData>>
    fun addCommentToPost(postId: String, comment: Comment): Flow<PostData>
    fun getComments(postId: String): Flow<List<Comment>>
}
