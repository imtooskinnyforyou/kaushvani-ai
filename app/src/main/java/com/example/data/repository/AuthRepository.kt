package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.firebase.AuthState
import com.example.data.firebase.AuthUser
import com.example.data.firebase.FirebaseInitializer
import com.example.data.firebase.FirestoreService
import com.example.data.model.ArtisanProfile
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.preferences.UserSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Clean Architecture Interface for Authentication and Session Persistence in KAARIGAR.
 */
interface AuthRepository {
    val authState: StateFlow<AuthState>
    val currentUser: StateFlow<AuthUser?>
    val userSession: Flow<UserSession?>

    suspend fun signInWithEmail(email: String, pass: String): Result<AuthUser>
    suspend fun registerWithEmail(
        email: String,
        pass: String,
        displayName: String,
        role: String = "ARTISAN",
        phoneNumber: String? = null,
        craftSpecialty: String? = null
    ): Result<AuthUser>
    suspend fun signInAnonymously(displayName: String = "Guest Artisan", role: String = "ARTISAN"): Result<AuthUser>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun sendEmailVerification(): Result<Unit>
    suspend fun updateProfile(displayName: String, photoUrl: String? = null): Result<Unit>
    suspend fun signOut(): Result<Unit>
    suspend fun reloadUser(): Result<AuthUser?>
    fun isUserLoggedIn(): Boolean
    fun getCurrentUserId(): String?
    fun getCurrentUserEmail(): String?
}

/**
 * Production implementation of AuthRepository backed by Firebase Authentication,
 * Jetpack DataStore (session persistence across app lifecycles), and Firestore user passport synchronization.
 */
