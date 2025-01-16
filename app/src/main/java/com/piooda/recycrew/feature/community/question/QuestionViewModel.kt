package com.piooda.recycrew.feature.community.question

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.piooda.data.model.Content
import com.piooda.data.repository.ContentUseCase
import com.piooda.recycrew.common.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class QuestionViewModel @Inject constructor(
    private val contentUseCase: ContentUseCase
) : ViewModel() {

    // ✅ 상태 관리를 위한 MutableStateFlow
    private val _state = MutableStateFlow<UiState<List<Content>>>(UiState.Loading)
    val state: StateFlow<UiState<List<Content>>> = _state.asStateFlow()

    init {
        refreshPosts()
    }

    // ✅ Firestore 데이터 Flow를 StateFlow로 관리
    val contentList: StateFlow<List<Content>> = contentUseCase.loadList()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ✅ Firestore 데이터 갱신 메서드 (명시적 새로고침 지원)
    fun refreshPosts() {
        viewModelScope.launch {
            contentUseCase.loadList()
                .catch { e -> Log.e("refreshPosts", "데이터 로드 실패: ${e.message}") }
                .collect { posts ->
                    Log.d("refreshPosts", "데이터 로드 완료, 사이즈: ${posts.size}")
                    _state.value = if (posts.isNotEmpty()) UiState.Success(posts) else UiState.Empty
                }
        }
    }

    fun toggleLike(content: Content) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        viewModelScope.launch(Dispatchers.IO) {
            Log.d("toggleLike", "좋아요 클릭 이벤트 시작!")
            try {
                val postRef = content.id?.let {
                    FirebaseFirestore.getInstance().collection("content").document(it)
                }

                val updatedContent = FirebaseFirestore.getInstance().runTransaction { transaction ->
                    Log.d("toggleLike", "Firestore 트랜잭션 시작")
                    val snapshot = postRef?.let { transaction.get(it) }
                    val currentContent = snapshot?.toObject(Content::class.java) ?: return@runTransaction content

                    if (currentContent.favorites.containsKey(uid)) {
                        currentContent.favoriteCount -= 1
                        currentContent.favorites.remove(uid)
                        Log.d("toggleLike", "좋아요 제거됨! 좋아요 수: ${currentContent.favoriteCount}")
                    } else {
                        currentContent.favoriteCount += 1
                        currentContent.favorites[uid] = true
                        Log.d("toggleLike", "좋아요 추가됨! 좋아요 수: ${currentContent.favoriteCount}")
                    }

                    transaction.set(postRef, currentContent)
                    currentContent
                }.await()

                // ✅ 강제로 새로운 참조를 생성하기 위해 toMutableList() 사용
                _state.value = UiState.Success(_state.value.let {
                    if (it is UiState.Success) {
                        it.resultData.toMutableList().apply {
                            val index = indexOfFirst { it.id == updatedContent.id }
                            if (index != -1) this[index] = updatedContent
                            else add(updatedContent)
                        }
                    } else listOf(updatedContent)
                })

                Log.d("toggleLike", "Firestore 업데이트 완료, UI 강제 갱신!")

            } catch (e: Exception) {
                Log.e("toggleLike", "오류 발생: ${e.message}")
                _state.value = UiState.Error(e)
            }
        }
    }

    // ✅ 게시글 추가 (단일 Boolean 반환 방식)
    fun insert(content: Content) {
        viewModelScope.launch {
            val success = contentUseCase.save(content)
            if (success) {
                _state.value = UiState.Success(listOf(content))
                refreshPosts()
            } else {
                _state.value = UiState.Error(Exception("게시글 저장 실패"))
            }
        }
    }
}