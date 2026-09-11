package com.example.data.repository

import android.util.Log
import com.example.data.firebase.FirestoreService
import com.example.data.model.ArtisanProfile
import com.example.data.preferences.UserPreferencesRepository
import com.example.ui.components.RegionalLanguage
import com.example.ui.components.SupportedRegionalLanguages
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * UserProfileFirestoreRepository defines the contract for User and Artisan Profile management,
 * identity credentials, regional preferences, and real-time synchronization with Cloud Firestore.
 */
interface UserProfileFirestoreRepository {
    /**
     * Real-time stream of the current active artisan/user profile.
     */
    val currentProfileState: StateFlow<ArtisanProfile>

    /**
     * Real-time stream of a user profile by Firestore document ID.
     */
    fun observeUserProfile(userId: String): Flow<ArtisanProfile?>

    /**
     * Real-time stream of all registered artisan passports in the community registry.
     */
    fun observeAllArtisans(): Flow<List<ArtisanProfile>>

    /**
     * Save/Create a user profile in Firestore and local state.
     */
    suspend fun saveUserProfile(profile: ArtisanProfile, userId: String? = null): Result<Unit>

    /**
     * Update an existing user profile in local state and Firestore.
     */
    suspend fun updateUserProfile(profile: ArtisanProfile, userId: String? = null): Result<Unit>

    /**
     * Fetch user profile snapshot by user ID (checking local state first, then Firestore).
     */
    suspend fun getUserProfile(userId: String): Result<ArtisanProfile?>

    /**
     * Delete user profile from Firestore.
     */
    suspend fun deleteUserProfile(userId: String): Result<Unit>

    /**
     * Update regional language preference across Firestore and local DataStore.
     */
    suspend fun updateLanguagePreference(language: String, localeTag: String, userId: String? = null): Result<Unit>

    /**
     * Update complete RegionalLanguage configuration in Firestore user document and local DataStore.
     */
    suspend fun updateRegionalLanguage(language: RegionalLanguage, userId: String? = null): Result<Unit>

    /**
     * Update artisan certification credentials (PM Vishwakarma, Artisan Card, UPI ID).
     */
    suspend fun updateArtisanCredentials(
        cardNo: String,
        isVishwakarmaEnrolled: Boolean,
        upiId: String,
        userId: String? = null
    ): Result<Unit>

    /**
     * Push current profile to Cloud Firestore.
     */
    suspend fun syncCurrentProfileToRemote(): Result<Unit>
}

/**
 * Production implementation of [UserProfileFirestoreRepository] with local in-memory/DataStore caching
 * and real-time Cloud Firestore synchronization.
 */