class FirebaseAuthRepository(
    private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository = UserPreferencesRepository(context),
    private val firestoreService: FirestoreService? = FirestoreService(context),
    private val firebaseAuthInstance: FirebaseAuth? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AuthRepository {

    companion object {
        private const val TAG = "FirebaseAuthRepository"
    }

    private val auth: FirebaseAuth? get() = firebaseAuthInstance ?: FirebaseInitializer.getAuthInstance()
    private val repositoryScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    override val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    override val userSession: Flow<UserSession?> = userPreferencesRepository.userSessionFlow

    init {
        FirebaseInitializer.initialize(context)
        setupAuthStateListener()
    }

    private fun setupAuthStateListener() {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            Log.w(TAG, "FirebaseAuth instance is unavailable during listener setup.")
            return
        }

        firebaseAuth.addAuthStateListener { fa ->
            val user = fa.currentUser
            if (user != null) {
                val authUser = mapFirebaseUser(user)
                _currentUser.value = authUser
                _authState.value = AuthState.Authenticated(
                    uid = user.uid,
                    email = user.email,
                    displayName = authUser.displayName,
                    isAnonymous = user.isAnonymous
                )
                Log.d(TAG, "Auth listener triggered: Logged in as ${user.uid} (${user.email})")

                // Update session persistence timestamp
                repositoryScope.launch {
                    val existingSession = userPreferencesRepository.getPersistedUserSession()
                    val role = existingSession?.role ?: if (user.isAnonymous) "ARTISAN" else "ARTISAN"
                    userPreferencesRepository.saveUserSession(
                        UserSession(
                            uid = user.uid,
                            email = user.email,
                            displayName = authUser.displayName,
                            phoneNumber = user.phoneNumber ?: existingSession?.phoneNumber,
                            photoUrl = user.photoUrl?.toString(),
                            role = role,
                            isAnonymous = user.isAnonymous,
                            isLoggedIn = true,
                            lastLoginTimestamp = System.currentTimeMillis()
                        )
                    )
                }
            } else {
                _currentUser.value = null
                _authState.value = AuthState.Unauthenticated
                Log.d(TAG, "Auth listener triggered: Unauthenticated state.")
            }
        }
    }

    private fun mapFirebaseUser(user: FirebaseUser): AuthUser {
        val defaultName = if (user.isAnonymous) "Guest Artisan/Buyer" else (user.email?.substringBefore("@") ?: "Artisan User")
        return AuthUser(
            uid = user.uid,
            email = user.email,
            displayName = user.displayName ?: defaultName,
            phoneNumber = user.phoneNumber,
            photoUrl = user.photoUrl?.toString(),
            isAnonymous = user.isAnonymous
        )
    }

    override suspend fun signInWithEmail(email: String, pass: String): Result<AuthUser> = withContext(ioDispatcher) {
        val firebaseAuth = auth ?: return@withContext Result.failure(
            IllegalStateException("Firebase Auth सेवा अनुपलब्ध है। कृपया इंटरनेट कनेक्शन जांचें।")
        )

        val cleanEmail = email.trim()
        if (cleanEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            return@withContext Result.failure(IllegalArgumentException("कृपया वैध ईमेल पता दर्ज करें (Enter a valid email)."))
        }
        if (pass.isEmpty() || pass.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("पासवर्ड कम से कम 6 अक्षरों का होना चाहिए (Password must be >= 6 characters)."))
        }

        _authState.value = AuthState.Authenticating

        try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(cleanEmail, pass).await()
            val user = authResult.user ?: throw IllegalStateException("Firebase user was null after login.")
            val authUser = mapFirebaseUser(user)

            _currentUser.value = authUser
            _authState.value = AuthState.Authenticated(
                uid = user.uid,
                email = user.email,
                displayName = authUser.displayName,
                isAnonymous = false
            )

            // Persist session to DataStore
            val session = UserSession(
                uid = user.uid,
                email = user.email,
                displayName = authUser.displayName,
                phoneNumber = user.phoneNumber,
                photoUrl = user.photoUrl?.toString(),
                role = "ARTISAN",
                isAnonymous = false,
                isLoggedIn = true,
                lastLoginTimestamp = System.currentTimeMillis()
            )
            userPreferencesRepository.saveUserSession(session)

            Log.i(TAG, "Email login successful: uid=${user.uid}, email=${user.email}")
            Result.success(authUser)
        } catch (e: Exception) {
            val userMessage = mapAuthError(e)
            Log.e(TAG, "Email login failed: ${e.message}", e)
            _authState.value = AuthState.Error(userMessage)
            Result.failure(Exception(userMessage, e))
        }
    }

    override suspend fun registerWithEmail(
        email: String,
        pass: String,
        displayName: String,
        role: String,
        phoneNumber: String?,
        craftSpecialty: String?
    ): Result<AuthUser> = withContext(ioDispatcher) {
        val firebaseAuth = auth ?: return@withContext Result.failure(
            IllegalStateException("Firebase Auth सेवा अनुपलब्ध है।")
        )

        val cleanEmail = email.trim()
        val cleanName = displayName.trim().ifEmpty { "Artisan" }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            return@withContext Result.failure(IllegalArgumentException("कृपया वैध ईमेल पता दर्ज करें (Valid email required)."))
        }
        if (pass.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("सुरक्षा हेतु पासवर्ड कम से कम 6 अक्षरों का होना आवश्यक है।"))
        }

        _authState.value = AuthState.Authenticating

        try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(cleanEmail, pass).await()
            val user = authResult.user ?: throw IllegalStateException("Firebase user was null after registration.")

            // Set Display Name in Firebase Auth Profile
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(cleanName)
                .build()
            user.updateProfile(profileUpdates).await()

            val authUser = AuthUser(
                uid = user.uid,
                email = user.email,
                displayName = cleanName,
                phoneNumber = phoneNumber,
                photoUrl = null,
                isAnonymous = false
            )

            _currentUser.value = authUser
            _authState.value = AuthState.Authenticated(
                uid = user.uid,
                email = user.email,
                displayName = cleanName,
                isAnonymous = false,
                role = role
            )

            // Save persistent session in DataStore
            val session = UserSession(
                uid = user.uid,
                email = user.email,
                displayName = cleanName,
                phoneNumber = phoneNumber,
                photoUrl = null,
                role = role,
                isAnonymous = false,
                isLoggedIn = true,
                lastLoginTimestamp = System.currentTimeMillis()
            )
            userPreferencesRepository.saveUserSession(session)

            // Save initial user profile in Firestore
            try {
                firestoreService?.saveUserProfile(
                    profile = ArtisanProfile(
                        name = cleanName,
                        craftSpecialty = craftSpecialty ?: "Traditional Handicrafts",
                        phone = phoneNumber ?: "",
                        state = "India"
                    ),
                    userId = user.uid
                )
            } catch (fe: Exception) {
                Log.w(TAG, "Firestore user profile sync deferred: ${fe.message}")
            }

            Log.i(TAG, "User registered successfully: uid=${user.uid}, email=${user.email}")
            Result.success(authUser)
        } catch (e: Exception) {
            val userMessage = mapAuthError(e)
            Log.e(TAG, "User registration failed: ${e.message}", e)
            _authState.value = AuthState.Error(userMessage)
            Result.failure(Exception(userMessage, e))
        }
    }

    override suspend fun signInAnonymously(displayName: String, role: String): Result<AuthUser> = withContext(ioDispatcher) {
        val firebaseAuth = auth ?: return@withContext Result.failure(
            IllegalStateException("Firebase Auth सेवा अनुपलब्ध है।")
        )

        _authState.value = AuthState.Authenticating
        val cleanName = displayName.trim().ifEmpty { if (role == "BUYER") "Verified Buyer" else "Master Artisan" }

        try {
            val authResult = firebaseAuth.signInAnonymously().await()
            val user = authResult.user ?: throw IllegalStateException("Firebase user was null.")

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(cleanName)
                .build()
            user.updateProfile(profileUpdates).await()

            val authUser = AuthUser(
                uid = user.uid,
                email = null,
                displayName = cleanName,
                phoneNumber = null,
                photoUrl = null,
                isAnonymous = true
            )

            _currentUser.value = authUser
            _authState.value = AuthState.Authenticated(
                uid = user.uid,
                email = null,
                displayName = cleanName,
                isAnonymous = true,
                role = role
            )

            // Save persistent session
            val session = UserSession(
                uid = user.uid,
                email = null,
                displayName = cleanName,
                phoneNumber = null,
                photoUrl = null,
                role = role,
                isAnonymous = true,
                isLoggedIn = true,
                lastLoginTimestamp = System.currentTimeMillis()
            )
            userPreferencesRepository.saveUserSession(session)

            Log.i(TAG, "Anonymous guest sign-in successful: ${user.uid} as $cleanName ($role)")
            Result.success(authUser)
        } catch (e: Exception) {
            val userMessage = mapAuthError(e)
            Log.e(TAG, "Anonymous sign-in failed: ${e.message}", e)
            _authState.value = AuthState.Error(userMessage)
            Result.failure(Exception(userMessage, e))
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = withContext(ioDispatcher) {
        val firebaseAuth = auth ?: return@withContext Result.failure(
            IllegalStateException("Firebase Auth सेवा अनुपलब्ध है।")
        )

        val cleanEmail = email.trim()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            return@withContext Result.failure(IllegalArgumentException("कृपया वैध ईमेल दर्ज करें।"))
        }

        try {
            firebaseAuth.sendPasswordResetEmail(cleanEmail).await()
            Log.i(TAG, "Password reset email dispatched to $cleanEmail")
            Result.success(Unit)
        } catch (e: Exception) {
            val userMessage = mapAuthError(e)
            Log.e(TAG, "Password reset failed for $cleanEmail: ${e.message}", e)
            Result.failure(Exception(userMessage, e))
        }
    }

    override suspend fun sendEmailVerification(): Result<Unit> = withContext(ioDispatcher) {
        val user = auth?.currentUser ?: return@withContext Result.failure(
            IllegalStateException("कोई सक्रिय उपयोगकर्ता सत्र नहीं मिला।")
        )

        try {
            user.sendEmailVerification().await()
            Log.i(TAG, "Email verification link sent to ${user.email}")
            Result.success(Unit)
        } catch (e: Exception) {
            val userMessage = mapAuthError(e)
            Log.e(TAG, "Email verification send failed: ${e.message}", e)
            Result.failure(Exception(userMessage, e))
        }
    }

    override suspend fun updateProfile(displayName: String, photoUrl: String?): Result<Unit> = withContext(ioDispatcher) {
        val user = auth?.currentUser ?: return@withContext Result.failure(
            IllegalStateException("सत्र उपलब्ध नहीं है।")
        )

        try {
            val builder = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName.trim())
            if (photoUrl != null) {
                builder.setPhotoUri(android.net.Uri.parse(photoUrl))
            }
            user.updateProfile(builder.build()).await()

            val updatedAuthUser = mapFirebaseUser(user)
            _currentUser.value = updatedAuthUser

            // Update session
            val session = userPreferencesRepository.getPersistedUserSession()
            if (session != null) {
                userPreferencesRepository.saveUserSession(
                    session.copy(displayName = displayName.trim(), photoUrl = photoUrl)
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> = withContext(ioDispatcher) {
        try {
            auth?.signOut()
            _currentUser.value = null
            _authState.value = AuthState.Unauthenticated
            userPreferencesRepository.clearUserSession()
            Log.i(TAG, "User session signed out and cleared from persistence.")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Sign out error: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun reloadUser(): Result<AuthUser?> = withContext(ioDispatcher) {
        try {
            val user = auth?.currentUser
            if (user != null) {
                user.reload().await()
                val refreshedUser = mapFirebaseUser(user)
                _currentUser.value = refreshedUser
                Result.success(refreshedUser)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isUserLoggedIn(): Boolean {
        return auth?.currentUser != null
    }

    override fun getCurrentUserId(): String? {
        return auth?.currentUser?.uid
    }

    override fun getCurrentUserEmail(): String? {
        return auth?.currentUser?.email
    }

    private fun mapAuthError(e: Throwable): String {
        return when (e) {
            is FirebaseAuthInvalidUserException -> "यह ईमेल पंजीकृत नहीं है या खाता हटा दिया गया है (User not found)."
            is FirebaseAuthInvalidCredentialsException -> "अमान्य ईमेल अथवा पासवर्ड दर्ज किया गया है (Invalid email or password)."
            is FirebaseAuthUserCollisionException -> "यह ईमेल पहले से ही पंजीकृत है। कृपया लॉगिन करें (Email already registered)."
            is FirebaseAuthWeakPasswordException -> "पासवर्ड बहुत कमजोर है। कृपया कम से कम 6 अक्षरों का मजबूत पासवर्ड चुनें।"
            is FirebaseAuthException -> when (e.errorCode) {
                "ERROR_INVALID_EMAIL" -> "अमान्य ईमेल पता (Invalid email format)."
                "ERROR_WRONG_PASSWORD" -> "गलत पासवर्ड। कृपया पुनः प्रयास करें।"
                "ERROR_USER_NOT_FOUND" -> "उपयोगकर्ता नहीं मिला। कृपया पहले पंजीकरण करें।"
                "ERROR_EMAIL_ALREADY_IN_USE" -> "यह ईमेल पहले से पंजीकृत है।"
                "ERROR_WEAK_PASSWORD" -> "पासवर्ड कम से कम 6 अक्षरों का होना चाहिए।"
                "ERROR_TOO_MANY_REQUESTS" -> "बहुत अधिक असफल प्रयास। कृपया कुछ समय बाद पुनः प्रयास करें।"
                else -> e.localizedMessage ?: "प्रमाणीकरण त्रुटि (Authentication error)."
            }
            else -> e.localizedMessage ?: "नेटवर्क या प्रमाणीकरण त्रुटि उत्पन्न हुई।"
        }
    }
}
