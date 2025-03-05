package com.piooda.recycrew.feature.community.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.piooda.recycrew.core.BaseFragment
import com.piooda.recycrew.core.ViewModelFactory
import com.piooda.recycrew.databinding.FragmentSearchBinding
import com.piooda.recycrew.feature.community.adapter.SearchRecyclerAdapter
import com.piooda.recycrew.feature.community.viewmodel.SearchViewModel
import kotlinx.coroutines.launch

class SearchFragment : BaseFragment<FragmentSearchBinding>(FragmentSearchBinding::inflate) {

    private val viewModel: SearchViewModel by viewModels { ViewModelFactory(requireContext()) }

    private val searchAdapter: SearchRecyclerAdapter by lazy {
        SearchRecyclerAdapter(
            emptyList(),
            emptyList(),
            { content ->  //  게시글 클릭 이벤트
                val action = SearchFragmentDirections.actionSearchFragmentToQuestionDetailFragment(content)
                findNavController().navigate(action)
            },
            { username ->  //  유저 클릭 이벤트
                Log.d("SearchFragment", "🔥 유저 클릭됨: $username")
//                val action = SearchFragmentDirections.actionSearchFragmentToUserProfileFragment()
//                findNavController().navigate(action)
            }
        )
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSearchView()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchAdapter
        }
    }

    private fun setupSearchView() {
        binding.searchView.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let {
                        viewModel.searchContent(it) //  엔터 시 게시글 + 댓글 검색 실행
                        viewModel.addSearchHistory(it)
                        clearFocus()
                    }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText?.let {
                        if (it.isNotEmpty()) {
                            viewModel.searchUsers(it) //  입력 중에는 유저 자동완성 검색
                        } else {
                            searchAdapter.updateList(
                                emptyList(),
                                emptyList()
                            ) // 🔹 입력이 없으면 자동완성 목록 비우기
                        }
                    }
                    return true
                }
            })

            setOnCloseListener {
                searchAdapter.updateList(emptyList(), emptyList()) // 🔹 검색창 닫힐 때 자동완성 목록 초기화
                false
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.searchResults.collect { results ->
                        Log.d("UI", " RecyclerView 업데이트: ${results.size}개")
                        searchAdapter.updateList(results, viewModel.userSuggestions.value)
                    }
                }
                launch {
                    viewModel.userSuggestions.collect { users ->
                        Log.d("UI", " 유저 자동완성 업데이트: ${users.size}개")
                        searchAdapter.updateList(viewModel.searchResults.value, users)
                    }
                }
            }
        }
    }

}
