package com.piooda.recycrew.ui.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.piooda.recycrew.R
import com.piooda.recycrew.base.BaseFragment
import com.piooda.recycrew.databinding.FragmentHomeBinding
import com.piooda.recycrew.databinding.FragmentMeetUpBinding

class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {
    companion object {
        fun newInstance() = HomeFragment()
    }

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.eventBtn.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_eventFragment)
        }

        binding.communityBtn.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_communityFragment)
        }

        binding.myPageBtn.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_myPageFragment)
        }
        return binding.root
    }

}