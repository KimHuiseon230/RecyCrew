package com.piooda.data.repositoryImpl

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.piooda.data.model.Comment
import com.piooda.data.model.PostData
import com.piooda.data.repository.PostDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class PostDataRepositoryImpl(
    private val db: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
) : PostDataRepository {
    private val postsCollection = db.collection("content")
    private val storageRef = firebaseStorage.reference


    override suspend fun createPost(postData: PostData): Result<List<PostData>> {
        return try {
            // Firestore에 게시물 데이터 추가
            val postRef = postsCollection.add(postData).await()

            // 게시글 하위에 빈 'comments' 컬렉션 생성

            val postId = postRef.id // 생성된 게시물의 ID

            // Firestore에 하위 'comments' 컬렉션을 빈 문서로 초기화
            val commentsCollection = postsCollection.document(postId).collection("comments")
            commentsCollection.add(hashMapOf<String, Any>())

            // 결과로 새로운 게시물 반환
            val createdPost = postData.copy(postId = postId)
            Result.success(listOf(createdPost))

        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun addCommentToPost(postId: String, comment: Comment): Boolean {
        return try {
            val commentsCollection = postsCollection.document(postId).collection("comments")

            // postId를 사용해 commentsCollection을 참조
            commentsCollection.add(comment).await()
            true
        } catch (e: Exception) {
            Log.e("TAG:addCommentToPost", "Error adding comment: ${e.message}")
            false
        }
    }

    override suspend fun getComments(postId: String): Result<MutableList<Comment>> {
        val commentList = mutableListOf<Comment>()
        return try {
            Log.d("TAG:getComments", "Post ID: $postId")

            val commentSnapshots = postsCollection
                .document(postId)
                .collection("comments")
                .get()
                .await()

            Log.d("TAG:getComments", "Number of comments found: ${commentSnapshots.size()}")

            for (snapshot in commentSnapshots) {
                val comment = snapshot.toObject(Comment::class.java)
                Log.d("TAG:getComments", "Snapshot data: ${snapshot.data}")
                Log.d("TAG:getComments", "Deserialized comment: $comment")

                commentList.add(comment)
            }

            Result.success(commentList)
        } catch (e: Exception) {
            Log.e("TAG:getComments", "Error getting comments for postId: $postId", e)
            Result.failure(e)
        }
    }

    override suspend fun getPostById(postId: String): PostData {
        Log.d(TAG, "Fetching post with ID: $postId")
        return try {
            val snapshot = postsCollection.document(postId).get().await()
            if (snapshot.exists()) {
                val post = snapshot.toObject(PostData::class.java)
                    ?: throw Exception("Post data is null")
                Log.d(TAG, "Snapshot exists: ${snapshot.exists()}")

                // Firebase Storage에서 다운로드 URL 가져오기
                val imageUrl = withContext(Dispatchers.IO) {
                    getImageDownloadUrl((post.imagePath))
                } ?: ""

                // 댓글 가져오기
                val comments = withContext(Dispatchers.IO) {
                    getComments(postId).getOrNull()?.toMutableList()
                } ?: mutableListOf()

                // 데이터 복사
                post.copy(imagePath = imageUrl, comments = comments)
            } else {
                throw Exception("Post not found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching post: ${e.message}", e)
            throw e
        }
    }


    override suspend fun getAllPosts(): Result<List<PostData>> {
        return try {
            val snapshot = postsCollection.orderBy("time").get().await()
            val posts = snapshot.toObjects(PostData::class.java)

            // 비동기적으로 모든 포스트에 대해 이미지 URL과 댓글 가져오기
            val postsWithImagesAndComments = posts.map { post ->
                withContext(Dispatchers.IO) {
                    // 이미지 URL 가져오기 (null일 경우 기본값 설정)
                    val imageUrl = getImageDownloadUrl(post.imagePath)

                    // 댓글 가져오기
                    val comments =
                        getComments(post.postId).getOrNull()?.toMutableList() ?: mutableListOf()

                    // 데이터 복사
                    post.copy(imagePath = imageUrl.toString(), comments = comments)
                }
            }

            Result.success(postsWithImagesAndComments)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching posts: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun updatePost(postData: PostData): Boolean {
        return try {
            postsCollection.document(postData.postId).set(postData).await()
            true
        } catch (e: Exception) {
            false
        }
    }
    // Repository에서 Boolean 값 반환 -> Result로 변환
    override suspend fun deletePost(postId: String): Result<Boolean> {
        return try {
            // 댓글 먼저 삭제
            val commentsCollection = postsCollection.document(postId).collection("comments")
            val commentSnapshots = commentsCollection.get().await()
            for (comment in commentSnapshots) {
                commentsCollection.document(comment.id).delete().await()
            }
            // 게시물 삭제
            postsCollection.document(postId).delete().await()

            Log.d("TAG:deletePost", "Deleted post with ID: $postId")  // 게시글 삭제 확인
            Result.success(true)  // 성공 시 Result.success로 감싸서 반환
        } catch (e: Exception) {
            Log.e("TAG:deletePost", "Error deleting post with ID $postId: ${e.message}", e)
            Result.failure(e)  // 실패 시 Result.failure로 감싸서 반환
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

    private suspend fun getImageDownloadUrl(imagePath: String): String? =
        withContext(Dispatchers.IO) {
            try {
                if (imagePath.isBlank()) {
                    Log.e(TAG, "Image path is blank.")
                    return@withContext null
                }

                // imagePath가 이미 다운로드 URL인지 확인
                if (imagePath.startsWith("https://")) {
                    Log.d(TAG, "Image path is already a download URL: $imagePath")
                    return@withContext imagePath
                }

                // Storage 경로를 사용해 다운로드 URL 생성
                val uri = storageRef.child(imagePath).downloadUrl.await()
                Log.d(TAG, "Download URL retrieved: $uri")
                uri.toString()
            } catch (e: Exception) {
                Log.e(TAG, "Error retrieving download URL for path: $imagePath", e)
                null
            }
        }


    companion object {
        private const val TAG = "Firebase"
    }
}
