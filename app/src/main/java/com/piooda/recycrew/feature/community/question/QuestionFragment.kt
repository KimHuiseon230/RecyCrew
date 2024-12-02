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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.piooda.data.model.PostData
import com.piooda.recycrew.common.UiState
import com.piooda.recycrew.core_ui.base.BaseFragment
import com.piooda.recycrew.core_ui.base.InputActivity
import com.piooda.recycrew.core_ui.base.ViewModelFactory
import com.piooda.recycrew.databinding.FragmentQuestionBinding
import com.piooda.recycrew.feature.community.question.adapter.QuestionRecyclerAdapter
import kotlinx.coroutines.launch

class QuestionFragment : BaseFragment<FragmentQuestionBinding>(FragmentQuestionBinding::inflate) {
    private lateinit var recyclerAdapter: QuestionRecyclerAdapter
    private val viewModel by viewModels<QuestionViewModel> {
        ViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuestionBinding.inflate(inflater, container, false)

        setupRecyclerView()
        setupFloatingButton()
        observeViewModel()

        return binding.root
    }

    private fun setupRecyclerView() {
        recyclerAdapter = QuestionRecyclerAdapter(
            onClick = { item -> navigateToDetailFragment(item) },
            onLikeClick = { item ->
                viewModel.postdateLikeCount(item.postId, item.isLiked)
            }
        )

        binding.recyclerView.apply {
            adapter = recyclerAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupFloatingButton() {
        binding.floatingButton.setOnClickListener {
            val intent = Intent(requireActivity(), InputActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observePostState() }
                launch { observeLikeState() }
            }
        }
    }

    private suspend fun observePostState() {
        viewModel.state.collect { uiState ->
            binding.progressBar.visibility = when (uiState) {
                is UiState.Loading -> View.VISIBLE
                else -> View.GONE
            }

            when (uiState) {
                is UiState.Success -> {
                    uiState.resultData?.let { posts ->
                        recyclerAdapter.submitList(posts)
                    }
                }
                is UiState.Error -> {
                    Log.e("PostData", "Error fetching data: ${uiState.exception.message}")
                    Toast.makeText(
                        requireContext(),
                        "Error: ${uiState.exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                UiState.Empty -> {
                    recyclerAdapter.submitList(emptyList())
                    Toast.makeText(
                        requireContext(),
                        "No data available",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is UiState.Loading -> {} // 이미 progressBar 처리됨
            }
        }
    }

    private suspend fun observeLikeState() {
        viewModel.likeState.collect { likeState ->
            when (likeState) {
                is UiState.Success -> {
                    // 좋아요 상태 업데이트 성공 시 필요한 추가 작업
                    viewModel.loadData() // 데이터 새로고침
                }
                is UiState.Error -> {
                    Toast.makeText(
                        requireContext(),
                        "좋아요 업데이트 실패: ${likeState.exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {} // Loading 등 다른 상태 무시
            }
        }
    }

    private fun navigateToDetailFragment(item: PostData) {
        val action = QuestionFragmentDirections.actionQuestionFragmentToQuestionDetailFragment(item)
        findNavController().navigate(action)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadData() // 뷰가 완전히 생성된 후 데이터 로드
    }
}