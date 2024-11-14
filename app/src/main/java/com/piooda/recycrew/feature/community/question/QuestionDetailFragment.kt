package com.piooda.recycrew.feature.community.question

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ConcatAdapter
import com.piooda.domain.model.PostData
import com.piooda.recycrew.core_ui.base.BaseFragment
import com.piooda.recycrew.core_ui.base.ViewModelFactory
import com.piooda.recycrew.databinding.FragmentQuestionDetailBinding
import com.piooda.recycrew.feature.community.question.adapter.QuestionCommentRecyclerAdapter
import com.piooda.recycrew.feature.community.question.adapter.QuestionDetailRecyclerAdapter

class QuestionDetailFragment :
    BaseFragment<FragmentQuestionDetailBinding>(FragmentQuestionDetailBinding::inflate) {

    private val viewModel by viewModels<QuestionDetailsViewModel> {
        ViewModelFactory
    }

    private lateinit var postAdapter: QuestionDetailRecyclerAdapter
    private lateinit var commentAdapter: QuestionCommentRecyclerAdapter
    private val args: QuestionFragmentArgs by navArgs()
    private val detailedPostData: PostData by lazy {
        args.detailedQuestData
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.commentFiled
        viewModel.loadData(detailedPostData)
        viewModel.getPostById(detailedPostData) // 수정된 부분

        Log.d("QuestionDetailFragment", "detailedImageData: $detailedPostData")

        binding.backButton.setOnClickListener {requireActivity().onBackPressed()}

        lifecycleScope.launchWhenStarted {
            viewModel.q_postData.collect { post ->
                post?.let {
                    // 가져온 포스트와 댓글 데이터를 UI에 반영
                    binding.rvQuestionDetail.apply {
                        postAdapter.submitList(listOf(detailedPostData))
                        commentAdapter.submitList(detailedPostData.comments)
                        adapter = ConcatAdapter(postAdapter, commentAdapter)
                    }
                }
            }
        }
        concatAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentQuestionDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun concatAdapter() {
        binding.rvQuestionDetail.apply {
            postAdapter = QuestionDetailRecyclerAdapter().apply {
                submitList(listOf(detailedPostData))
            }
            commentAdapter = QuestionCommentRecyclerAdapter()
            postAdapter = QuestionDetailRecyclerAdapter().apply {
                submitList(listOf(detailedPostData)) // 상세 게시글 데이터 설정
            }
            commentAdapter = QuestionCommentRecyclerAdapter().apply {
                submitList(detailedPostData.comments)
            }
            adapter = ConcatAdapter(postAdapter, commentAdapter)
            Log.d("concatAdapter", "Detailed Post: $detailedPostData")
            Log.d("concatAdapter", "Comments: ${detailedPostData.comments}")
        }
    }
}
