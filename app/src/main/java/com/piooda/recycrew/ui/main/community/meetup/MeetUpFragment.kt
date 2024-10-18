package com.piooda.recycrew.ui.main.community.meetup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.piooda.recycrew.base.BaseFragment
import com.piooda.recycrew.databinding.FragmentMeetUpBinding

class MeetUpFragment : BaseFragment<FragmentMeetUpBinding>(FragmentMeetUpBinding::inflate) {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMeetUpBinding.inflate(inflater, container, false)
        return binding.root
    }
}