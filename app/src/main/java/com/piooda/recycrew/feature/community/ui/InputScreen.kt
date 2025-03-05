package com.piooda.recycrew.feature.community.ui

import android.app.Activity
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.piooda.data.model.Content
import com.piooda.recycrew.feature.community.viewmodel.QuestionViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputScreen(viewModel: QuestionViewModel) {
    val context = LocalContext.current
    val activity = context as? Activity
    val coroutineScope = rememberCoroutineScope()

    var category by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var contentText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val storage = FirebaseStorage.getInstance()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> selectedImageUri = uri }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("게시글 작성") })
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("카테고리 입력") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("제목 입력") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = contentText,
                    onValueChange = { contentText = it },
                    label = { Text("내용 입력") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    maxLines = 5
                )
                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("이미지 선택")
                }

                selectedImageUri?.let { uri ->
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = "선택한 이미지",
                        modifier = Modifier
                            .size(200.dp)
                            .padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (title.isEmpty() || contentText.isEmpty()) {
                            Toast.makeText(context, "제목과 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        coroutineScope.launch {
                            val user = FirebaseAuth.getInstance().currentUser
                            val userName = user?.displayName ?: "Unknown User"
                            val imagePath = selectedImageUri?.let { uri ->
                                uploadImageToFirebase(storage, uri)
                            } ?: ""

                            val newContent = Content(
                                id = UUID.randomUUID().toString(),
                                nickname = userName,
                                title = title,
                                content = contentText,
                                createdDate = Date(),
                                favoriteCount = 0,
                                commentCount = 0,
                                viewCount = 0,
                                searchIndex = generateSearchIndex("$title $contentText"),
                                imagePath = imagePath
                            )

                            viewModel.insert(newContent)
                            Toast.makeText(context, "게시글이 등록되었습니다.", Toast.LENGTH_SHORT).show()

                            // 현재 Activity 종료
                            activity?.finish()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("게시글 등록")
                }
            }
        }
    )
}


private fun generateSearchIndex(text: String): List<String> {
    val indexList = mutableSetOf<String>()
    val words = text.lowercase().split(" ")

    words.forEach { word ->
        for (i in 1..word.length) {
            indexList.add(word.substring(0, i))
        }
    }
    return indexList.toList()
}

private suspend fun uploadImageToFirebase(storage: FirebaseStorage, uri: Uri): String {
    val ref = storage.reference.child("images/${UUID.randomUUID()}.png")
    val uploadTask = ref.putFile(uri)
    return uploadTask.continueWithTask { task ->
        if (!task.isSuccessful) throw task.exception!!
        ref.downloadUrl
    }.await().toString()
}
