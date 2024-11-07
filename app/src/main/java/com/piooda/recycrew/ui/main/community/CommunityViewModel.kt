package com.piooda.recycrew.ui.main.community

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

data class ContentDto(
    val id: String? = null,
    val title: String,
    val content: String,
    val time: String,
    val name: String,
    val likeCount: Int? = null,
    val commentCount: Int? = null,
    val viewCount: Int? = null,
    val imagePath: String,
    val imageUrl: String
)

class CommunityViewModel : ViewModel() {

    private val _imageList = MutableLiveData<List<ContentDto>>()
    val imageList: LiveData<List<ContentDto>> = _imageList

    fun fetchImagesFromFirebase() {
        val db = FirebaseFirestore.getInstance()
        val storageRef = FirebaseStorage.getInstance().reference

        // Firestore에서 이미지 경로 가져오기
        db.collection("content")
            .get()
            .addOnSuccessListener { result ->
                val imageList = mutableListOf<ContentDto>()
                val tasks = mutableListOf<Task<Uri>>() // 모든 downloadUrl 작업을 저장할 리스트

                // Firestore에서 가져온 각 문서에 대해 처리
                for (document in result) {
                    val id = document.getString("id") ?: ""
                    val title = document.getString("title") ?: ""
                    val content = document.getString("content") ?: ""
                    val likeCount = document.getLong("likeCount")?.toInt() ?: 0
                    val commentCount = document.getLong("commentCount")?.toInt() ?: 0
                    val viewCount = document.getLong("viewCount")?.toInt() ?: 0
                    val imagePath = document.getString("imagePath") ?: ""
                    val name = document.getString("name") ?: ""
                    val time = document.getString("time") ?: ""

                    // Firebase Storage에서 이미지 다운로드 URL 가져오기
                    val task = storageRef.child(imagePath).downloadUrl
                        .addOnSuccessListener { uri ->
                            val imageUrl = uri.toString()
                            // 가져온 데이터를 리스트에 추가
                            imageList.add(
                                ContentDto(
                                    id,
                                    title,
                                    content,
                                    time,
                                    name,
                                    likeCount,
                                    commentCount,
                                    viewCount,
                                    imagePath,
                                    imageUrl
                                )
                            )
                        }
                        .addOnFailureListener { exception ->
                            Log.e("Firebase", "Error getting download URL: ${exception.message}")
                        }

                    // 각 작업을 리스트에 추가
                    tasks.add(task)
                }

                // 모든 작업이 완료될 때까지 기다린 후 UI 업데이트
                Tasks.whenAllComplete(tasks).addOnCompleteListener { taskResults ->
                    if (taskResults.isSuccessful) {
                        // num 값 기준으로 정렬 후 LiveData 업데이트
                        _imageList.value = imageList.sortedBy { it.title }
                    } else {
                        Log.e("Firebase", "Error fetching some or all images.")
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firebase", "Error getting documents: ", exception)
            }
    }
}