package com.piooda.recycrew.core

import android.app.Application
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseApp
import com.piooda.recycrew.R

class AttendanceCheckApplication : Application() {
    lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private lateinit var attendanceCheckApplication: AttendanceCheckApplication
        fun getAttendanceCheckApplication() = attendanceCheckApplication
    }

    override fun onCreate() {
        super.onCreate()
        attendanceCheckApplication = this
        // Firebase 초기화
        FirebaseApp.initializeApp(this)
        initializeGoogleSignInClient()
    }

    private fun initializeGoogleSignInClient() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }
}