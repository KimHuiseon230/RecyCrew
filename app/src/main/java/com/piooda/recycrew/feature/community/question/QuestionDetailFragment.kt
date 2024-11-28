package com.piooda.recycrew.feature.community.question

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ConcatAdapter
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.piooda.data.model.Comment
import com.piooda.data.model.PostData
import com.piooda.recycrew.common.UiState
import com.piooda.recycrew.core_ui.base.BaseFragment
import com.piooda.recycrew.core_ui.base.ViewModelFactory
import com.piooda.recycrew.databinding.FragmentQuestionDetailBinding
import com.piooda.recycrew.feature.community.question.adapter.QuestionCommentRecyclerAdapter
import com.piooda.recycrew.feature.community.question.adapter.QuestionDetailRecyclerAdapter
import kotlinx.coroutines.launch

class QuestionDetailFragment :
    BaseFragment<FragmentQuestionDetailBinding>(FragmentQuestionDetailBinding::inflate) {

    private val viewModel by viewModels<QuestionDetailsViewModel> {
        ViewModelFactory(requireContext())
    }

    private lateinit var postAdapter: QuestionDetailRecyclerAdapter
    private lateinit var commentAdapter: QuestionCommentRecyclerAdapter
    private val args: QuestionDetailFragmentArgs by navArgs()  // 자동으로 생성된 NavArgs 클래스

    private val postData: PostData by lazy { args.detailedQuestPostData }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentQuestionDetailBinding.inflate(inflater, container, false)
        concatAdapter()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observePostData()
        binding.backButton.setOnClickListener { requireActivity().onBackPressed() }
        viewModel.loadPostAndComments(postData.postId)

        binding.commentFiled.setEndIconOnClickListener {
            val commentText = binding.commentFiled.editText?.text.toString().trim()
            if (commentText.isNotEmpty()) {
                val currentUser = FirebaseAuth.getInstance().currentUser
                val userName = currentUser?.displayName ?: "Anonymous"
                val newComment = Comment(
                    author = userName, // 실제 사용자 이름으로 대체
                    content = commentText,
                    timestamp = Timestamp.now()
                )
                viewModel.addCommentToPost(postData.postId, newComment)
                binding.commentFiled.editText?.text?.clear()
            } else {
                Log.d("addCommentToPost", "Comment field is empty")
            }
        }
        // 삭제 및 수정 구현 코드 라인

    }

    private fun concatAdapter() {
        postAdapter = QuestionDetailRecyclerAdapter(
            onEditClick = { navigateToEditPost(postData) },
            onDeleteClick = { viewModel.deletePost(postData.postId) }
        ).apply { submitList(listOf(postData)) }
        commentAdapter = QuestionCommentRecyclerAdapter().apply { submitList(emptyList()) }
        binding.rvQuestionDetail.adapter = ConcatAdapter(postAdapter, commentAdapter)
    }

    fun navigateToEditPost(postData: PostData) {
        // 포스트 수정 화면으로 네비게이션
        val intent = Intent(context, QuestionPostEditActivity::class.java).apply {
            putExtra("postData", postData) // postData는 Parcelable
        }
        startActivity(intent)
    }
    private fun observePostData() {
        viewLifecycleOwner.lifecycleScope.launch {
            val progressBar = binding.progressBar
            progressBar.visibility = View.GONE
            try {
                // 게시글과 댓글 상태를 동시에 처리
                launch {
                    viewModel.postState.collect { uiState ->
                        when (uiState) {
                            is UiState.Loading -> {
                                progressBar.visibility = View.VISIBLE
                            }

                            is UiState.Success -> {
                                progressBar.visibility = View.GONE
                                uiState.resultData?.let { post ->
                                    // 게시글 데이터를 postAdapter에 제출
                                    postAdapter.submitList(listOf(post))
                                    // 댓글 로드
                                    viewModel.loadPostAndComments(post.postId)
                                }
                            }

                            is UiState.Error -> {
                                progressBar.visibility = View.GONE
                                Log.e("PostData", "Error fetching post data: ${uiState.exception}")
                            }

                            UiState.Empty -> {
                                progressBar.visibility = View.GONE
                                Toast.makeText(
                                    requireContext(),
                                    "No data available",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }

                launch {
                    viewModel.commentsState.collect { uiState ->
                        when (uiState) {
                            is UiState.Loading -> {
                                // 댓글 로딩 중 처리 (선택적으로 로딩 표시)
                            }

                            is UiState.Success -> {
                                // 댓글 성공 시 commentAdapter에 리스트 제출
                                commentAdapter.submitList(uiState.resultData)
                            }

                            is UiState.Error -> {
                                // 댓글 로딩 오류 처리
                                Log.e(
                                    "CommentData",
                                    "Error fetching comments: ${uiState.exception.message}"
                                )
                            }

                            UiState.Empty -> {
                                // 댓글이 없을 때 처리
                                Log.d("CommentData", "No comments available")
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e("PostData", "Unexpected error: ${e.message}")
                progressBar.visibility = View.GONE
            }
        }
    }
}
