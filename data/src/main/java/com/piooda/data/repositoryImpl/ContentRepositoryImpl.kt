package com.piooda.data.repositoryImpl

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.piooda.data.model.Content
import com.piooda.data.repository.question.ContentRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ContentRepositoryImpl(
    private val db: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
) : ContentRepository { // ✅ 인터페이스 구현 추가
    private val postsCollection = db.collection("content")

    // 🔹 게시글 목록 가져오기 (Flow 사용)
    override fun loadList(): Flow<List<Content>> = callbackFlow {
        val listener = postsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val contents = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Content::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            trySend(contents)
        }
        awaitClose { listener.remove() }
    }


    // ✅ 게시글 추가 (ID 자동 생성 및 안정성 개선)
    override suspend fun insert(content: Content): Boolean = try {
        val postRef = postsCollection.document()
        postRef.set(content.copy(id = postRef.id)).await()
        true
    } catch (e: Exception) {
        false
    }


    // 🔥 3. 게시글 업데이트 (ID 필요)
    override suspend fun update(content: Content): Boolean {
        return try {
            val postRef = FirebaseFirestore.getInstance()
                .collection("content")
                .document(content.id ?: return false)

            // ✅ Firestore에서 현재 데이터 로드 후 +1 (동시성 처리 개선)
            val snapshot = postRef.get().await()
            val currentLikeCount = snapshot.getLong("likeCount")?.toInt() ?: 0

            // ✅ Firestore에 정확한 값 업데이트
            postRef.update("likeCount", currentLikeCount + 1).await()
            true
        } catch (e: Exception) {
            Log.e("ContentUseCase", "Failed to update content: ${e.message}")
            false
        }
    }

    // 🔥 4. 게시글 삭제 (ID 필요)
    override suspend fun delete(postId: String?): Boolean = try {
        postId?.let {
            postsCollection.document(it).delete().await()
            true
        } ?: false
    } catch (e: Exception) {
        false
    }

    // ✅ 댓글 불러오기 (Firestore)
    override suspend fun getCommentsForPost(postId: String): Flow<List<Content.Comment>> = callbackFlow {
        val listener = postsCollection.document(postId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val comments = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Content.Comment::class.java)
                } ?: emptyList()
                trySend(comments)
            }
        awaitClose { listener.remove() }
    }

    // ✅ 댓글 추가 (Firestore)
    override suspend fun addCommentToPost(postId: String, comment: Content.Comment) {
        try {
            postsCollection.document(postId)
                .collection("comments")
                .add(comment)
                .await()
        } catch (e: Exception) {
            Log.e("ContentRepository", "Failed to add comment: ${e.message}")
        }
    }

    override suspend fun toggleLike(contentId: String, uid: String) {
        postsCollection.document(contentId).run {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(this)
                val content = snapshot.toObject(Content::class.java)
                    ?: throw Exception("Content not found")

                val newFavorites = content.favorites.toMutableMap()
                if (newFavorites.containsKey(uid)) {
                    newFavorites.remove(uid)
                    content.favoriteCount -= 1
                } else {
                    newFavorites[uid] = true
                    content.favoriteCount += 1
                }

                content.favorites = newFavorites
                transaction.set(this, content)
            }.await()
        }
    }

    override suspend fun observeContentList(): Flow<List<Content>> = callbackFlow {
        val listener = postsCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val contents = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Content::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(contents)
            }

        awaitClose { listener.remove() }
    }
}