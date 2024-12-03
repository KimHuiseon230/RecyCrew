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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadData() // 뷰 생성 후 데이터 로드
        observeViewModel()   // 생명주기 안전성을 고려한 데이터 관찰
    }

    private fun setupRecyclerView() {
        recyclerAdapter = QuestionRecyclerAdapter(
            onClick = { item -> navigateToDetailFragment(item) },
            onLikeClick = { item -> viewModel.postdateLikeCount(item.postId, item.isLiked) }
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
                launch { collectPostState() }
                launch { collectLikeState() }
            }
        }
    }

    private suspend fun collectPostState() {
        viewModel.state.collect { uiState ->
            handlePostState(uiState)
        }
    }

    private suspend fun collectLikeState() {
        viewModel.likeState.collect { likeState ->
            handleLikeState(likeState)
        }
    }

    private fun handlePostState(uiState: UiState<List<PostData>>) {
        binding.progressBar.visibility = if (uiState is UiState.Loading) View.VISIBLE else View.GONE

        when (uiState) {
            is UiState.Success -> {
                uiState.resultData?.let { posts -> recyclerAdapter.submitList(posts) }
            }
            is UiState.Error -> {
                Log.e("PostData", "Error fetching data: ${uiState.exception.message}")
                showToast("Error: ${uiState.exception.message}")
            }
            UiState.Empty -> {
                recyclerAdapter.submitList(emptyList())
                showToast("No data available")
            }
            is UiState.Loading -> {} // 이미 progressBar 처리됨
        }
    }

    private fun handleLikeState(likeState: UiState<Boolean>) {
        when (likeState) {
            is UiState.Success -> {
                // 좋아요 성공 시 데이터 새로고침
                viewModel.loadData()
            }
            is UiState.Error -> {
                showToast("좋아요 업데이트 실패: ${likeState.exception.message}")
            }
            else -> {} // Loading 등 다른 상태 무시
        }
    }

    private fun navigateToDetailFragment(item: PostData) {
        val action = QuestionFragmentDirections.actionQuestionFragmentToQuestionDetailFragment(item)
        findNavController().navigate(action)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
