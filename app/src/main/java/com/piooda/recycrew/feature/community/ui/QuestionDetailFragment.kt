package com.piooda.recycrew.feature.community.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.piooda.UiState
import com.piooda.data.model.Content
import com.piooda.recycrew.core.BaseFragment
import com.piooda.recycrew.core.ViewModelFactory
import com.piooda.recycrew.databinding.FragmentQuestionDetailBinding
import com.piooda.recycrew.feature.community.adapter.QuestionCommentRecyclerAdapter
import com.piooda.recycrew.feature.community.viewmodel.QuestionDetailsViewModel
import kotlinx.coroutines.launch

class QuestionDetailFragment :
    BaseFragment<FragmentQuestionDetailBinding>(FragmentQuestionDetailBinding::inflate) {

    private val viewModel by viewModels<QuestionDetailsViewModel> { ViewModelFactory(requireContext()) }
    private lateinit var commentAdapter: QuestionCommentRecyclerAdapter
    private val args: QuestionDetailFragmentArgs by navArgs()
    private val content: Content by lazy { args.detailedQuestPostData }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        content.id?.let { viewModel.loadComments(it) }
        content.id?.let { viewModel.loadContentDetail(it) }

        // 댓글 RecyclerView 설정
        commentAdapter = QuestionCommentRecyclerAdapter()
        binding.rvQuestionDetail.adapter = commentAdapter

        // 🔥 게시물 상세 부분만 Compose 적용
        // 🔥 ComposeView를 활용하여 게시글 상세 화면만 Compose로 전환
        binding.composeContainer.setContent {
            QuestionDetailContent(
                content = content,
                onBackClick = { findNavController().popBackStack() }, // 🔥 뒤로 가기 추가
                onEditClick = { /* 수정 기능 추가 */ },
                onDeleteClick = { content.id?.let { viewModel.deletePost(it) } }
            )
        }

        binding.commentFiled.setEndIconOnClickListener {
            val commentText = binding.commentFiled.editText?.text.toString().trim()
            if (commentText.isNotEmpty()) {
                val currentUser = FirebaseAuth.getInstance().currentUser
                val userName = currentUser?.displayName ?: "Anonymous"
                val newComment = Content.Comment(
                    author = userName, // 유저 이름
                    content = commentText, // 댓글 내용
                    timestamp = Timestamp.now() // 댓글 시간
                )
                content.id?.let { it1 -> viewModel.addCommentToPost(it1, newComment) }
                binding.commentFiled.editText?.text?.clear()
            } else {
                Log.d("QuestionDetailFragment - addCommentToPost ", "댓글이 비워져 있습니다.")
            }
        }

        //  댓글 데이터 관찰 (StateFlow → collect 사용)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.commentList.collect { comments ->
                commentAdapter.submitList(comments)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                if (state is UiState.Success) {
                    findNavController().popBackStack()
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun QuestionDetailContent(
        content: Content,
        onBackClick: () -> Unit,
        onEditClick: () -> Unit,
        onDeleteClick: () -> Unit,
    ) {
        var expanded by remember { mutableStateOf(false) } // 🍔 메뉴 상태

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("게시글 상세") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) { // 🔙 뒤로 가기 버튼
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "뒤로 가기"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { expanded = true }) { // 🍔 점 3개 버튼
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "더보기")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(text = { Text("수정하기") }, onClick = {
                                expanded = false
                                onEditClick()
                            })
                            DropdownMenuItem(text = { Text("삭제하기") }, onClick = {
                                expanded = false
                                onDeleteClick()
                            })
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // 🔥 닉네임
                Text(
                    text = content.nickname ?: "익명",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )

                // 🔥 이미지 표시 (있을 경우만)
                content.imagePath?.let { imageUrl ->
                    if (imageUrl.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(model = imageUrl),
                            contentDescription = "게시글 이미지",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .padding(8.dp)
                        )
                    }
                }

                // 🔥 게시글 내용
                Text(
                    text = content.content ?: "내용 없음",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )

                Divider()
            }
        }
    }

}
