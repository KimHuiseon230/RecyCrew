package com.piooda.recycrew.feature.mypage.detail.editprofile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.piooda.data.model.UserProfile
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.piooda.recycrew.R
import com.piooda.recycrew.common.UiState
import com.piooda.recycrew.core_ui.base.AttendanceCheckApplication
import com.piooda.recycrew.core_ui.base.BaseFragment
import com.piooda.recycrew.core_ui.base.ViewModelFactory
import com.piooda.recycrew.core_ui.util.logDebug
import com.piooda.recycrew.core_ui.util.logError
import com.piooda.recycrew.core_ui.util.showToastShort
import com.piooda.recycrew.databinding.FragmentEditprofileBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class EditProfileFragment: BaseFragment<FragmentEditprofileBinding>(FragmentEditprofileBinding::inflate) {
    private val args: EditProfileFragmentArgs by navArgs() //SafeArgs로 데이터 받기
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private lateinit var googleSignInClient: GoogleSignInClient
    // Activity Result Launcher 선언
    private lateinit var photoPickerLauncher: ActivityResultLauncher<Intent>

    private val viewModel by viewModels<EditProfileViewModel> {
        ViewModelFactory(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializePhotoPickerLauncher()
        val app = requireActivity().application as AttendanceCheckApplication
        googleSignInClient = app.googleSignInClient
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditprofileBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userProfile = args.userProfile
        initializeUserProfile(userProfile)

        signOutButtonClickListener()
        setPhotoPickerClickListener()
        setupNicknameInputListener()
        saveButtonClickListener()

        collectCheckNickname()
        collectUpdateProfileState()
    }

    // Activity Result Launcher 초기화
    private fun initializePhotoPickerLauncher() {
        photoPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedImageUri: Uri? = result.data?.data
                if (selectedImageUri != null) {
                    viewModel.setSelectedImageUri(selectedImageUri)
                    binding.circularImageviewProfile.setImageUri(selectedImageUri)
                } else {
                    Toast.makeText(requireContext(),
                        getString(R.string.choose_image), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun launchPhotoPicker() {
        val photoPickerIntent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        photoPickerLauncher.launch(photoPickerIntent) // Activity Result API 사용
    }


    private fun initializeUserProfile(userProfile: UserProfile) {
        with(binding) {
            // 닉네임 설정
            tagEdittextNickname.setText(userProfile.nickname ?: "")

            // 프로필 사진 설정
            val profilePicUrl = userProfile.profilePicUrl
            if (!profilePicUrl.isNullOrEmpty()) {
                circularImageviewProfile.setImageUrl(profilePicUrl)
            } else {
                circularImageviewProfile.setImageUrl(R.drawable.img_user_default)
            }
        }
    }

    private fun collectCheckNickname() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.checkNicknameDuplicateState.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        showNicknameLoading()
                    }

                    is UiState.Success -> {
                        logDebug("CheckNickname", R.string.not_duplicated_nickname)
                        handleNicknameSuccess(state.resultData)
                    }

                    is UiState.Error -> {
                        logError("CheckNickname", R.string.failure_check_nickname, state.exception)
                        showNicknameError(state.exception.localizedMessage ?: getString(R.string.unknown_error))
                    }

                    is UiState.Empty -> {
                        logDebug("CheckNickname", R.string.empty_nickname)
                        resetNicknameState()
                    }
                }
            }
        }
    }


    private fun collectUpdateProfileState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.updateProfileState.collectLatest { state ->
                when (state) {
                    is UiState.Loading -> {
                        // 로딩 상태일 때 별도 처리 없음 (필요 시 추가)
                        logDebug("EditProfileFragment", R.string.updating_profile)
                    }
                    is UiState.Success -> {
                        logDebug("EditProfileFragment", R.string.success_update_profile)
                        findNavController().navigate(R.id.action_editProfileFragment_to_myPageFragment)
                    }
                    is UiState.Error -> {
                        logError("EditProfileFragment", R.string.failure_update_profile, state.exception)
                    }
                    is UiState.Empty -> {
                        logDebug("EditProfileFragment", R.string.initial_state)
                    }
                }
            }
        }
    }



    private fun saveButtonClickListener() {
        binding.greenButtonSave.setOnClickListener {
            val newNickname = binding.tagEdittextNickname.getText().trim()
            val email = FirebaseAuth.getInstance().currentUser?.email
            val selectedImageUri = viewModel.selectedImageUri.value
            val currentNickname = args.userProfile.nickname

            if (email != null) {
                // 닉네임과 프로필 이미지 중 하나라도 변경되었는지 확인
                val isNicknameChanged = currentNickname != newNickname && newNickname.isNotBlank()
                val isImageChanged = selectedImageUri != null

                if (isNicknameChanged || isImageChanged) {
                    viewModel.updateProfile(email, newNickname, selectedImageUri)
                } else {
                    showToastShort(R.string.nothing_changed)
                }
            } else {
                showToastShort(R.string.empty_login_info)
            }
        }
    }

    private fun signOutButtonClickListener() {
        binding.textviewSignOut.setOnClickListener {
            firebaseAuth.signOut()
            googleSignInClient.signOut().addOnCompleteListener {
                googleSignInClient.revokeAccess().addOnCompleteListener {
                    logDebug("EditProfileFragment", R.string.success_initialized_google_session)
                    findNavController().navigate(R.id.action_editProfileFragment_to_authActivity)
                }
            }
        }
    }


    private fun setPhotoPickerClickListener() {
        binding.circularImageviewEditProfile.setOnClickListener {
            launchPhotoPicker()
        }
    }

    // 닉네임 입력 리스너 설정 함수
    private fun setupNicknameInputListener() {
        binding.tagEdittextNickname.doAfterTextChanged { text ->
            val newNickname = text.toString().trim()
            if (newNickname.isNotBlank()) {
                viewModel.checkNicknameDuplicate(newNickname)
            }
        }
    }

    // 닉네임 로딩 상태 처리
    private fun showNicknameLoading() {
        binding.nicknameErrorTextView.visibility = View.VISIBLE
        binding.nicknameErrorTextView.text = getString(R.string.checking_nickname)
    }

    // 닉네임 성공 상태 처리
    private fun handleNicknameSuccess(isAvailable: Boolean) {
        if (isAvailable) {
            // 닉네임이 사용 가능할 때
            binding.greenButtonSave.isEnabled = true
            binding.nicknameErrorTextView.visibility = View.GONE
        } else {
            // 닉네임이 중복일 때
            binding.greenButtonSave.isEnabled = false
            binding.nicknameErrorTextView.visibility = View.VISIBLE
            binding.nicknameErrorTextView.text = getString(R.string.duplicated_nickname)
        }
    }

    // 닉네임 에러 상태 처리
    private fun showNicknameError(errorMessage: String) {
        binding.greenButtonSave.isEnabled = false
        binding.nicknameErrorTextView.visibility = View.VISIBLE
        binding.nicknameErrorTextView.text =
            getString(R.string.failure_check_nickname, errorMessage)
    }

    // 닉네임 초기 상태 처리
    private fun resetNicknameState() {
        binding.greenButtonSave.isEnabled = false
        binding.nicknameErrorTextView.visibility = View.GONE
    }
}
