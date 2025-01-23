package com.piooda.recycrew.feature.community.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ConcatAdapter
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.piooda.UiState
import com.piooda.data.model.Content
import com.piooda.recycrew.core.BaseFragment
import com.piooda.recycrew.core.ViewModelFactory
import com.piooda.recycrew.databinding.FragmentQuestionDetailBinding
import com.piooda.recycrew.feature.community.adapter.QuestionCommentRecyclerAdapter
import com.piooda.recycrew.feature.community.adapter.QuestionDetailRecyclerAdapter
import com.piooda.recycrew.feature.community.viewmodel.QuestionDetailsViewModel
import kotlinx.coroutines.launch

class QuestionDetailFragment :
    BaseFragment<FragmentQuestionDetailBinding>(FragmentQuestionDetailBinding::inflate) {

    private val sharedViewModel by activityViewModels<QuestionDetailsViewModel>() // 🔥 Shared ViewModel
    private val viewModel by viewModels<QuestionDetailsViewModel> {
        ViewModelFactory(requireContext())
    }

    private lateinit var postAdapter: QuestionDetailRecyclerAdapter
    private lateinit var commentAdapter: QuestionCommentRecyclerAdapter
    private val args: QuestionDetailFragmentArgs by navArgs()
    private val content: Content by lazy { args.detailedQuestPostData } // ✅ 게시물 데이터 직접 사용

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

        binding.backButton.setOnClickListener { findNavController().popBackStack() }

        // ✅ 게시글 정보 바로 표시 (Firestore 재조회 X)
        postAdapter.submitList(listOf(content))

        // ✅ 댓글 불러오기 (Firestore 호출)
        content.id?.let { viewModel.loadComments(it) }
        content.id?.let{ arguments?.getString("CONTENT_ID")}  // ✅ 전달받은 게시물 ID 가져오기

        content.id?.let{viewModel.loadContentDetail(it) }  // ✅ ViewModel에서 데이터 불러오기

        // ✅ 댓글 작성 기능
        binding.commentFiled.setEndIconOnClickListener {
            val commentText = binding.commentFiled.editText?.text.toString().trim()
            if (commentText.isNotEmpty()) {
                val currentUser = FirebaseAuth.getInstance().currentUser
                val userName = currentUser?.displayName ?: "Anonymous"
                val newComment = Content.Comment(
                    author = userName,
                    content = commentText,
                    timestamp = Timestamp.now()
                )
                content.id?.let { it1 -> viewModel.addCommentToPost(it1, newComment) }
                binding.commentFiled.editText?.text?.clear()
            } else {
                Log.d("addCommentToPost", "Comment field is empty")
            }
        }

        // ✅ 댓글 데이터 관찰 (StateFlow → collect 사용)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.commentList.collect { comments ->
                commentAdapter.submitList(comments)
            }
        }

        // ✅ 게시물 삭제 처리 감지 (StateFlow → collect 사용)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                if (state is UiState.Success) {
                    findNavController().popBackStack() // 삭제 후 뒤로가기
                }
            }
        }
    }


    private fun concatAdapter() {
        postAdapter = QuestionDetailRecyclerAdapter(
            onEditClick = { navigateToEditPost(content) }, // ✅ 수정 시 기존 데이터 그대로 전달
            onDeleteClick = { content.id?.let { it1 -> viewModel.deletePost(it1) } } // ✅ 삭제는 ViewModel에서 처리
        )
        commentAdapter = QuestionCommentRecyclerAdapter()
        binding.rvQuestionDetail.adapter = ConcatAdapter(postAdapter, commentAdapter)
    }

    fun navigateToEditPost(content: Content) {
        val intent = Intent(context, QuestionPostEditActivity::class.java).apply {
            putExtra("content", content)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // ✅ 메모리 누수 방지
    }
}
