package com.piooda.recycrew.feature.auth

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navArgs
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import com.piooda.data.model.PostData
import com.piooda.recycrew.core_ui.base.ViewModelFactory
import com.piooda.recycrew.databinding.ActivityInputBinding
import com.piooda.recycrew.feature.community.question.QuestionDetailsViewModel
import com.piooda.recycrew.feature.community.question.QuestionFragmentArgs
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID


class InputActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInputBinding
    private lateinit var uri: Uri

    private val viewModel by viewModels<QuestionDetailsViewModel> {
        ViewModelFactory
    }
    private val args: QuestionFragmentArgs by navArgs()
    private val detailedPostData: PostData by lazy {
        args.detailedQuestData
    }

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission granted, get images
                lifecycleScope.launch {
                    /*   try {
                           selectImage()
                       } catch (e: Exception) {
                           navigateToErrorPage(this@SignUpProfileActivity)
                       }*/
                }
            } else {
                /*Timber.tag("permission").d("권한 거부")
                showPermissionAppSettingsDialog()*/
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*       // 이미지 선택 버튼 클릭 시 갤러리로 이동
               binding.selectImageButton.setOnClickListener {
                   val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                   registerForActivityResult.launch(intent)
               }*/

        // 등록하기 버튼을 눌렀을 경우
        binding.confirmButton.setOnClickListener { createPostWithImage() }


    }

    private fun imageUpload(uri: Uri) {
        // storage 인스턴스 생성
        val storage = Firebase.storage
        // storage 참조
        val storageRef = storage.getReference("image")
        // storage에 저장할 파일명 선언
        val fileName = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        val mountainsRef = storageRef.child("${fileName}.png")

        val uploadTask = mountainsRef.putFile(uri)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            // 파일 업로드 성공
            Toast.makeText(this, "사진 업로드 성공", Toast.LENGTH_SHORT).show();
        }.addOnFailureListener {
            // 파일 업로드 실패
            Toast.makeText(this, "사진 업로드 실패", Toast.LENGTH_SHORT).show();
        }
    }

    private fun createPostWithImage() {
        val imageUrl = imageUpload(uri)
        val postData = PostData(
            postId = UUID.randomUUID().toString(),  // 게시글 ID
            userId = detailedPostData.postId,
            title = binding.titleEdit.toString(),
            content = binding.contentEdit.toString(),
            imagePath = imageUrl.toString(),  // 업로드된 이미지 URL
            userName = "Author Name",
            category = "General",
            commentCount = 0,
            likeCount = 0,
            viewCount = 0,
            time = System.currentTimeMillis().toString(),
            comments = mutableListOf()
        )

        // ViewModel을 통해 게시글 생성
        viewModel.createPost(postData)
    }

}