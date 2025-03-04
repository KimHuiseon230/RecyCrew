package com.piooda.recycrew.feature.mypage.detail.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.piooda.data.datasource.remote.PreferenceDataStoreManager
import com.piooda.recycrew.R
import com.piooda.recycrew.core.BaseFragment
import com.piooda.recycrew.core.ViewModelFactory
import com.piooda.data.datasource.remote.PreferenceDataStoreConstants
import com.piooda.UiState
import com.piooda.recycrew.core.util.logDebug
import com.piooda.recycrew.core.util.logError
import com.piooda.recycrew.core.util.showToastShort
import com.piooda.recycrew.databinding.FragmentSettingnotificationBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationFragment :
    BaseFragment<FragmentSettingnotificationBinding>(FragmentSettingnotificationBinding::inflate) {
    private lateinit var dataStoreManager: PreferenceDataStoreManager

    private val viewModel by viewModels<NotificationViewModel> {
        ViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingnotificationBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 알림 채널 생성
        createNotificationChannel()

        // DataStoreManager 초기화
        dataStoreManager = PreferenceDataStoreManager(requireContext())

        // DataStore에서 알림 설정 값을 읽어와 스위치 초기 상태 설정
        initSwitch()
        collectNotificationState()

        // 스위치 상태 변경 처리
        observeSwitch()
        collectSaveNotificationState()

        // 알림 버튼 클릭 처리
        setupNotificationBtn()

        // 필요시 퍼미션 요청
        requestNotificationPermissionIfNeeded()
    }

    // DataStore에서 알림 설정 값을 읽어와 스위치 초기 상태 설정
    // repository
    private fun initSwitch() {
        viewModel.getNotificationPreference()
    }

    private fun collectNotificationState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.notificationState.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        logDebug("NotificationSetting", R.string.loading_notification_setting)
                    }

                    is UiState.Success -> {
                        Log.d("NotificationSetting",
                            "알림 설정 로드 성공:, ${state.resultData}")
                        binding.pushNotice.isChecked = state.resultData
                    }

                    is UiState.Error -> {
                        logError("NotificationSetting", R.string.failure_load_notification_setting, state.exception)
                    }

                    is UiState.Empty -> {
                        logDebug("NotificationSetting", R.string.no_notification_setting)
                    }
                }
            }
        }
    }

    private fun setupNotificationBtn() {
        binding.sendNotification.setOnClickListener {
            sendNotification()
        }
    }

    private fun observeSwitch() {
        binding.pushNotice.setOnCheckedChangeListener { _, isChecked ->
            viewModel.saveNotificationPreference(isChecked)
        }
    }

    private fun collectSaveNotificationState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.saveNotificationState.collectLatest { state ->
                when (state) {
                    is UiState.Loading -> {
                        logDebug("NotificationPreference", R.string.saving_notification_setting)
                    }

                    is UiState.Success -> {
                        logDebug("NotificationPreference", R.string.success_notification_setting)
                    }

                    is UiState.Error -> {
                        logError("NotificationPreference", R.string.failure_notification_setting, state.exception)
                    }

                    is UiState.Empty -> {
                        logDebug("NotificationPreference", R.string.empty_notification_setting)
                    }
                }
            }
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            showToastShort(R.string.denied_notification_permission)
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun checkSystemNotification(): Boolean {
        val notificationManager = NotificationManagerCompat.from(requireContext())
        if (!notificationManager.areNotificationsEnabled()) {
            showToastShort(R.string.unactive_notification)
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                showToastShort(R.string.request_notification)
                return false
            }
        }
        return true
    }

    private fun sendNotification() {
        if (!checkSystemNotification()) return

        CoroutineScope(Dispatchers.IO).launch {
            val isPushEnabled = dataStoreManager.readPreference(
                PreferenceDataStoreConstants.PUSH_NOTICE,
                false
            ).first()

            withContext(Dispatchers.Main) {
                if (isPushEnabled) {
                    try {
                        showNotification()
                    } catch (e: SecurityException) {
                        showToastShort(R.string.denied_notification_permission)
                    }
                } else {
                    showToastShort(R.string.unactive_app_notification)
                }
            }
        }
    }

    private fun showNotification() {
        try {
            val notification = NotificationCompat.Builder(requireContext(),
                getString(R.string.channel_id))
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_description))
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

            NotificationManagerCompat.from(requireContext()).notify(1, notification)
        } catch (e: SecurityException) {
            throw e
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = getString(R.string.channel_id)
            val channelName = getString(R.string.notification_channel)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = getString(R.string.notification_description)
            }

            val notificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}


// 성공 상태 로그 및 알림 스위치 업데이트
