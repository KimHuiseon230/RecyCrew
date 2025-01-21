package com.piooda.data.repository.question

import com.piooda.data.model.Content
import kotlinx.coroutines.flow.Flow

interface ContentRepository {
    fun loadList(): Flow<List<Content>>
    suspend fun insert(item: Content): Boolean
    suspend fun delete(item: String?): Boolean
    suspend fun update(content: Content): Boolean
    suspend fun getCommentsForPost(postId: String): Flow<List<Content.Comment>>
    suspend fun addCommentToPost(postId: String, comment: Content.Comment)
    suspend fun toggleLike(contentId: String, uid: String)
    suspend fun observeContentList(): Flow<List<Content>>

    }