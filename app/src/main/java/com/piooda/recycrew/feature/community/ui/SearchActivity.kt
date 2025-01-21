package com.piooda.recycrew.feature.community.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.piooda.recycrew.databinding.ActivitySearchBinding
import com.piooda.recycrew.feature.community.adapter.QuestionRecyclerAdapter
import com.piooda.recycrew.feature.community.viewmodel.SearchViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private val searchViewModel: SearchViewModel by viewModels()
    private lateinit var adapter: QuestionRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearchView()
        observeSearchResults()
    }

    private fun setupRecyclerView() {
        adapter = QuestionRecyclerAdapter(
            onClick = { content ->
                Toast.makeText(this, "게시글: ${content.title}", Toast.LENGTH_SHORT).show()
            },
            onLikeClick = { content -> }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchViewModel.search(newText.orEmpty())
                return true
            }
        })
    }

    private fun observeSearchResults() {
        lifecycleScope.launch {
            searchViewModel.filteredList.collectLatest { results ->
                adapter.submitList(results)
            }
        }
    }
}
