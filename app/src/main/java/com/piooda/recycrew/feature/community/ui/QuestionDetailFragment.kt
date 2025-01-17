package com.piooda.recycrew.feature.community.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ConcatAdapter
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.piooda.data.model.Content
import com.piooda.recycrew.core.BaseFragment
import com.piooda.recycrew.core.ViewModelFactory
import com.piooda.recycrew.databinding.FragmentQuestionDetailBinding
import com.piooda.recycrew.feature.community.adapter.QuestionCommentRecyclerAdapter
import com.piooda.recycrew.feature.community.adapter.QuestionDetailRecyclerAdapter
import com.piooda.recycrew.feature.community.viewmodel.QuestionDetailsViewModel

class QuestionDetailFragment :
    BaseFragment<FragmentQuestionDetailBinding>(FragmentQuestionDetailBinding::inflate) {

    private val viewModel by viewModels<QuestionDetailsViewModel> {
        ViewModelFactory(requireContext())
    }

    private lateinit var postAdapter: QuestionDetailRecyclerAdapter
    private lateinit var commentAdapter: QuestionCommentRecyclerAdapter
    private val args: QuestionDetailFragmentArgs by navArgs()
    private val content: Content by lazy { args.detailedQuestPostData }

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

        binding.backButton.setOnClickListener { requireActivity().onBackPressed() }

        // ✅ 포스트 데이터를 어댑터에 바로 설정
        postAdapter.submitList(listOf(content))

        // ✅ 포스트 로드 후 댓글 별도 로드
        viewModel.loadComments(content.id)

        // ✅ 댓글 작성
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
                viewModel.addCommentToPost(content.id, newComment)
                binding.commentFiled.editText?.text?.clear()
            } else {
                Log.d("addCommentToPost", "Comment field is empty")
            }
        }

        // ✅ 댓글 데이터 관찰
        viewModel.commentList.observe(viewLifecycleOwner) { comments ->
            commentAdapter.submitList(comments)
        }
    }

    private fun concatAdapter() {
        postAdapter = QuestionDetailRecyclerAdapter(
            onEditClick = { navigateToEditPost(content) },
            onDeleteClick = { viewModel.deletePost(content) }
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

}
