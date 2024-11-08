package com.piooda.recycrew.ui.main.home

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

// 데이터 클래스 정의
data class ImageData(val title: String, val imageUrl: String, val num: Int)

class HomeViewModel : ViewModel() {

    private val _imageList = MutableLiveData<List<ImageData>>()
    val imageList: LiveData<List<ImageData>> = _imageList

    fun fetchImagesFromFirebase() {
        val db = FirebaseFirestore.getInstance()
        val storageRef = FirebaseStorage.getInstance().reference

        // Firestore에서 이미지 경로 가져오기
        db.collection("images")
            .orderBy("num") // 숫자 기준으로 정렬
            .get()
            .addOnSuccessListener { result ->
                val imageList = mutableListOf<ImageData>()
                val tasks = mutableListOf<Task<Uri>>() // 모든 downloadUrl 작업을 저장할 리스트

                // Firestore에서 가져온 각 문서에 대해 처리
                for (document in result) {
                    val title = document.getString("title") ?: ""
                    val imagePath = document.getString("imagePath") ?: ""
                    val num = document.getLong("num")?.toInt() ?: 0

                    // Firebase Storage에서 이미지 다운로드 URL 가져오기
                    val task = storageRef.child(imagePath).downloadUrl
                        .addOnSuccessListener { uri ->
                            val imageUrl = uri.toString()
                            // 가져온 데이터를 리스트에 추가
                            imageList.add(ImageData(title, imageUrl, num))
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
                        _imageList.value = imageList.sortedBy { it.num }
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

