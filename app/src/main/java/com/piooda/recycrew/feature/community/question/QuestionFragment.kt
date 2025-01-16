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
import kotlinx.coroutines.launch

class QuestionFragment : BaseFragment<FragmentQuestionBinding>(FragmentQuestionBinding::inflate) {

    private lateinit var recyclerAdapter: QuestionRecyclerAdapter
    private val viewModel by viewModels<QuestionViewModel> {
        ViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuestionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFloatingButton()
        observeViewModel() // ✅ 여기에서 반드시 호출

        if (viewModel.state.value !is UiState.Success) {
            viewModel.refreshPosts()
        }
    }

    // ✅ RecyclerView 설정 (좋아요 클릭 이벤트 추가)
    private fun setupRecyclerView() {
        recyclerAdapter = QuestionRecyclerAdapter(
            onClick = { item -> navigateToDetailFragment(item) },
            onLikeClick = { item ->
                viewModel.toggleLike(item) // ✅ 좋아요 클릭 이벤트 연결
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
            viewModel.refreshPosts()  // ✅ 새 데이터 로드
        }
    }

    // ✅ ViewModel 관찰 (좋아요 클릭 및 데이터 갱신 반영)
    @SuppressLint("NotifyDataSetChanged")
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.contentList
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED) // ✅ STARTED 상태에서 바로 수집
                .collect { contentList ->
                    recyclerAdapter.submitList(contentList.toMutableList()) {
                        binding.progressBar.isVisible = false
                        binding.recyclerView.isVisible = contentList.isNotEmpty()
                        Log.d("RecyclerView", "데이터 수집 완료: ${contentList.size}")
                    }
                }
        }
    }

    // ✅ 상세 페이지 이동
    private fun navigateToDetailFragment(item: Content) {
        val action = QuestionFragmentDirections.actionQuestionFragmentToQuestionDetailFragment(item)
        findNavController().navigate(action)
    }

    // ✅ Toast 메시지 처리
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
