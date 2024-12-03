package com.piooda.recycrew.feature.community.question

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val progressBar = binding.progressBar

                // 게시글 상태 관찰
                launch {
                    viewModel.postState.collect { uiState ->
                        handlePostState(uiState, progressBar)
                    }
                }

                // 댓글 상태 관찰
                launch {
                    viewModel.commentsState.collect { uiState ->
                        handleCommentsState(uiState)
                    }
                }
            }
        }
    }

    private fun handlePostState(uiState: UiState<PostData>, progressBar: View) {
        when (uiState) {
            is UiState.Loading -> {
                progressBar.visibility = View.VISIBLE
            }

            is UiState.Success -> {
                progressBar.visibility = View.GONE
                uiState.resultData?.let { post ->
                    postAdapter.submitList(listOf(post))
                    viewModel.loadPostAndComments(post.postId)
                }
            }

            is UiState.Error -> {
                progressBar.visibility = View.GONE
                Log.e("PostData", "Error fetching post data: ${uiState.exception}")
            }

            UiState.Empty -> {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "No data available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleCommentsState(uiState: UiState<List<Comment>?>) {
        when (uiState) {
            is UiState.Loading -> {
                // 선택적으로 로딩 처리
            }

            is UiState.Success -> {
                commentAdapter.submitList(uiState.resultData)
            }

            is UiState.Error -> {
                Log.e("CommentData", "Error fetching comments: ${uiState.exception.message}")
            }

            UiState.Empty -> {
                Log.d("CommentData", "No comments available")
            }
        }
    }
}
