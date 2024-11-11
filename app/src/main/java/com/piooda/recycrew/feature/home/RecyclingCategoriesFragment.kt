package com.piooda.recycrew.feature.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.piooda.recycrew.core_ui.base.BaseFragment
import com.piooda.recycrew.databinding.FragmentRecyclingCategoriesBinding

class RecyclingCategoriesFragment :
    BaseFragment<FragmentRecyclingCategoriesBinding>(FragmentRecyclingCategoriesBinding::inflate) {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentRecyclingCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }
}