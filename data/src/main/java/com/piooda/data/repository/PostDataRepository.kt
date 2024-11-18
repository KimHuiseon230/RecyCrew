package com.piooda.data.repository

import com.piooda.data.model.Comment
import com.piooda.data.model.PostData

interface PostDataRepository {
    // 게시글 생성
    suspend fun createPost(postData: PostData): Result<List<PostData>>

    // 게시글 조회 (postId로 검색)
    suspend fun getPostById(postId: String): PostData

    // 모든 게시글 조회
    suspend fun getAllPosts(): Result<List<PostData>>

    // 게시글 업데이트
    suspend fun updatePost(postData: PostData): Boolean

    // 게시글 삭제 (postId로 삭제)
    suspend fun deletePost(postId: String): Result<Boolean>

    // 게시글 제목으로 검색
    suspend fun getPostsByTitle(title: String): Result<List<PostData>>

    suspend fun addCommentToPost(postId: String, comment: Comment): Boolean

    suspend fun getComments(postId: String): Result<List<Comment>>
}