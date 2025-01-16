package com.piooda.recycrew.feature.community.question

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
import com.piooda.data.model.Content
import com.piooda.recycrew.common.UiState
import com.piooda.recycrew.core_ui.base.ViewModelFactory
import com.piooda.recycrew.databinding.ActivityQuestionPostEditBinding
import kotlinx.coroutines.launch

class QuestionPostEditActivity : AppCompatActivity() {
//    private lateinit var binding: ActivityQuestionPostEditBinding
//    private val viewModel by viewModels<QuestionDetailsViewModel> {
//        ViewModelFactory(this)
//    }
//    private lateinit var content: Content
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityQuestionPostEditBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        // Intent로 전달받은 데이터 처리
//        content = intent.getParcelableExtra("content") ?: return
//
//        // 기존 데이터 UI에 반영
//        setupUI(content)
//
//        // 수정 버튼 클릭 이벤트 처리
//        binding.confirmButton.setOnClickListener {
//            updatePostData()
//        }
//
//        // 상태 관찰
//        observePostUpdate()
//    }
//
//    private fun setupUI(content: Content) {
//        binding.titleEdit.setText(content.title)
//        binding.contentEdit.setText(content.content)
//        binding.categoryEdit.setText(content.category)
//        Glide.with(binding.root.context)
//            .load(content.imagePath)
//            .into(binding.imagePreview)
//    }
//
//    private fun updatePostData() {
//        // 사용자가 입력한 데이터 가져오기
//        val updatedTitle = binding.titleEdit.text.toString().trim()
//        val updatedContent = binding.contentEdit.text.toString().trim()
//
//        // 필수값 검증
//        if (updatedTitle.isBlank() || updatedContent.isBlank()) {
//            Toast.makeText(this, "제목과 내용을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        // 기존 postData에서 수정된 데이터로 Content 생성
//        val updatedPost = content.copy(
//            title = updatedTitle,
//            content = updatedContent
//        )
//
//        // ViewModel로 수정 요청
//        viewModel.updatePost(updatedPost)
//
//        // UI 처리 (수정 요청 진행 중 버튼 비활성화)
//        binding.confirmButton.isEnabled = false
//    }
//
//    private fun observePostUpdate() {
//        lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                viewModel.postState.collect { state ->
//                    handlePostUpdateState(state)
//                }
//            }
//        }
//    }
//
//    private fun handlePostUpdateState(state: UiState<Content>) {
//        when (state) {
//            is UiState.Loading -> {
//                binding.progressBar.visibility = View.VISIBLE
//            }
//            is UiState.Success -> {
//                binding.progressBar.visibility = View.GONE
//                Toast.makeText(this, "수정되었습니다.", Toast.LENGTH_SHORT).show()
//                finish() // 수정 완료 후 화면 종료
//            }
//            is UiState.Error -> {
//                binding.progressBar.visibility = View.GONE
//                binding.confirmButton.isEnabled = true
//                Toast.makeText(
//                    this,
//                    "수정 실패: ${state.exception.message}",
//                    Toast.LENGTH_SHORT
//                ).show()
//                Log.e("PostUpdate", "Error: ${state.exception.message}")
//            }
//            else -> Unit
//        }
//    }
}
