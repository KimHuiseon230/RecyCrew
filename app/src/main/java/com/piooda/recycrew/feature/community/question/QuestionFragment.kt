package com.piooda.recycrew.feature.community.question

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentQuestionBinding.inflate(inflater, container, false)

        recyclerAdapter = QuestionRecyclerAdapter { item -> onItemClicked(item) }
        // RecyclerView 초기화
        binding.recyclerView.adapter = recyclerAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // ProgressBar를 위한 변수
        val progressBar = binding.progressBar
        progressBar.visibility = View.GONE

        binding.floatingButton.setOnClickListener {
            // InputActivity로 이동
            val intent = Intent(requireActivity(), InputActivity::class.java)
            startActivity(intent)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // postData의 상태를 처리
                viewModel.state.collect { uiState ->
                    when (uiState) {
                        is UiState.Loading -> {
                            // 로딩 상태일 때 ProgressBar를 보이게
                            progressBar.visibility = View.VISIBLE
                        }

                        is UiState.Success -> {
                            // 데이터가 정상적으로 로드된 경우
                            progressBar.visibility = View.GONE
                            recyclerAdapter.submitList(uiState.resultData)
                        }

                        is UiState.Error -> {
                            // 에러 발생 시 ProgressBar를 숨기고 에러 메시지 처리
                            progressBar.visibility = View.GONE
                            Log.e("PostData", "Error fetching data: ${uiState.exception.message}")
                            // 예: 에러 메시지를 Toast로 표시할 수도 있음
                            Toast.makeText(
                                requireContext(),
                                "Error: ${uiState.exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        UiState.Empty -> {
                            // 데이터가 없을 경우 처리
                            progressBar.visibility = View.GONE
                            // 예: 빈 리스트를 보여주거나 사용자에게 메시지 표시
                            recyclerAdapter.submitList(emptyList())
                            Toast.makeText(
                                requireContext(),
                                "No data available",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                // 예외 처리
                Log.e("PostData", "Unexpected error: ${e.message}")
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Unexpected error occurred", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        viewModel.loadData() // 데이터를 로드하는 함수 호출
        return binding.root
    }


    private fun onItemClicked(item: PostData) {
        val action =
            QuestionFragmentDirections.actionQuestionFragmentToQuestionDetailFragment(item)
        findNavController().navigate(action)
    }
}