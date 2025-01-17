package com.piooda.data.repository.question

import com.piooda.data.model.Content
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class ContentUseCase @Inject constructor(
    private val contentRepository: ContentRepository // ✅ 인터페이스 사용
) {

     fun loadList(): Flow<List<Content>> {
        return contentRepository.loadList() // Flow는 이미 비동기 스트림 처리 가능
    }

    // ✅ Firestore에 데이터 저장
    suspend fun save(item: Content): Boolean {
        return try {
            contentRepository.insert(item)
        } catch (e: Exception) {
            false
        }
    }

    // ✅ Firestore 데이터 삭제
    suspend fun delete(item: Content): Boolean {
        return try {
            contentRepository.delete(item)
        } catch (e: Exception) {
            false
        }
    }

    // ✅ Firestore 데이터 업데이트 (성공 여부 반환)
    suspend fun updateContent(content: Content): Boolean {
        return try {
            contentRepository.update(content)
        } catch (e: Exception) {
            false
        }
    }

    // ✅ 특정 게시글의 댓글 가져오기
    suspend fun getCommentsForPost(postId: String): List<Content.Comment> {
        return try {
            contentRepository.getCommentsForPost(postId)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ✅ 특정 게시글에 댓글 추가
    suspend fun addCommentToPost(postId: String, comment: Content.Comment) {
        try {
            contentRepository.addCommentToPost(postId, comment)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
