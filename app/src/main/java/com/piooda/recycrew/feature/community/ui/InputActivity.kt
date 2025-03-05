package com.piooda.recycrew.feature.community.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.piooda.recycrew.core.ViewModelFactory
import com.piooda.recycrew.feature.community.viewmodel.QuestionViewModel

class InputActivity : ComponentActivity() {
    private val viewModel: QuestionViewModel by viewModels {
        ViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InputScreen(viewModel)
        }
    }
}
