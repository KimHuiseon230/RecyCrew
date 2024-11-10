package com.piooda.domain.repositoryImpl

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.piooda.domain.model.ImageData
import com.piooda.domain.repository.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ImageRepositoryImpl: ImageRepository {
    private val db = FirebaseFirestore.getInstance()
    private val storageRef = FirebaseStorage.getInstance().reference

    override suspend fun fetchImageData(): Result<List<ImageData>> = runCatching {
        withContext(Dispatchers.IO) {
            val imageList = mutableListOf<ImageData>()

            try {
                // Firestore에서 이미지 경로 가져오기
                val result = db.collection("images").orderBy("num").get().await()
                val tasks = result.documents.mapNotNull { document ->
                    val title = document.getString("title") ?: ""
                    val imagePath = document.getString("imagePath") ?: ""
                    val num = document.getLong("num")?.toInt() ?: 0

                    // Firebase Storage에서 이미지 다운로드 URL 가져오기
                    storageRef.child(imagePath).downloadUrl.addOnSuccessListener { uri ->
                        imageList.add(ImageData(title, uri.toString(), num))
                    }.addOnFailureListener { exception ->
                        Log.e("Firebase", "Error getting download URL: ${exception.message}")
                    }.let { it }
                }
                // 모든 downloadUrl 작업을 완료하기까지 기다리기
                Tasks.whenAllComplete(tasks).await()
                imageList.sortedBy { it.num }
            } catch (e: Exception) {
                Log.e("Firebase", "Error fetching image data: ${e.message}")
                emptyList()
                
            }
        }
    }.onFailure { exception: Throwable ->
        throw exception
    }
}