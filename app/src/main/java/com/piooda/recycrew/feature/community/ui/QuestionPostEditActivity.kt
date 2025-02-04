package com.piooda.recycrew.feature.community.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.piooda.UiState
import com.piooda.data.model.Content
import com.piooda.recycrew.core.ViewModelFactory
import com.piooda.recycrew.databinding.ActivityQuestionPostEditBinding
import com.piooda.recycrew.feature.community.viewmodel.QuestionDetailsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class QuestionPostEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuestionPostEditBinding
    private val viewModel by viewModels<QuestionDetailsViewModel> {
        ViewModelFactory(this)
    }
    private lateinit var content: Content
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionPostEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 현재 로그인한 사용자 ID 가져오기
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        // Intent로 전달받은 데이터 처리
        content = intent.getParcelableExtra("content") ?: return

        // UI 업데이트
        setupUI(content)

        // 수정 버튼 클릭 이벤트 처리
        binding.confirmButton.setOnClickListener {
            updatePostData()
        }

        // 삭제 버튼 클릭 이벤트 처리
//        binding.deleteButton.setOnClickListener {
//            deletePost()
//        }

        // 상태 관찰
        observePostUpdate()
    }

    private fun setupUI(content: Content) {
        binding.titleEdit.setText(content.title)
        binding.contentEdit.setText(content.content)
        Glide.with(binding.root.context)
            .load(content.imagePath)
            .into(binding.imagePreview)

        // 게시글 작성자와 현재 로그인한 사용자를 비교하여 권한 체크
        if (currentUserId != content.id) {
            binding.confirmButton.isEnabled = false
//            binding.deleteButton.isEnabled = false
            Toast.makeText(this, "수정 권한이 없습니다.", Toast.LENGTH_SHORT).show()
        } else {
            binding.confirmButton.isEnabled = true
//            binding.deleteButton.isEnabled = true
        }
    }

    private fun updatePostData() {
        if (currentUserId != content.id) {  // 🔥 권한 체크
            Toast.makeText(this, "게시글 수정 권한이 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedTitle = binding.titleEdit.text.toString().trim()
        val updatedContent = binding.contentEdit.text.toString().trim()

        if (updatedTitle.isBlank() || updatedContent.isBlank()) {
            Toast.makeText(this, "제목과 내용을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedPost = content.copy(
            title = updatedTitle,
            content = updatedContent
        )

        viewModel.updatePost(updatedPost)
        binding.confirmButton.isEnabled = false
    }

    private fun deletePost() {
        if (currentUserId != content.id) {  // 🔥 권한 체크
            Toast.makeText(this, "게시글 삭제 권한이 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.deletePost(content.id!!)
        finish()
    }

    private fun observePostUpdate() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.uiState.collectLatest { state ->
                    handlePostUpdateState(state)
                }
            }
        }
    }


    private fun handlePostUpdateState(state: UiState<Unit>) {
        when (state) {
            is UiState.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
            }

            is UiState.Success -> {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "수정되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }

            is UiState.Error -> {
                binding.progressBar.visibility = View.GONE
                binding.confirmButton.isEnabled = true
                Toast.makeText(
                    this,
                    "수정 실패: ${state.exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("PostUpdate", "Error: ${state.exception.message}")
            }

            else -> Unit
        }
    }
}
