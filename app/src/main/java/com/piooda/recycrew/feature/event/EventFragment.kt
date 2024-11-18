package com.piooda.recycrew.feature.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.piooda.recycrew.R
import com.piooda.recycrew.core_ui.base.BaseFragment
import com.piooda.recycrew.databinding.FragmentEventBinding

class EventFragment :
    BaseFragment<FragmentEventBinding>(FragmentEventBinding::inflate) {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentEventBinding.inflate(inflater, container, false)
        binding.attendanceCheckSection.setOnClickListener {
            findNavController().navigate(R.id.action_eventFragment_to_attendanceCheckFragment)
        }
        return binding.root
    }


}