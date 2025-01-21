package com.piooda.recycrew.feature.community.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.piooda.UiState
import com.piooda.data.model.Content
import com.piooda.recycrew.core.BaseFragment
import com.piooda.recycrew.core.ViewModelFactory
import com.piooda.recycrew.databinding.FragmentQuestionBinding
import com.piooda.recycrew.feature.community.adapter.QuestionRecyclerAdapter
import com.piooda.recycrew.feature.community.viewmodel.QuestionViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class QuestionFragment : BaseFragment<FragmentQuestionBinding>(FragmentQuestionBinding::inflate) {

    private lateinit var recyclerAdapter: QuestionRecyclerAdapter
    private val viewModel by viewModels<QuestionViewModel> {
        ViewModelFactory(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        setupFloatingButton()
    }

    private fun setupRecyclerView() {
        recyclerAdapter = QuestionRecyclerAdapter(
            onClick = { item -> navigateToDetailFragment(item) },
            onLikeClick = { item -> viewModel.toggleLike(item) }
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
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // 리스트 가져오는 Flow
                launch {
                    viewModel.contentList.collect { contentList ->
                        Log.d("Fragment", "🔥 데이터 수집 완료: ${contentList.size}개")

                        if (contentList.isEmpty()) {
                            viewModel.setUiState(UiState.Empty) // 🔥 데이터가 없으면 Empty 상태로 변경
                        } else {
                            recyclerAdapter.submitList(contentList.toMutableList()) {
                                Log.d("RecyclerView", "🔥 RecyclerView 업데이트 완료 | 데이터 개수: ${contentList.size}")
                                binding.progressBar.isVisible = false // 여기서 ProgressBar 숨김 처리
                                binding.recyclerView.isVisible = true
                            }
                        }
                    }
                }

                // UI 상태 처리 Flow
                launch {
                    viewModel.state.collectLatest { state ->
                        when (state) {
                            is UiState.Loading -> {
                                binding.progressBar.isVisible = true // 즉시 표시
                                binding.progressBar.animate().alpha(1f).setDuration(500).start()
                                Log.d("UI State", "Loading 상태 감지 🚀")
                            }

                            is UiState.Success -> {
                                Log.d("UI State", "데이터 로드 완료 ✅")
                                if (state.resultData.isEmpty()) {
                                    viewModel.setUiState(UiState.Empty) // 🔥 성공했지만 데이터가 없으면 Empty 상태로 변경
                                } else {
                                    recyclerAdapter.submitList(state.resultData.toMutableList()) {
                                        binding.progressBar.animate().alpha(0f).setDuration(300)
                                            .withEndAction { binding.progressBar.isVisible = false }
                                            .start()
                                    }
                                }
                            }

                            is UiState.Empty -> {
                                Log.d("UI State", "데이터 없음")
                                recyclerAdapter.submitList(emptyList()) { // 🔥 Empty 상태에서 RecyclerView 초기화
                                    binding.progressBar.animate().alpha(0f).setDuration(300)
                                        .withEndAction { binding.progressBar.isVisible = false }
                                        .start()
                                    binding.recyclerView.isVisible = false
                                }
                            }

                            is UiState.Error -> {
                                Log.e("UI State", "데이터 로드 실패 ❌", state.exception)
                                binding.progressBar.animate().alpha(0f).setDuration(300)
                                    .withEndAction { binding.progressBar.isVisible = false }
                                    .start()
                                Toast.makeText(
                                    requireContext(),
                                    "데이터 로드 실패: ${state.exception.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("onResume", "🔥 onResume 실행됨")
    }

    private fun navigateToDetailFragment(item: Content) {
        val action = QuestionFragmentDirections.actionQuestionFragmentToQuestionDetailFragment(item)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        binding.recyclerView.adapter = null
        _binding = null
        super.onDestroyView()
    }
}