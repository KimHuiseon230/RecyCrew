package com.piooda.recycrew.feature.home.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.piooda.data.model.DetailedImageData
import com.piooda.data.model.UserProfile
import com.piooda.recycrew.R
import com.piooda.UiState
import com.piooda.recycrew.core.BaseFragment
import com.piooda.recycrew.core.ViewModelFactory
import com.piooda.recycrew.core.util.logDebug
import com.piooda.recycrew.core.util.logError
import com.piooda.recycrew.databinding.FragmentHomeBinding
import com.piooda.recycrew.feature.home.adapter.CategoriesBasicImagesAdapter
import com.piooda.recycrew.feature.home.viewmodle.CategoriesBasicImagesViewModel
import com.piooda.recycrew.feature.mypage.viewmodel.MyPageViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {
    private lateinit var categoriesBasicImagesAdapter: CategoriesBasicImagesAdapter
    private val viewModel by viewModels<CategoriesBasicImagesViewModel> {
        ViewModelFactory(requireContext())
    }

    private val myPageViewModel: MyPageViewModel by activityViewModels {
        ViewModelFactory(requireContext())
    }

    private var userProfile: UserProfile? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // RecyclerView 초기화
        categoriesBasicImagesAdapter = CategoriesBasicImagesAdapter { item -> onItemClicked(item) }
        binding.recyclerView.adapter = categoriesBasicImagesAdapter

        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.imageData.collect { result ->
                categoriesBasicImagesAdapter.submitList(result)
            }
        }

        viewModel.loadImageData()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myPageViewModel.loadUserProfile()
        collectLoadUserProfile()
    }

    private fun onItemClicked(item: DetailedImageData) {
        val action = HomeFragmentDirections.actionHomeFragmentToCategoriesDetailedImagesFragment(item)
        findNavController().navigate(action)
    }


    private fun collectLoadUserProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            myPageViewModel.loadUserProfileState.collectLatest { state ->
                when (state) {
                    is UiState.Loading -> {
                        logDebug("UserProfile", R.string.loading_user_profile)
                    }

                    is UiState.Success -> {
                        userProfile = state.resultData
                        updateUIWithUserProfile(userProfile)
                    }

                    is UiState.Error -> {
                        logError("UserProfile", R.string.failure_loading_user_profile, state.exception)
                        updateUIWithUserProfile(null)
                    }

                    is UiState.Empty -> {
                        logDebug("UserProfile", R.string.empty_user_profile)
                        updateUIWithUserProfile(null)
                    }
                }
            }
        }
    }


    private fun updateUIWithUserProfile(userProfile: UserProfile?) {
        with(binding) {
            if (userProfile != null) {
                tvUsername.text =
                    (buildString {
                        append(userProfile.nickname)
                        append("님".ifEmpty { userProfile.name })
                    })

                tvPoints.text = buildString {
                    append(userProfile.point.toString())
                    append(getString(R.string.notice_point))
                }

            } else {
                tvUsername.text = getString(R.string.guest)
            }
        }
    }
}