package com.piooda.recycrew.feature.community.question

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.piooda.data.model.Content
import com.piooda.recycrew.common.UiState
import com.piooda.recycrew.core_ui.base.BaseFragment
import com.piooda.recycrew.core_ui.base.InputActivity
import com.piooda.recycrew.core_ui.base.ViewModelFactory
import com.piooda.recycrew.databinding.FragmentQuestionBinding
import com.piooda.recycrew.feature.community.question.adapter.QuestionRecyclerAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class QuestionFragment : BaseFragment<FragmentQuestionBinding>(FragmentQuestionBinding::inflate) {

    private lateinit var recyclerAdapter: QuestionRecyclerAdapter
    private val viewModel by viewModels<QuestionViewModel> {
        ViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentQuestionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        setupFloatingButton()
    }

    // ✅ RecyclerView 설정 (좋아요 클릭 이벤트 추가)
    private fun setupRecyclerView() {
        recyclerAdapter = QuestionRecyclerAdapter(
            onClick = { item -> navigateToDetailFragment(item) },
            onLikeClick = { item ->
                viewModel.toggleLike(item)
            }
        )
        binding.recyclerView.apply {
            adapter = recyclerAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    // ✅ FloatingButton 클릭 시 데이터 갱신
    private fun setupFloatingButton() {
        binding.floatingButton.setOnClickListener {
            val intent = Intent(requireActivity(), InputActivity::class.java)
            startActivity(intent)
            viewModel.refreshPosts()
        }
    }

    // ✅ ViewModel 관찰 (좋아요 클릭 및 데이터 갱신 반영)
    @SuppressLint("NotifyDataSetChanged")
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.contentList
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest { contentList ->
                    recyclerAdapter.submitList(contentList.toMutableList()) {
                        binding.progressBar.isVisible = false
                        binding.recyclerView.isVisible = contentList.isNotEmpty()
                        Log.d("RecyclerView", "데이터 수집 완료: ${contentList.size}")
                    }
                }
        }
        lifecycleScope.launch {
            viewModel.state.collectLatest {
                when (it) {
                    is UiState.Success -> {
                        binding.progressBar.isVisible = false
                        recyclerAdapter.submitList(it.resultData.toMutableList())
                    }

                    is UiState.Error -> {
                        binding.progressBar.isVisible = false
                        Toast.makeText(
                            requireContext(),
                            "데이터 로드 실패: ${it.exception}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    UiState.Loading -> {
                        binding.progressBar.isVisible = true
                    }

                    else -> {

                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPosts()
    }

    private fun navigateToDetailFragment(item: Content) {
        val action = QuestionFragmentDirections.actionQuestionFragmentToQuestionDetailFragment(item)
        findNavController().navigate(action)
    }
}