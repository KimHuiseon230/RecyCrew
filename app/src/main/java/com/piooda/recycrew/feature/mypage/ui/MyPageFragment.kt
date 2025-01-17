package com.piooda.recycrew.feature.mypage.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.piooda.data.model.UserProfile
import com.piooda.recycrew.R
import com.piooda.UiState
import com.piooda.recycrew.core.BaseFragment
import com.piooda.recycrew.core.ViewModelFactory
import com.piooda.recycrew.core.util.logDebug
import com.piooda.recycrew.core.util.logError
import com.piooda.recycrew.core.util.showToastShort
import com.piooda.recycrew.databinding.FragmentMypageBinding
import com.piooda.recycrew.feature.mypage.viewmodel.MyPageViewModel

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MyPageFragment : BaseFragment<FragmentMypageBinding>(FragmentMypageBinding::inflate) {

    private var userProfile: UserProfile? = null

    private val viewModel by viewModels<MyPageViewModel> {
        ViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypageBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupNavigationListeners(userProfile = null) // 기본 리스너 설정
        viewModel.loadUserProfile()
        collectLoadUserProfile()
    }

    private fun collectLoadUserProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loadUserProfileState.collectLatest { state ->
                    handleUserProfileState(state)
                }
            }
        }
    }

    private fun handleUserProfileState(state: UiState<UserProfile>) {
        when (state) {
            is UiState.Loading -> {
                logDebug("UserProfile", R.string.loading_user_profile)
            }

            is UiState.Success -> {
                userProfile = state.resultData
                updateUIWithUserProfile(userProfile)
                setupNavigationListeners(userProfile)
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

    private fun setupNavigationListeners(userProfile: UserProfile?) {
        val navigationMap = mapOf(
            binding.faq to R.id.action_myPageFragment_to_FAQFragment,
            binding.notice to R.id.action_myPageFragment_to_noticeFragment,
            binding.profileSettings to R.id.action_myPageFragment_to_editProfileFragment,
            binding.notificationSettings to R.id.action_myPageFragment_to_notificationFragment
        )

        navigationMap.forEach { (view, actionId) ->
            view.setOnClickListener {
                if (actionId == R.id.action_myPageFragment_to_editProfileFragment) {
                    if (userProfile == null) {
                        showToastShort(R.string.request_login)
                    } else {
                        val action = MyPageFragmentDirections.actionMyPageFragmentToEditProfileFragment(userProfile)
                        findNavController().navigate(action)
                    }
                } else {
                    findNavController().navigate(actionId)
                }
            }
        }
    }

    private fun updateUIWithUserProfile(userProfile: UserProfile?) {
        with(binding) {
            if (userProfile != null) {
                profileName.text = userProfile.nickname?.ifEmpty { userProfile.name } ?: userProfile.name
                profileEmail.text = userProfile.email

                if (!userProfile.profilePicUrl.isNullOrEmpty()) {
                    Glide.with(profileImage.context)
                        .load(userProfile.profilePicUrl)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(profileImage)
                } else {
                    profileImage.setImageResource(R.drawable.img_user_default)
                }

                point.text = buildString {
                    append(userProfile.point.toString())
                    append("P")
                }

            } else {
                profileName.text = getString(R.string.guest)
                profileEmail.text = getString(R.string.request_login)
                profileImage.setImageResource(R.drawable.img_user_default)
            }
        }
    }
}
