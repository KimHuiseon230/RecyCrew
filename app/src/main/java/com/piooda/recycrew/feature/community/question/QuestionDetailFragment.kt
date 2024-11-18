package com.piooda.recycrew.feature.community.question

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ConcatAdapter
import com.piooda.data.model.PostData
import com.piooda.recycrew.core_ui.base.BaseFragment
import com.piooda.recycrew.core_ui.base.UIState
import com.piooda.recycrew.core_ui.base.ViewModelFactory
import com.piooda.recycrew.databinding.FragmentQuestionDetailBinding
import com.piooda.recycrew.feature.community.question.adapter.QuestionCommentRecyclerAdapter
import com.piooda.recycrew.feature.community.question.adapter.QuestionDetailRecyclerAdapter

class QuestionDetailFragment :
    BaseFragment<FragmentQuestionDetailBinding>(FragmentQuestionDetailBinding::inflate) {

    private val viewModel by viewModels<QuestionDetailsViewModel> { ViewModelFactory }

    private lateinit var postAdapter: QuestionDetailRecyclerAdapter
    private lateinit var commentAdapter: QuestionCommentRecyclerAdapter
    private val args: QuestionFragmentArgs by navArgs()
    private val detailedPostData: PostData by lazy { args.detailedQuestData }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()

        // 초기 데이터 로드
        viewModel.loadData()

        binding.backButton.setOnClickListener { requireActivity().onBackPressed() }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuestionDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun setupRecyclerView() {
        postAdapter = QuestionDetailRecyclerAdapter { postId, imagePath ->
            viewModel.deletePost(detailedPostData.postId)
        }

        commentAdapter = QuestionCommentRecyclerAdapter()

        binding.rvQuestionDetail.adapter = ConcatAdapter(postAdapter, commentAdapter)
    }

    private fun setupObservers() {
        val progressBar = binding.progressBar

        lifecycleScope.launchWhenStarted {
            viewModel.q_postData.collect { state ->
                when (state) {
                    is UIState.Loading -> progressBar.visibility = View.VISIBLE
                    is UIState.Success -> {
                        progressBar.visibility = View.GONE
                        updateAdapters(state.data)
                    }
                    is UIState.Failure -> {
                        progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), state.errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateAdapters(postData: List<PostData>) {
        // 단일 게시물과 댓글 데이터만 처리
        val currentPost = postData.firstOrNull() ?: detailedPostData

        postAdapter.submitList(listOf(currentPost))
        commentAdapter.submitList(currentPost.comments)
    }
}
