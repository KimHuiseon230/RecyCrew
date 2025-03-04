package com.piooda.data.repositoryImpl

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.piooda.data.model.Content
import com.piooda.data.repository.question.ContentRepository
import com.piooda.data.repositoryImpl.ContentMapper.Companion.toMap
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ContentRepositoryImpl(
    private val db: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
) : ContentRepository { //  인터페이스 구현 추가
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

    private fun generateSearchIndex(text: String): List<String> {
        val indexList = mutableListOf<String>()
        text.lowercase().split(" ").forEach { word ->
            for (i in 1..word.length) {
                indexList.add(word.substring(0, i))
            }
        }
        return indexList
    }

    override suspend fun insert(content: Content): Boolean = try {
        val searchIndex = generateSearchIndex("${content.title} ${content.content}") // 🔥 검색 인덱스 생성

        val postRef = postsCollection.document()
        val postData = content.copy(id = postRef.id).toMap().toMutableMap().apply {
            this["searchIndex"] = searchIndex // 🔹 검색 인덱스 필드 추가
        }

        postRef.set(postData).await()

        Log.d("Firestore", " 게시글 저장 완료! ID: ${postRef.id}, SearchIndex: $searchIndex")
        true
    } catch (e: Exception) {
        Log.e("Firestore", "❌ 게시글 저장 오류: ${e.message}")
        false
    }

    override suspend fun update(content: Content): Boolean {
        return try {
            val postRef = FirebaseFirestore.getInstance()
                .collection("content")
                .document(content.id ?: return false)

            //  Firestore에서 현재 데이터 로드
            val snapshot = postRef.get().await()
            val currentLikeCount = snapshot.getLong("likeCount")?.toInt() ?: 0

            // 🔥 새롭게 검색 인덱스를 생성 (title, content 기반)
            val updatedSearchIndex = generateSearchIndex("${content.title} ${content.content}")

            //  Firestore에 업데이트할 데이터 맵 생성
            val updateData = mutableMapOf<String, Any>(
                "likeCount" to currentLikeCount + 1, // 좋아요 수 증가
                "searchIndex" to updatedSearchIndex  // 🔥 검색 인덱스 업데이트
            )

            //  Firestore에 업데이트 실행
            postRef.update(updateData).await()

            Log.d("Firestore", " 게시글 업데이트 완료! ID: ${content.id}, SearchIndex: $updatedSearchIndex")
            true
        } catch (e: Exception) {
            Log.e("ContentUseCase", "❌ 게시글 업데이트 실패: ${e.message}")
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

    //  댓글 불러오기 (Firestore)
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

    //  댓글 추가 (Firestore)
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

    override fun observeContentList(): Flow<List<Content>> = callbackFlow {
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
    override fun getContentById(contentId: String): Flow<Content> = callbackFlow {
        val docRef = db.collection("content").document(contentId)

        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            snapshot?.toObject(Content::class.java)?.let { content ->
                trySend(content).isSuccess
            }
        }

        awaitClose { listener.remove() }
    }

}