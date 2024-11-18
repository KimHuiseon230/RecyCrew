package com.piooda.recycrew.feature.auth


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
import androidx.navigation.navArgs
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import com.piooda.data.model.PostData
import com.piooda.recycrew.core_ui.base.ViewModelFactory
import com.piooda.recycrew.databinding.ActivityInputBinding
import com.piooda.recycrew.feature.community.question.QuestionDetailsViewModel
import com.piooda.recycrew.feature.community.question.QuestionFragmentArgs
import java.util.UUID

class InputActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInputBinding
    private var selectedImageUri: Uri? = null
    private val MIME_TYPE_IMAGE = "image/*"
    private val TAG = "InputActivity" // 로그 태그 추가

    private val viewModel by viewModels<QuestionDetailsViewModel> { ViewModelFactory }

    private val args: QuestionFragmentArgs by navArgs()
    private val detailedPostData: PostData by lazy { args.detailedQuestData }

    private val pickSinglePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            if (uri != null) {
                Log.d(TAG, "Selected URI: $uri")
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
            Log.d(TAG, "Select image button clicked")
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
            Log.d(TAG, "Confirm button clicked")
            if (selectedImageUri != null) {
                Log.d(TAG, "Starting image upload with URI: $selectedImageUri")
                uploadImageAndCreatePost()
            } else {
                Toast.makeText(this, "이미지를 선택하세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadImageWithGlide(url: String) {
        Log.d(TAG, "Attempting to load image with URL: $url")
        try {
            Glide.with(this)
                .load(url)
                .into(binding.imagePreview)
            Log.d(TAG, "Image loaded successfully with Glide")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading image with Glide", e)
            Toast.makeText(this, "이미지 로딩 실패: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES),
                    PERMISSION_REQUEST_CODE
                )
            }
        } else {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permissions granted")
                Toast.makeText(this, "권한이 승인되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Log.e(TAG, "Permissions denied")
                Toast.makeText(this, "이미지 선택을 위해 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PHOTO_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                Log.d(TAG, "Image selected from onActivityResult: $uri")
                selectedImageUri = uri
                loadImageWithGlide(uri.toString())
            }
        }
    }

    private fun isPhotoPickerAvailable(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }

    private fun uploadImageAndCreatePost() {
        // 입력값 검증
        val title = binding.titleEdit.text.toString()
        val content = binding.contentEdit.text.toString()

        if (title.isEmpty()) {
            Toast.makeText(this, "제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (content.isEmpty()) {
            Toast.makeText(this, "내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        selectedImageUri?.let { uri ->
            try {
                // 로딩 표시 시작
                binding.confirmButton.isEnabled = false

                val storageRef = Firebase.storage.reference
                val imageRef = storageRef.child("sample/${System.currentTimeMillis()}.png")

                Log.d(TAG, "Starting upload to Firebase Storage")

                val uploadTask = imageRef.putFile(uri)

                uploadTask
                    .addOnProgressListener { taskSnapshot ->
                        val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                        Log.d(TAG, "Upload progress: $progress%")
                    }
                    .addOnSuccessListener { taskSnapshot ->
                        Log.d(TAG, "Upload successful: ${taskSnapshot.metadata?.path}")

                        // 업로드 후 URL 가져오기
                        getImageDownloadUrl(imageRef) { downloadUrl ->
                            if (downloadUrl.isNotEmpty()) {
                                val postData = PostData(
                                    postId = UUID.randomUUID().toString(),
                                    userId = "SampleUserId",
                                    title = title,
                                    content = content,
                                    imagePath = downloadUrl, // 여기에서 이미 String으로 받음
                                    userName = "Author Name",
                                    category = binding.categoryEdit.text.toString(),
                                    commentCount = 0,
                                    likeCount = 0,
                                    viewCount = 0,
                                    time = System.currentTimeMillis().toString(),
                                    comments = mutableListOf()
                                )

                                // Use the ViewModel to create the post
                                viewModel.createPost(postData)

                                // 화면 이동을 위한 코드 추가
                                Toast.makeText(this, "게시글이 성공적으로 등록되었습니다.", Toast.LENGTH_SHORT).show()
                                finish() // 현재 Activity 종료하여 이전 화면으로 돌아가기
                            } else {
                                handleError("다운로드 URL 획득 실패", Exception("URL is empty"))
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        handleError("이미지 업로드 실패", exception)
                    }
            } catch (e: Exception) {
                handleError("업로드 중 오류 발생", e)
            }
        }
    }
    private fun getImageDownloadUrl(imageRef: StorageReference, callback: (String) -> Unit) {
        // 비동기적으로 다운로드 URL 가져오기
        imageRef.downloadUrl.addOnSuccessListener { uri ->
            callback(uri.toString()) // Uri를 String으로 변환하여 callback에 전달
        }.addOnFailureListener { exception ->
            Log.e("TAG:Firebase", "Error getting download URL: ${exception.message}")
            callback("") // 실패 시 빈 문자열 반환
        }
    }
    private fun handleError(message: String, exception: Exception) {
        Log.e(TAG, "$message: ${exception.message}", exception)
        Toast.makeText(this, "$message: ${exception.message}", Toast.LENGTH_SHORT).show()
        binding.confirmButton.isEnabled = true
    }

    companion object {
        private const val PHOTO_PICKER_REQUEST_CODE = 101
        private const val PERMISSION_REQUEST_CODE = 102
    }
}
