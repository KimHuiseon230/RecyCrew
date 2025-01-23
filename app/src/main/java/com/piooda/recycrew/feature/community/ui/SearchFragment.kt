package com.piooda.recycrew.feature.community.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.piooda.data.model.Content
import com.piooda.recycrew.R
import com.piooda.recycrew.core.BaseFragment
import com.piooda.recycrew.core.ViewModelFactory
import com.piooda.recycrew.databinding.FragmentSearchBinding
import com.piooda.recycrew.feature.community.adapter.QuestionRecyclerAdapter
import com.piooda.recycrew.feature.community.viewmodel.SearchViewModel
import kotlinx.coroutines.launch

class SearchFragment : BaseFragment<FragmentSearchBinding>(FragmentSearchBinding::inflate) {

    private val viewModel: SearchViewModel by viewModels { ViewModelFactory(requireContext()) }
    private val questionAdapter: QuestionRecyclerAdapter by lazy {
        QuestionRecyclerAdapter(
            onClick = ::navigateToDetailFragment,
            onLikeClick = {}
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeComponents()
        observeViewModelStates()
    }

    private fun initializeComponents() {
        setupSearchView()
        setupRecyclerView()
    }

    private fun setupSearchView() {
        binding.searchView.apply {
            setIconifiedByDefault(false)
            queryHint = "검색어를 입력하세요"

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let {
                        performSearch(it)
                        clearFocus()
                    }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText?.let {
                        if (it.isEmpty()) { clearSearchResults() }
                        else { performSearch(it) }
                    }
                    return true
                }
            })

            setOnCloseListener { clearFocus()
                true
            }

            setOnQueryTextFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    showKeyboard(view)
                    clearSearchResults()
                }
            }

            post { requestFocus()
                showKeyboard(this)
            }
        }
    }

    private fun showKeyboard(view: View) {
        view.post {
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showSoftInput(view.findFocus(), InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = questionAdapter
        }
    }

    private fun observeViewModelStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observeSearchHistory() }
                launch { observeSearchResults() }
            }
        }
    }

    private suspend fun observeSearchHistory() {
        viewModel.searchHistory.collect { history ->
            updateSearchHistoryChips(history)
        }
    }

    private fun updateSearchHistoryChips(history: List<String>) {
        binding.apply {
            searchHistoryChipGroup.removeAllViews()
            tvNoSearchHistory.isVisible = history.isEmpty()
            searchHistoryChipGroup.isVisible = history.isNotEmpty()

            history.forEach { query ->
                createSearchHistoryChip(query)?.let { chip ->
                    searchHistoryChipGroup.addView(chip)
                }
            }
        }
    }

    private fun createSearchHistoryChip(query: String) = Chip(requireContext()).apply {
        text = query
        isCloseIconVisible = true
        setOnClickListener { handleSearchHistoryChipClick(query) }
        setOnCloseIconClickListener { viewModel.deleteSearchQuery(query) }
    }

    private fun handleSearchHistoryChipClick(query: String) {
        binding.searchView.setQuery(query, true)
    }

    private suspend fun observeSearchResults() {
        viewModel.searchResults.collect { results ->
            updateSearchResultsVisibility(results)
        }
    }

    private fun updateSearchResultsVisibility(results: List<Content>) {
        binding.apply {
            tvNoSearchResults.isVisible = results.isEmpty()
            recyclerView.isVisible = results.isNotEmpty()
        }
        questionAdapter.submitList(results)
    }

    private fun performSearch(query: String) {
        viewModel.searchContent(query)
        viewModel.addSearchHistory(query)
    }

    private fun clearSearchResults() {
        viewModel.clearSearchResults()
        questionAdapter.submitList(emptyList())
        binding.tvNoSearchResults.isVisible = true
        binding.recyclerView.isVisible = false
    }

    private fun navigateToDetailFragment(content: Content) {
        val bundle = bundleOf("CONTENT_ID" to content.id)
        findNavController().navigate(R.id.action_questionFragment_to_searchFragment, bundle)
    }
}
