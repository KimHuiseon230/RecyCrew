package com.piooda.recycrew.ui.main.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayout
import com.piooda.recycrew.R
import com.piooda.recycrew.base.BaseFragment
import com.piooda.recycrew.databinding.FragmentEventBinding
import com.piooda.recycrew.ui.main.event.attendanceCheck.AttendanceCheckFragment

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
        // 탭 추가
        binding.tabs?.apply {
            addTab(this.newTab().setText("출석"))
        }

        // 기본으로 첫 번째 탭의 Fragment 로드
        loadFragment(AttendanceCheckFragment())

        // 탭 선택 시 프래그먼트 변경
        binding.tabs?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.text) {
                    "출석" -> loadFragment(AttendanceCheckFragment())
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        return binding.root
    }

    // 프래그먼트 로드 함수
    private fun loadFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.tabContent, fragment)
            .commit()
    }

}