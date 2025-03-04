package com.piooda.recycrew.feature.community.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import com.piooda.recycrew.R
import com.piooda.recycrew.core.BaseFragment
import com.piooda.recycrew.databinding.FragmentCommunityBinding

class CommunityFragment :
    BaseFragment<FragmentCommunityBinding>(FragmentCommunityBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbarMenu()
        setupTabs()
        loadFragment(QuestionFragment())
    }

    private fun setupToolbarMenu() {
        binding.myToolbar.toolbar.apply {
            if (menu.size() == 0) { // 메뉴가 중복 생성되지 않도록 체크
                inflateMenu(R.menu.menu_toolbar)
            }

            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_search -> {
                        findNavController().navigate(R.id.action_communityFragment_to_searchFragment)
                        true
                    }

                    else -> false
                }
            }
        }
    }

    private fun setupTabs() {
        binding.tabs?.apply {
            addTab(newTab().setText("게시글"))
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    when (tab?.text) {
                        "게시글" -> loadFragment(QuestionFragment())
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })
        }
    }

    private fun loadFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.tabContent, fragment)
            .commit()
    }

}