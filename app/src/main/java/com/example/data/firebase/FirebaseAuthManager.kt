package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Authenticating : AuthState()
    data class Authenticated(
        val uid: String,
        val email: String?,
        val displayName: String?,
        val isAnonymous: Boolean = false,
        val role: String = "ARTISAN"
    ) : AuthState()
    data class Error(val message: String) : AuthState()
}

data class AuthUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val phoneNumber: String?,
    val photoUrl: String?,
    val isAnonymous: Boolean
)

/**
 * FirebaseAuthManager: Manages user authentication, anonymous login, email/password,
 * and user identity state flows for multi-user capabilities.
 */
class FirebaseAuthManager(
    private val context: Context,
    private val authInstance: FirebaseAuth? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    companion object {
        private const val TAG = "FirebaseAuthManager"
    }

    private val auth: FirebaseAuth? get() = authInstance ?: FirebaseInitializer.getAuthInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    init {
        FirebaseInitializer.initialize(context)
        observeAuthState()
    }

    private fun observeAuthState() {
        auth?.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                val authUser = AuthUser(
                    uid = user.uid,
                    email = user.email,
                    displayName = user.displayName ?: if (user.isAnonymous) "Guest Artisan/Buyer" else "User",
                    phoneNumber = user.phoneNumber,
                    photoUrl = user.photoUrl?.toString(),
                    isAnonymous = user.isAnonymous
                )
                _currentUser.value = authUser
                _authState.value = AuthState.Authenticated(
                    uid = user.uid,
                    email = user.email,
                    displayName = authUser.displayName,
                    isAnonymous = user.isAnonymous
                )
                Log.d(TAG, "User authenticated: uid=${user.uid}, email=${user.email}, isAnon=${user.isAnonymous}")
            } else {
                _currentUser.value = null
                _authState.value = AuthState.Unauthenticated
                Log.d(TAG, "User signed out or unauthenticated.")
            }
        }
    }

    /**
     * Signs in anonymously to enable immediate multi-user Firestore read/write permissions
     * without blocking the artisan or buyer with mandatory sign-up walls.
     */
    suspend fun signInAnonymously(
        displayName: String = "Artisan User",
        role: String = "ARTISAN"
    ): Result<AuthUser> = withContext(ioDispatcher) {
        val firebaseAuth = auth ?: return@withContext Result.failure(IllegalStateException("FirebaseAuth is unavailable."))
        _authState.value = AuthState.Authenticating

        try {
            val result = firebaseAuth.signInAnonymously().await()
            val user = result.user ?: throw IllegalStateException("Firebase user was null after anonymous login.")
            
            // Set profile display name
            if (displayName.isNotBlank()) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                user.updateProfile(profileUpdates).await()
            }

            val authUser = AuthUser(
                uid = user.uid,
                email = null,
                displayName = displayName,
                phoneNumber = null,
                photoUrl = null,
                isAnonymous = true
            )
            _currentUser.value = authUser
            _authState.value = AuthState.Authenticated(
                uid = user.uid,
                email = null,
                displayName = displayName,
                isAnonymous = true,
                role = role
            )
            Log.i(TAG, "Anonymous sign-in successful for user: ${user.uid} as $displayName")
            Result.success(authUser)
        } catch (e: Exception) {
            Log.e(TAG, "Anonymous sign-in failed: ${e.message}", e)
            _authState.value = AuthState.Error(e.message ?: "Sign-in failed")
            Result.failure(e)
        }
    }

    /**
     * Signs in with email and password for registered artisans/buyers.
     */
    suspend fun signInWithEmail(email: String, pass: String): Result<AuthUser> = withContext(ioDispatcher) {
        val firebaseAuth = auth ?: return@withContext Result.failure(IllegalStateException("FirebaseAuth is unavailable."))
        _authState.value = AuthState.Authenticating

        try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, pass).await()
            val user = result.user ?: throw IllegalStateException("User null after email login.")
            val authUser = AuthUser(
                uid = user.uid,
                email = user.email,
                displayName = user.displayName,
                phoneNumber = user.phoneNumber,
                photoUrl = user.photoUrl?.toString(),
                isAnonymous = false
            )
            _currentUser.value = authUser
            _authState.value = AuthState.Authenticated(
                uid = user.uid,
                email = user.email,
                displayName = user.displayName,
                isAnonymous = false
            )
            Log.i(TAG, "Email sign-in successful for user: ${user.uid} ($email)")
            Result.success(authUser)
        } catch (e: Exception) {
            Log.e(TAG, "Email sign-in failed: ${e.message}", e)
            _authState.value = AuthState.Error(e.message ?: "Email login failed")
            Result.failure(e)
        }
    }

    /**
     * Registers a new account with email, password, and display name.
     */
    suspend fun registerWithEmail(
        email: String,
        pass: String,
        displayName: String,
        role: String = "ARTISAN"
    ): Result<AuthUser> = withContext(ioDispatcher) {
        val firebaseAuth = auth ?: return@withContext Result.failure(IllegalStateException("FirebaseAuth is unavailable."))
        _authState.value = AuthState.Authenticating

        try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user ?: throw IllegalStateException("User null after account creation.")

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            user.updateProfile(profileUpdates).await()

            val authUser = AuthUser(
                uid = user.uid,
                email = user.email,
                displayName = displayName,
                phoneNumber = null,
                photoUrl = null,
                isAnonymous = false
            )
            _currentUser.value = authUser
            _authState.value = AuthState.Authenticated(
                uid = user.uid,
                email = user.email,
                displayName = displayName,
                isAnonymous = false,
                role = role
            )
            Log.i(TAG, "Account created successfully for user: ${user.uid} ($email)")
            Result.success(authUser)
        } catch (e: Exception) {
            Log.e(TAG, "Registration failed: ${e.message}", e)
            _authState.value = AuthState.Error(e.message ?: "Account registration failed")
            Result.failure(e)
        }
    }

    /**
     * Signs out the current user session.
     */
    fun signOut() {
        try {
            auth?.signOut()
            _currentUser.value = null
            _authState.value = AuthState.Unauthenticated
            Log.i(TAG, "User signed out.")
        } catch (e: Exception) {
            Log.e(TAG, "Sign out exception: ${e.message}", e)
        }
    }

    fun getCurrentUserId(): String? {
        return auth?.currentUser?.uid
    }

    fun isUserLoggedIn(): Boolean {
        return auth?.currentUser != null
    }
}
