package com.piooda.recycrew.ui.main.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.piooda.recycrew.R
import com.piooda.recycrew.base.BaseFragment
import com.piooda.recycrew.databinding.FragmentEventBinding

class EventFragment : BaseFragment<FragmentEventBinding>(FragmentEventBinding::inflate) {
    private lateinit var callback: OnBackPressedCallback

    companion object {
        fun newInstance() = EventFragment()
    }

    private val viewModel: EventViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentEventBinding.inflate(inflater, container, false)

        binding.attendanceCheckBtn.setOnClickListener {
            findNavController().navigate(R.id.action_eventFragment_to_attendanceCheckFragment)
        }
        return binding.root
    }
}