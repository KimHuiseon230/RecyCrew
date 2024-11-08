package com.piooda.recycrew.feature.community

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.piooda.recycrew.R
import com.piooda.recycrew.core_ui.base.BaseFragment
import com.piooda.recycrew.databinding.FragmentCommunityBinding
import com.piooda.recycrew.feature.community.meetup.MeetUpFragment
import com.piooda.recycrew.feature.community.question.QuestionFragment
import com.piooda.recycrew.feature.community.trade.TradeFragment

class CommunityFragment :
    BaseFragment<FragmentCommunityBinding>(FragmentCommunityBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val main = (activity as AppCompatActivity).supportActionBar

        with(main) {
            binding.myToolbar.toolbar
            this?.setDisplayShowTitleEnabled(false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)

        // 탭 추가
        binding.tabs?.apply {
            addTab(this.newTab().setText("게시글"))
            addTab(this.newTab().setText("나눔"))
            addTab(this.newTab().setText("모임"))
        }

        // 기본으로 첫 번째 탭의 Fragment 로드
        loadFragment(QuestionFragment())

        // 탭 선택 시 프래그먼트 변경
        binding.tabs?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.text) {
                    "게시글" -> loadFragment(QuestionFragment())
                    "나눔" -> loadFragment(TradeFragment())
                    "모임" -> loadFragment(MeetUpFragment())
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_toolbar, menu) // Replace with your menu file name
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item1 -> {
                true
            }

            R.id.item2 -> {
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

}
