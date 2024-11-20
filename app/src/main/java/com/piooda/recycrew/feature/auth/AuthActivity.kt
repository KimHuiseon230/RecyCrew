package com.piooda.recycrew.feature.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.piooda.recycrew.R
import com.piooda.recycrew.core_ui.base.AttendanceCheckApplication
import com.piooda.recycrew.databinding.ActivityAuthBinding
import com.piooda.recycrew.feature.MainActivity

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle: ${account.idToken}")
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.e(TAG, "Google sign in failed", e)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // GoogleSignInClient 초기화
        val app = application as AttendanceCheckApplication
        googleSignInClient = app.googleSignInClient

        // Google 로그인 버튼 클릭 이벤트
        binding.googleLoginBtn.setOnClickListener { signIn() }

        // 이미 로그인된 사용자가 있는 경우 MainActivity로 이동
        if (firebaseAuth.currentUser != null) {
            navigateToMainActivity()
        }
    }

    // Google 로그인 시작
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    // Firebase 인증 처리
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    firebaseAuth.currentUser?.let { user ->
                        saveUserToFireStore(user)
                        navigateToMainActivity()
                    }
                } else {
                    Log.e(TAG, getString(R.string.failure_firebase_auth), task.exception)
                    retrySignIn()
                }
            }
    }

    // Firestore에 사용자 정보 저장
    private fun saveUserToFireStore(user: FirebaseUser) {
        val db = FirebaseFirestore.getInstance()
        val email = user.email ?: return
        val userDoc = db.collection("users").document(email)

        userDoc.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    Log.d(TAG, getString(R.string.already_existed_user))
                } else {
                    val userData = mapOf(
                        "name" to (user.displayName ?: "Unknown"),
                        "email" to email,
                        "profilePicUrl" to (user.photoUrl?.toString() ?: ""),
                        "nickname" to "",
                        "point" to 0
                    )
                    userDoc.set(userData)
                        .addOnSuccessListener { Log.d(TAG, getString(R.string.success_register_user)) }
                        .addOnFailureListener { e -> Log.w(TAG,
                            getString(R.string.failure_register_user), e) }
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, getString(R.string.failure_check_user), e)
            }
    }

    // 로그인 재시도
    private fun retrySignIn() {
        firebaseAuth.signOut()
        googleSignInClient.signOut().addOnCompleteListener {
            googleSignInClient.revokeAccess().addOnCompleteListener { signIn() }
        }
    }

    // MainActivity로 이동
    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    companion object {
        private const val TAG = "AuthActivity"
    }
}
