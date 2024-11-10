package com.piooda.recycrew.feature.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.piooda.recycrew.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}