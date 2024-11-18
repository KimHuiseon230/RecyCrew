package com.piooda.recycrew.feature.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.piooda.recycrew.R
import com.piooda.recycrew.databinding.ActivitySignupBinding

class SignActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private val viewModel: SignViewModel by activityViewModels()
    private val googleSignInClient: GoogleSignInClient by lazy { getGoogleClient() }
    private val googleAuthLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

        try {
            val account = task.getResult(ApiException::class.java)
            account.idToken?.let { viewModel.requestGiverSignIn(it) } // 서버에 idToken 보내기

            // 클라단에서 바로 이름, 이메일 등이 필요하다면 아래와 같이 account를 통해 각 메소드를 불러올 수 있다.
            val userName = account.givenName
            val serverAuth = account.serverAuthCode

            moveSignUpActivity()

        } catch (e: ApiException) {
            Log.e(SignFragment::class.java.simpleName, e.stackTraceToString())
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)

        addListener()
        setContentView(binding.root)
    }

    private fun addListener() {
        binding.clGoogleLogin.setOnClickListener {
            requestGoogleLogin()
        }
    }

    private fun requestGoogleLogin() {
        googleSignInClient.signOut()
        val signInIntent = googleSignInClient.signInIntent
        googleAuthLauncher.launch(signInIntent)
    }

    private fun getGoogleClient(): GoogleSignInClient {
        val googleSignInOption = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope("https://www.googleapis.com/auth/pubsub"))
            .requestServerAuthCode(getString(R.string.google_login_client_id)) // 콘솔에서 가져온 client id 를 이용해 server authcode를 요청한다.
            .requestIdToken(getString(R.string.google_login_client_id)) // 콘솔에서 가져온 client id 를 이용해 id token을 요청한다.
            .requestEmail() // 이메일도 요청할 수 있다.
            .build()

        return GoogleSignIn.getClient(requireActivity(), googleSignInOption)
    }

    private fun moveSignUpActivity() {
        requireActivity().run {
            startActivity(Intent(requireContext(), SignUpActivity::class.java))
            finish()
        }
    }
}