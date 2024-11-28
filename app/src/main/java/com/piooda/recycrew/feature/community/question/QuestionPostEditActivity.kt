package com.piooda.recycrew.feature.community.question

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.piooda.data.model.PostData
import com.piooda.recycrew.common.UiState
import com.piooda.recycrew.core_ui.base.ViewModelFactory
import com.piooda.recycrew.databinding.ActivityQuestionPostEditBinding
import kotlinx.coroutines.launch

class QuestionPostEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuestionPostEditBinding
    private val viewModel by viewModels<QuestionDetailsViewModel> {
        ViewModelFactory(this)
    }
    private lateinit var postData: PostData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionPostEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Intent로 전달받은 데이터 처리
        postData = intent.getParcelableExtra("postData") ?: return

        // 기존 데이터 UI에 반영
        setupUI(postData)

        // 수정 버튼 클릭 시
        binding.confirmButton.setOnClickListener {
            updatePostData()
        }
    }

    private fun setupUI(postData: PostData) {
        binding.titleEdit.setText(postData.title)
        binding.contentEdit.setText(postData.content)
        binding.categoryEdit.setText(postData.category)
        Glide.with(binding.root.context)
            .load(postData.imagePath)
            .into(binding.imagePreview)
    }

    private fun updatePostData() {
        // 사용자가 수정한 데이터 가져오기
        val updatedTitle = binding.titleEdit.text.toString().trim()
        val updatedContent = binding.contentEdit.text.toString().trim()

        // 필수값 검증 (예: 제목이나 내용이 비어 있으면 실패 처리)
        if (updatedTitle.isBlank() || updatedContent.isBlank()) {
            Toast.makeText(this, "제목과 내용을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // 기존 postData에서 수정된 데이터로 PostData 생성
        val updatedPost = postData.copy(
            title = updatedTitle,
            content = updatedContent
        )

        // ViewModel로 수정 요청
        viewModel.updatePost(updatedPost)

        // UI 처리 (수정 요청이 완료되기 전까지 버튼 비활성화 등)
        binding.confirmButton.isEnabled = false

        // 상태를 관찰하여 UI 업데이트
        observePostUpdate()
    }

    private fun observePostUpdate() {
        lifecycleScope.launch {
            viewModel.postState.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        // 로딩 중 UI 처리
                        binding.progressBar.visibility = View.VISIBLE
                    }

                    is UiState.Success -> {
                        // 수정 성공
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(
                            this@QuestionPostEditActivity,
                            "수정되었습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish() // 수정 후 화면 종료
                    }

                    is UiState.Error -> {
                        // 에러 발생
                        binding.progressBar.visibility = View.GONE
                        binding.confirmButton.isEnabled = true
                        Toast.makeText(
                            this@QuestionPostEditActivity,
                            "수정 실패: ${state.exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("observePostUpdate", "observePostUpdate: ${state.exception.message}")

                    }

                    else -> Unit
                }
            }
        }
    }

}
