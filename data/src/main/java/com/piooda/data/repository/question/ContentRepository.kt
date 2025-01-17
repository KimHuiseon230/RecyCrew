package com.piooda.data.repository.question

import com.piooda.data.model.Content
import kotlinx.coroutines.flow.Flow

interface ContentRepository {
    fun loadList(): Flow<List<Content>>
    suspend fun insert(item: Content): Boolean
    suspend fun delete(item: Content): Boolean
    suspend fun update(content: Content): Boolean
    suspend fun getCommentsForPost(postId: String): List<Content.Comment>
    suspend fun addCommentToPost(postId: String, comment: Content.Comment)
}