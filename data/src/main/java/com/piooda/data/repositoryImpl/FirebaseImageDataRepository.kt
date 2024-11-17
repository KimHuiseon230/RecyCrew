package com.piooda.data.repositoryImpl

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.piooda.data.model.DetailedImageData
import com.piooda.data.repository.ImageDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseImageDataRepository(
    private val db: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
) : ImageDataRepository {

    private val storageRef = firebaseStorage.reference

    // fetchBasicImagesData: 기본적인 데이터만 가져오기
    override suspend fun fetchBasicImagesData(): Result<List<DetailedImageData>> {
        return try {
            val imageList = fetchImageDataFromFirestore()
            val imageDataList = fetchImageDownloadUrls(imageList)
            // 성공적으로 데이터를 반환
            Result.success(imageDataList)
        } catch (e: Exception) {
            Log.e("ImageDataRepository", "Error fetching basic image data: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun fetchDetailedImageData(): Result<List<DetailedImageData>> {
        return try {
            // Firestore에서 이미지 데이터를 가져옵니다
            val imageList = fetchImageDataFromFirestore()
            val imageDataList = fetchImageDownloadUrls(imageList)

            // 성공적으로 데이터가 로드되었을 경우 반환
            Result.success(imageDataList)
        } catch (e: Exception) {
            Log.e("ImageDataRepository", "Error fetching detailed image data: ${e.message}")
            Result.failure(e)
        }
    }

    private suspend fun fetchImageDataFromFirestore(): List<DocumentSnapshot> {
        val result = db.collection("images").orderBy("num").get().await()
        return result.documents
    }

    private suspend fun fetchImageDownloadUrls(imageList: List<DocumentSnapshot>): List<DetailedImageData> {
        return withContext(Dispatchers.IO) {
            // 각 DocumentSnapshot을 map으로 처리하여 비동기적으로 다운로드 URL 가져오기
            imageList.map { document ->
                val title = document.getString("title") ?: ""
                val imagePath = document.getString("imagePath") ?: ""
                val num = document.getLong("num")?.toInt() ?: 0
                val detailItems = document.getString("detailItems") ?: ""
                val excludedItems = document.getString("excludedItems") ?: ""
                val categoryLabel = document.getString("categoryLabel") ?: ""

                // 이미지 URL을 동기적으로 가져오기
                val uri = getImageDownloadUrl(imagePath)
                DetailedImageData(
                    title = title,
                    imageUrl = uri,
                    num = num,
                    subcategory = detailItems,
                    excludedItems = excludedItems,
                    categoryLabel = categoryLabel
                )
            }
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