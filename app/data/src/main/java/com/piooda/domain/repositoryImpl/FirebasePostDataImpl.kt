package com.piooda.domain.repositoryImpl

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.piooda.domain.model.Comment
import com.piooda.domain.model.PostData
import com.piooda.domain.repository.PostDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class PostDataRepositoryImpl(
    private val db: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
) : PostDataRepository {

    private val postsCollection = db.collection("content")
    val commentRef = db.collection("content").document("postId").collection("comments")
    private val storageRef = firebaseStorage.reference

    override suspend fun createPost(postData: PostData): Result<List<PostData>> {
        return try {
            postsCollection.add(postData).await()
            val allPosts = getAllPosts().getOrNull() ?: emptyList()
            Result.success(allPosts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addCommentToPost(postId: String, comment: Comment): Boolean {
        return try {
            // postId를 사용해 commentsCollection을 참조
            val commentsCollection = postsCollection.document(postId).collection("comments")
            commentsCollection.add(comment).await()
            true
        } catch (e: Exception) {
            Log.e("addCommentToPost", "Error adding comment: ${e.message}")
            false
        }
    }

    override suspend fun getComments(postId: String): Result<List<Comment>> {
        return try {
            Log.d("getComments", "Fetching comments for postId: $postId")

            // 포스트의 comments 서브컬렉션을 가져옴
            val commentSnapshots = postsCollection
                .document(postId) // 포스트 문서 참조
                .collection("comments") // 'comments' 서브컬렉션 참조
                .get()
                .await()

            if (commentSnapshots.isEmpty) {
                Log.d("getComments", "No comments found for postId: $postId")
                return Result.success(emptyList())
            }

            // 댓글을 Comment 객체로 변환하여 리스트로 반환
            val commentsList = commentSnapshots.documents.mapNotNull { document ->
                document.toObject(Comment::class.java)
            }

            Result.success(commentsList)
        } catch (e: Exception) {
            Log.e("getComments", "Error getting comments for postId: $postId", e)
            Result.failure(e)
        }
    }

    override suspend fun getPostById(postId: String): PostData {
        Log.d("getPostById", "Fetching post with ID: $postId")

        return try {
            // 포스트 데이터를 가져옴
            val snapshot = postsCollection.document(postId).get().await()

            if (snapshot.exists()) {
                val post = snapshot.toObject(PostData::class.java)
                val imageUrl = getImageDownloadUrl(post!!.imagePath)

                // 댓글 가져오기
                Log.d("getPostById", "Fetching comments for postId: $postId")
                val result = getComments(postId)

                if (result.isFailure) {
                    Log.e("getPostById", "Failed to get comments for postId: $postId")
                }

                // 댓글을 가져온 후 처리
                val comments = result.getOrNull() ?: emptyList()

                // 댓글을 포함하여 포스트 반환
                post.copy(imagePath = imageUrl, comments = comments)
            } else {
                throw Exception("Post not found")
            }
        } catch (e: Exception) {
            Log.e("getPostById", "Error fetching post: ${e.message}")
            throw e
        }
    }

    override suspend fun getAllPosts(): Result<List<PostData>> {
        return try {
            val snapshot = postsCollection.orderBy("time").get().await()
            val posts = snapshot.toObjects(PostData::class.java)

            // Fetch image URLs for each post
            val postsWithImages = posts.map { post ->
                val imageUrl = getImageDownloadUrl(post.imagePath)
                post.copy(imagePath = imageUrl)
            }
            Result.success(postsWithImages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePost(postData: PostData): Boolean {
        return try {
            postsCollection.document(postData.postId.toString()).set(postData).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deletePost(postId: String): Boolean {
        return try {
            postsCollection.document(postId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getPostsByTitle(title: String): Result<List<PostData>> {
        return try {
            val snapshot = postsCollection.whereEqualTo("title", title).get().await()
            val posts = snapshot.toObjects(PostData::class.java)
            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getImageDownloadUrl(imagePath: String): String =
        withContext(Dispatchers.IO) {
            try {
                val uri = storageRef.child(imagePath).downloadUrl.await()
                uri.toString()
            } catch (e: Exception) {
                Log.e("Firebase", "Error getting download URL: ${e.message}")
                ""
            }
        }
}
