package com.piooda.data.repositoryImpl

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.piooda.data.model.Comment
import com.piooda.data.model.PostData
import com.piooda.data.repository.PostDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class PostDataRepositoryImpl(
    private val db: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
) : PostDataRepository {
    private val postsCollection = db.collection("content")
    private val storageRef = firebaseStorage.reference


    override fun postdateLikeCount(postId: String, isLiked: Boolean): Flow<Boolean> = flow {
        val postRef = postsCollection.document(postId)

        try {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(postRef)
                val currentLikeCount = snapshot.getLong("likeCount")?.toInt() ?: 0
                val currentUserLiked = snapshot.getBoolean("userLiked") ?: false

                // 상태 변경 로직 개선
                val newLikeCount = if (currentUserLiked != isLiked) {
                    if (isLiked) currentLikeCount + 1 else (currentLikeCount - 1).coerceAtLeast(0)
                } else {
                    currentLikeCount
                }

                // 좋아요 상태와 카운트 함께 업데이트
                transaction.update(
                    postRef,
                    mapOf(
                        "likeCount" to newLikeCount,
                        "userLiked" to isLiked
                    )
                )
            }.await()

            emit(true) // 작업 성공
        } catch (e: Exception) {
            emit(false) // 작업 실패
            throw e
        }
    }


    override fun createPost(postData: PostData): Flow<Boolean> = flow {
        val postId = postData.postId // postId를 외부에서 생성하거나, 전달받은 값으로 설정
        val postRef = postsCollection.document(postId) // 문서 ID로 postId 사용
        postRef.set(postData) // 문서 생성, postId로 문서 ID가 설정됨
        emit(true) // 작업 성공 시 true 발행
    }.catch { throw it }


    override fun addCommentToPost(postId: String, comment: Comment): Flow<PostData> = flow {
        val commentId = UUID.randomUUID().toString()
        val commentsCollection = postsCollection.document(postId).collection("comments")
        commentsCollection.document(commentId).set(comment).await()
        val postSnapshot = postsCollection.document(postId).get().await()
        val updatedPost = postSnapshot.toObject(PostData::class.java)
            ?: throw Exception("Failed to retrieve updated post data")
        postsCollection.document(postId).update("commentCount", updatedPost.commentCount + 1)
            .await()
        emit(updatedPost.copy(commentCount = updatedPost.commentCount + 1))
    }.catch { e ->
        Log.e("Repository", "Error adding comment: ${e.localizedMessage}", e)
        throw e
    }


    override fun getComments(postId: String): Flow<List<Comment>> = flow {
        val commentsCollection = postsCollection.document(postId).collection("comments")

        // Firestore에서 댓글 가져오기
        val snapshot = commentsCollection.get().await()
        Log.d("FirestoreDebug", "Fetched ${snapshot.documents.size} comments")

        // 댓글 객체로 변환
        val comments = snapshot.documents.mapNotNull { doc ->
            val comment = doc.toObject(Comment::class.java)
            Log.d("FirestoreDebug", "Comment: $comment")
            comment
        }

        emit(comments) // 댓글 리스트 반환
    }.catch { e ->
        Log.e("Firestore", "Error fetching comments: ${e.localizedMessage}", e)
        emit(emptyList())
    }


    override fun getPostById(postId: String): Flow<PostData> = flow {
        val snapshot = postsCollection.document(postId).get().await()
        if (!snapshot.exists()) throw Exception("Post not found")

        val post = snapshot.toObject(PostData::class.java)
            ?: throw Exception("Post data is null")

        val imageUrl = getImageDownloadUrl(post.imagePath) ?: ""
        val enrichedPost = post.copy(imagePath = imageUrl)
        emit(enrichedPost)
    }.catch { throw it }

    override fun getAllPosts(): Flow<List<PostData>> = flow {
        val snapshot = postsCollection.orderBy("time").get().await()
        val posts = snapshot.toObjects(PostData::class.java)

        val enrichedPosts =
            posts.map {
                it.copy(
                    imagePath = getImageDownloadUrl(it.imagePath) ?: "",
                    commentCount = it.commentCount,
                    likeCount = it.likeCount
                )
            }
        emit(enrichedPosts)
    }.catch {
        throw it
    }

    override fun updatePost(postData: PostData): Flow<Boolean> = flow {
        val postRef = postsCollection.document(postData.postId)
        val postMap = mapOf(
            "title" to postData.title,
            "content" to postData.content,
            "updatedAt" to System.currentTimeMillis() // 수정 시간 추가
        )

        postRef.update(postMap).await() // Firestore에 Map 데이터를 업데이트
        emit(true) // 성공 시 true 발행
    }.catch { exception ->
        emit(false) // 실패 시 false 발행
        throw exception
    }


    override fun deletePost(postId: String): Flow<Boolean> = flow {
        val postRef = postsCollection.document(postId)
        // 문서가 존재하는지 확인하고 삭제 작업 수행
        val documentSnapshot = postRef.get().await()
        if (documentSnapshot.exists()) {
            postRef.delete() // delete()를 사용해 문서 삭제
            emit(true) // 삭제 성공 시 true 발행
        } else {
            emit(false) // 문서가 없으면 false 발행
        }
    }.catch { exception ->
        emit(false) // 예외 발생 시 false 발행
        throw exception
    }

    override fun getPostsByTitle(title: String): Flow<List<PostData>> = flow {
        val snapshot = postsCollection.whereEqualTo("title", title).get().await()
        val posts = snapshot.toObjects(PostData::class.java)
        emit(posts)
    }.catch { throw it }

    private suspend fun getImageDownloadUrl(imagePath: String): String? {
        return try {
            if (imagePath.isBlank() || imagePath.startsWith("https://")) {
                imagePath
            } else {
                storageRef.child(imagePath).downloadUrl.await().toString()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving image URL for path: $imagePath", e)
            null
        }
    }

    companion object {
        private const val TAG = "PostDataRepositoryImpl"
    }
}
