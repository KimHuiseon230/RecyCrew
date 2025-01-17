package com.piooda.recycrew.feature.community.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import com.piooda.data.model.Content
import com.piooda.recycrew.core.ViewModelFactory
import com.piooda.recycrew.databinding.ActivityInputBinding
import com.piooda.recycrew.feature.community.viewmodel.QuestionViewModel
import java.util.UUID

class InputActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInputBinding
    private var selectedImageUri: Uri? = null
    private val MIME_TYPE_IMAGE = "image/*"
    private val TAG = "InputActivity"

    private val viewModel by viewModels<QuestionViewModel> { ViewModelFactory(this) }

    private val pickSinglePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            if (uri != null) {
                loadImageWithGlide(uri.toString())
                selectedImageUri = uri
            } else {
                Toast.makeText(this, "이미지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAndRequestPermissions()

        binding.selectImageButton.setOnClickListener {
            if (isPhotoPickerAvailable()) {
                pickSinglePhotoLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            } else {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    type = MIME_TYPE_IMAGE
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
                startActivityForResult(intent, PHOTO_PICKER_REQUEST_CODE)
            }
        }

        binding.confirmButton.setOnClickListener {
            uploadImageAndCreatePost()
        }
    }

    private fun loadImageWithGlide(url: String) {
        Glide.with(this).load(url).into(binding.imagePreview)
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES), PERMISSION_REQUEST_CODE)
            }
        } else {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
            }
        }
    }

    // ✅ 이미지 없이도 업로드 가능하게 수정
    private fun uploadImageAndCreatePost() {
        val title = binding.titleEdit.text.toString()
        val contentText = binding.contentEdit.text.toString()
        val category = binding.categoryEdit.text.toString()

        if (title.isEmpty()) {
            Toast.makeText(this, "제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (contentText.isEmpty()) {
            Toast.makeText(this, "내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: "UnknownUser"
        val userName = currentUser?.displayName ?: "Anonymous"

        // ✅ 이미지가 선택되지 않았을 경우, 텍스트만 업로드
        if (selectedImageUri == null) {
            val newContent = Content(
                id = UUID.randomUUID().toString(),
                title = title,
                content = contentText,
                imagePath = "",  // ✅ 이미지가 없으므로 빈 문자열 저장
                category = category,
                commentCount = 0,
                favoriteCount = 0,
                viewCount = 0
            )
            viewModel.insert(newContent)
            showToast("이미지 없이 게시글이 등록되었습니다.")
            finish()
            return
        }

        // ✅ 이미지가 선택되었을 경우 Firebase Storage 업로드
        val storageRef = Firebase.storage.reference.child("images/${System.currentTimeMillis()}.png")

        storageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    val newContent = Content(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        content = contentText,
                        imagePath = downloadUrl.toString(),  // ✅ 이미지 URL 저장
                        category = category,
                        commentCount = 0,
                        favoriteCount = 0,
                        viewCount = 0
                    )
                    viewModel.insert(newContent)
                    showToast("게시글이 성공적으로 등록되었습니다.")
                    finish()
                }
            }
            .addOnFailureListener { exception ->
                handleError("이미지 업로드 실패", exception)
            }
    }

    private fun getImageDownloadUrl(imageRef: StorageReference, callback: (String) -> Unit) {
        imageRef.downloadUrl.addOnSuccessListener { uri ->
            callback(uri.toString())
        }.addOnFailureListener {
            callback("")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun handleError(message: String, exception: Exception) {
        Log.e(TAG, "$message: ${exception.message}", exception)
        Toast.makeText(this, "$message: ${exception.message}", Toast.LENGTH_SHORT).show()
        binding.confirmButton.isEnabled = true
    }

    private fun isPhotoPickerAvailable(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }

    companion object {
        private const val PHOTO_PICKER_REQUEST_CODE = 101
        private const val PERMISSION_REQUEST_CODE = 102
    }
}