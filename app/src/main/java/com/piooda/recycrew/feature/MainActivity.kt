package com.piooda.recycrew.feature

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.piooda.recycrew.R
import com.piooda.recycrew.databinding.ActivityMainBinding
import com.piooda.recycrew.feature.community.CommunityFragment
import com.piooda.recycrew.feature.event.EventFragment
import com.piooda.recycrew.feature.home.HomeFragment
import com.piooda.recycrew.feature.myPage.MyPageFragment

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // NavHostFragment와 NavController 초기화
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fcv_main) as NavHostFragment
        navController = navHostFragment.navController
        setBottomNavigationView()

        // 앱 초기 실행 시 홈화면으로 설정
        if (savedInstanceState == null) {
            binding.bottomNavigationView.selectedItemId = R.id.fragment_home
        }
    }

    fun setBottomNavigationView() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.fragment_home -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fcv_main, HomeFragment())
                        .commit()
                    true
                }

                R.id.fragment_community -> {
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.fcv_main,
                            CommunityFragment()
                        ).commit()
                    true
                }

                R.id.fragment_event -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fcv_main, EventFragment()).commit()
                    true
                }

                R.id.fragment_mypag -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fcv_main, MyPageFragment()).commit()
                    true
                }

                else -> false
            }
        }
    }

}