class UserProfileFirestoreRepositoryImpl(
    private val firestoreService: FirestoreService,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : UserProfileFirestoreRepository {

    companion object {
        private const val TAG = "UserProfileRepo"
        val DEFAULT_PROFILE = ArtisanProfile(
            name = "Ram Prasad Prajapati",
            craftSpecialty = "Traditional Terracotta & Pottery",
            experienceYears = 18,
            villageOrCluster = "Bhiti Rawat, Gorakhpur",
            state = "Uttar Pradesh",
            phone = "+91 98765 43210",
            upiId = "ramprasad.artisan@upi",
            artisanCardNo = "PMV-UP-2024-8849",
            isVishwakarmaEnrolled = true,
            totalSalesCount = 142,
            totalRevenue = 184500.0,
            preferredLanguage = "Hindi",
            localePreference = "hi-IN"
        )
    }

    private val repositoryScope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val _currentProfile = MutableStateFlow(DEFAULT_PROFILE)
    override val currentProfileState: StateFlow<ArtisanProfile> = _currentProfile.asStateFlow()

    init {
        // Sync with authenticated user session and Firestore on launch
        repositoryScope.launch {
            authRepository?.currentUser?.collect { user ->
                if (user != null) {
                    val uid = user.uid
                    // If user profile exists in Firestore, pull and update local flow
                    val remoteResult = firestoreService.getUserProfile(uid)
                    val remoteProfile = remoteResult.getOrNull()
                    if (remoteProfile != null) {
                        _currentProfile.value = remoteProfile
                        Log.i(TAG, "Loaded remote Firestore profile for authenticated user: $uid")
                    } else {
                        // Initialize remote profile with current defaults
                        val initialProfile = _currentProfile.value.copy(
                            name = user.displayName?.ifBlank { _currentProfile.value.name } ?: _currentProfile.value.name,
                            phone = user.phoneNumber ?: _currentProfile.value.phone
                        )
                        _currentProfile.value = initialProfile
                        firestoreService.saveUserProfile(initialProfile, uid)
                        Log.i(TAG, "Initialized default Firestore profile for new user: $uid")
                    }
                }
            }
        }
    }

    override fun observeUserProfile(userId: String): Flow<ArtisanProfile?> {
        return firestoreService.observeUserProfile(userId)
            .onEach { remoteProfile ->
                if (remoteProfile != null) {
                    val currentUid = authRepository?.getCurrentUserId()
                    if (currentUid == userId) {
                        _currentProfile.value = remoteProfile
                    }
                }
            }
            .catch { e ->
                Log.w(TAG, "Error in observeUserProfile stream: ${e.message}")
                emit(if (userId == authRepository?.getCurrentUserId()) _currentProfile.value else null)
            }
            .flowOn(ioDispatcher)
    }

    override fun observeAllArtisans(): Flow<List<ArtisanProfile>> {
        return firestoreService.observeAllArtisans()
            .catch { e ->
                Log.w(TAG, "Error in observeAllArtisans stream: ${e.message}")
                emit(listOf(_currentProfile.value))
            }
            .flowOn(ioDispatcher)
    }

    override suspend fun saveUserProfile(profile: ArtisanProfile, userId: String?): Result<Unit> = withContext(ioDispatcher) {
        try {
            _currentProfile.value = profile
            val targetUid = userId ?: resolveUserId(profile)
            val result = firestoreService.saveUserProfile(profile, targetUid)
            if (result.isSuccess) {
                Log.i(TAG, "Profile saved to Firestore for $targetUid")
            } else {
                Log.w(TAG, "Profile saved locally; Firestore sync notice: ${result.exceptionOrNull()?.message}")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user profile: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(profile: ArtisanProfile, userId: String?): Result<Unit> = withContext(ioDispatcher) {
        saveUserProfile(profile, userId)
    }

    override suspend fun getUserProfile(userId: String): Result<ArtisanProfile?> = withContext(ioDispatcher) {
        try {
            val currentUid = authRepository?.getCurrentUserId()
            if (currentUid == userId && _currentProfile.value.name.isNotBlank()) {
                return@withContext Result.success(_currentProfile.value)
            }
            firestoreService.getUserProfile(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving profile for $userId: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteUserProfile(userId: String): Result<Unit> = withContext(ioDispatcher) {
        try {
            firestoreService.deleteUserProfile(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting profile for $userId: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun updateLanguagePreference(
        language: String,
        localeTag: String,
        userId: String?
    ): Result<Unit> = withContext(ioDispatcher) {
        try {
            val matchedRegional = SupportedRegionalLanguages.find {
                it.name.equals(language, ignoreCase = true) || it.code.equals(language, ignoreCase = true) || it.localeTag.equals(localeTag, ignoreCase = true)
            } ?: SupportedRegionalLanguages.first()

            val updated = _currentProfile.value.copy(
                preferredLanguage = matchedRegional.name,
                localePreference = matchedRegional.localeTag
            )
            _currentProfile.value = updated
            val targetUid = userId ?: resolveUserId(updated)
            userPreferencesRepository.savePreferredLanguage(matchedRegional)
            firestoreService.updateUserLanguagePreference(
                userId = targetUid,
                languageCode = matchedRegional.code,
                languageName = matchedRegional.name,
                localeTag = matchedRegional.localeTag,
                nativeName = matchedRegional.nativeName
            )
            firestoreService.saveUserProfile(updated, targetUid)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating language preference: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun updateRegionalLanguage(
        language: RegionalLanguage,
        userId: String?
    ): Result<Unit> = withContext(ioDispatcher) {
        try {
            val updated = _currentProfile.value.copy(
                preferredLanguage = language.name,
                localePreference = language.localeTag
            )
            _currentProfile.value = updated
            val targetUid = userId ?: resolveUserId(updated)
            userPreferencesRepository.savePreferredLanguage(language)
            firestoreService.updateUserLanguagePreference(
                userId = targetUid,
                languageCode = language.code,
                languageName = language.name,
                localeTag = language.localeTag,
                nativeName = language.nativeName
            )
            firestoreService.saveUserProfile(updated, targetUid)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating regional language in Firestore/DataStore: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun updateArtisanCredentials(
        cardNo: String,
        isVishwakarmaEnrolled: Boolean,
        upiId: String,
        userId: String?
    ): Result<Unit> = withContext(ioDispatcher) {
        try {
            val updated = _currentProfile.value.copy(
                artisanCardNo = cardNo,
                isVishwakarmaEnrolled = isVishwakarmaEnrolled,
                upiId = upiId
            )
            _currentProfile.value = updated
            val targetUid = userId ?: resolveUserId(updated)
            firestoreService.saveUserProfile(updated, targetUid)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating artisan credentials: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun syncCurrentProfileToRemote(): Result<Unit> = withContext(ioDispatcher) {
        try {
            val current = _currentProfile.value
            val targetUid = resolveUserId(current)
            firestoreService.saveUserProfile(current, targetUid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun resolveUserId(profile: ArtisanProfile): String {
        return authRepository?.getCurrentUserId()
            ?: "artisan_${profile.phone.replace(Regex("[^0-9]"), "")}"
    }
}
