package com.piooda.domain.repositoryImpl

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
        Log.d("TAG:getPostById", "Fetching post with ID: $postId")

        return try {
            // 포스트 데이터를 가져옴
            val snapshot = postsCollection.document(postId).get().await()

            if (snapshot.exists()) {
                val post = snapshot.toObject(PostData::class.java)
                val imageUrl = getImageDownloadUrl(post!!.imagePath) // 이미지 URL 비동기 처리

                // 댓글 가져오기
                Log.d("TAG:getPostById", "Fetching comments for postId: $postId")
                val result = getComments(postId)

                if (result.isFailure) {
                    Log.e("TAG:getPostById", "Failed to get comments for postId: $postId")
                }

                // 댓글을 가져온 후 처리
                val comments: MutableList<Comment> = result.getOrNull()?.toMutableList() ?: mutableListOf()

                // 댓글을 포함하여 포스트 반환
                post.copy(imagePath = imageUrl, comments = comments)
            } else {
                throw Exception("Post not found")
            }
        } catch (e: Exception) {
            Log.e("TAG:getPostById", "Error fetching post: ${e.message}")
            throw e
        }
    }
    override suspend fun getAllPosts(): Result<List<PostData>> {
        return try {
            val snapshot = postsCollection.orderBy("time").get().await()
            val posts = snapshot.toObjects(PostData::class.java)

            // 각 게시물에 대해 이미지와 댓글을 함께 가져옵니다.
            val postsWithImagesAndComments = posts.map { post ->
                val imageUrl = getImageDownloadUrl(post.imagePath) // 이미지 URL 비동기 처리

                // 댓글 가져오기
                val result = getComments(post.postId)
                Log.e("TAG:getAllPosts", "result: ${result}")

                if (result.isFailure) {
                    Log.e("TAG:getAllPosts", "Failed to get comments for postId: ${post.postId}")
                }

                val comments: MutableList<Comment> = result.getOrNull()?.toMutableList() ?: mutableListOf()

                // 이미지와 댓글이 포함된 PostData 객체 생성
                post.copy(imagePath = imageUrl, comments = comments)
            }

            Result.success(postsWithImagesAndComments)
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
                Log.e("TAG:Firebase", "Error getting download URL: ${e.message}")
                ""
            }
        }
}
