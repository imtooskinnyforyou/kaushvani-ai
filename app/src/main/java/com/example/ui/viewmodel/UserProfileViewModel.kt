package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.FirestoreService
import com.example.data.model.ArtisanProfile
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.AuthRepository
import com.example.data.repository.FirebaseAuthRepository
import com.example.data.repository.UserProfileFirestoreRepository
import com.example.data.repository.UserProfileFirestoreRepositoryImpl
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * UI State for User & Artisan Profile
 */
sealed interface UserProfileUiState {
    object Loading : UserProfileUiState
    data class Success(
        val profile: ArtisanProfile,
        val isVerified: Boolean = true,
        val isCloudSynced: Boolean = true
    ) : UserProfileUiState
    data class Error(val message: String) : UserProfileUiState
}

/**
 * UserProfileViewModel manages real-time artisan credentials, PM Vishwakarma certification,
 * regional language localization, and bi-directional synchronization with Cloud Firestore.
 */
class UserProfileViewModel(
    application: Application,
    repository: UserProfileFirestoreRepository? = null,
    authRepo: AuthRepository? = null,
    val firestoreService: FirestoreService? = null,
    val firebaseAuth: com.google.firebase.auth.FirebaseAuth? = null,
    val firestore: com.google.firebase.firestore.FirebaseFirestore? = null
) : AndroidViewModel(application) {

    private val userPrefs = UserPreferencesRepository(application.applicationContext)
    private val fsService = firestoreService ?: FirestoreService(application.applicationContext, firestore)
    private val authRepository: AuthRepository = authRepo ?: FirebaseAuthRepository(
        context = application.applicationContext,
        userPreferencesRepository = userPrefs,
        firestoreService = fsService,
        firebaseAuthInstance = firebaseAuth
    )

    val profileRepository: UserProfileFirestoreRepository = repository ?: UserProfileFirestoreRepositoryImpl(
        firestoreService = fsService,
        userPreferencesRepository = userPrefs,
        authRepository = authRepository
    )

    companion object {
        private const val TAG = "UserProfileViewModel"
    }

    // Real-time active profile flow
    val currentProfile: StateFlow<ArtisanProfile> = profileRepository.currentProfileState

    // Community artisan passports list
    val allArtisans: StateFlow<List<ArtisanProfile>> = profileRepository.observeAllArtisans()
        .catch { e ->
            Log.e(TAG, "Error in artisans community stream: ${e.message}")
            emit(listOf(currentProfile.value))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(currentProfile.value))

    // Profile UI State
    val uiState: StateFlow<UserProfileUiState> = currentProfile
        .map { profile ->
            UserProfileUiState.Success(
                profile = profile,
                isVerified = profile.artisanCardNo.isNotBlank() || profile.isVishwakarmaEnrolled,
                isCloudSynced = true
            ) as UserProfileUiState
        }
        .catch { e ->
            emit(UserProfileUiState.Error(e.message ?: "प्रोफ़ाइल लोड करने में त्रुटि"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfileUiState.Loading)

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun updateProfile(
        name: String,
        craftSpecialty: String,
        experienceYears: Int,
        villageOrCluster: String,
        state: String,
        phone: String,
        upiId: String,
        artisanCardNo: String,
        isVishwakarmaEnrolled: Boolean,
        preferredLanguage: String,
        localePreference: String,
        onComplete: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            val updated = currentProfile.value.copy(
                name = name.trim(),
                craftSpecialty = craftSpecialty.trim(),
                experienceYears = experienceYears,
                villageOrCluster = villageOrCluster.trim(),
                state = state.trim(),
                phone = phone.trim(),
                upiId = upiId.trim(),
                artisanCardNo = artisanCardNo.trim(),
                isVishwakarmaEnrolled = isVishwakarmaEnrolled,
                preferredLanguage = preferredLanguage,
                localePreference = localePreference
            )

            val currentUid = authRepository.getCurrentUserId()
            val result = profileRepository.updateUserProfile(updated, currentUid)

            _isSaving.value = false
            if (result.isSuccess) {
                _statusMessage.value = "प्रोफ़ाइल सफलतापूर्वक अपडेट हुई!"
                onComplete(true)
            } else {
                _statusMessage.value = "अपडेट में त्रुटि: ${result.exceptionOrNull()?.message}"
                onComplete(false)
            }
        }
    }

    fun updateArtisanCredentials(
        cardNo: String,
        isVishwakarmaEnrolled: Boolean,
        upiId: String,
        onComplete: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            val currentUid = authRepository.getCurrentUserId()
            val result = profileRepository.updateArtisanCredentials(cardNo, isVishwakarmaEnrolled, upiId, currentUid)
            _isSaving.value = false
            if (result.isSuccess) {
                _statusMessage.value = "शिल्पकार प्रमाण पत्र व UPI विवरण सुरक्षित!"
                onComplete(true)
            } else {
                _statusMessage.value = "त्रुटि: ${result.exceptionOrNull()?.message}"
                onComplete(false)
            }
        }
    }

    fun updateLanguage(language: String, localeTag: String) {
        viewModelScope.launch {
            val currentUid = authRepository.getCurrentUserId()
            profileRepository.updateLanguagePreference(language, localeTag, currentUid)
        }
    }

    fun syncToFirestore() {
        viewModelScope.launch {
            _isSaving.value = true
            val result = profileRepository.syncCurrentProfileToRemote()
            _isSaving.value = false
            if (result.isSuccess) {
                _statusMessage.value = "Cloud Firestore के साथ प्रोफ़ाइल सिंक हुई!"
            } else {
                _statusMessage.value = "सिंक त्रुटि: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
