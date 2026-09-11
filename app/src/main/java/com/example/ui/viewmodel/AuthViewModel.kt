package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.AuthState
import com.example.data.firebase.AuthUser
import com.example.data.firebase.FirebaseAuthManager
import com.example.data.firebase.FirestoreService
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.preferences.UserSession
import com.example.data.repository.AuthRepository
import com.example.data.repository.FirebaseAuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthMode {
    LOGIN,
    REGISTER,
    FORGOT_PASSWORD,
    GUEST_ACCESS
}

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val displayName: String = "",
    val phoneNumber: String = "",
    val role: String = "ARTISAN", // ARTISAN or BUYER
    val craftSpecialty: String = "Clay & Terracotta Craft",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val rememberSession: Boolean = true,
    val authMode: AuthMode = AuthMode.LOGIN,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val nameError: String? = null
)

/**
 * AuthViewModel: Manages authentication state, secure email/password credential input,
 * registration validations, password recovery, session persistence across app launches, and role assignment.
 */
class AuthViewModel(
    application: Application,
    private val authRepository: AuthRepository = FirebaseAuthRepository(application.applicationContext),
    private val userPreferencesRepo: UserPreferencesRepository? = null,
    val firebaseAuth: com.google.firebase.auth.FirebaseAuth? = null,
    val firestoreService: FirestoreService? = null,
    val firebaseAuthManager: FirebaseAuthManager? = null
) : AndroidViewModel(application) {

    private val userPreferencesRepository = userPreferencesRepo ?: UserPreferencesRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val authState: StateFlow<AuthState> = authRepository.authState
    val currentUser: StateFlow<AuthUser?> = authRepository.currentUser

    val userSession: StateFlow<UserSession?> = userPreferencesRepository.userSessionFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        // Auto-restore persisted session or provide default guest identification
        viewModelScope.launch {
            userPreferencesRepository.userSessionFlow.collect { session ->
                if (session != null && session.isLoggedIn) {
                    _uiState.update { current ->
                        current.copy(
                            displayName = session.displayName ?: current.displayName,
                            email = session.email ?: current.email,
                            phoneNumber = session.phoneNumber ?: current.phoneNumber,
                            role = session.role
                        )
                    }
                }
            }
        }
    }

    fun onEmailChange(email: String) {
        _uiState.update {
            it.copy(
                email = email,
                emailError = null,
                errorMessage = null
            )
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                passwordError = null,
                errorMessage = null
            )
        }
    }

    fun onConfirmPasswordChange(confirmPass: String) {
        _uiState.update {
            it.copy(
                confirmPassword = confirmPass,
                confirmPasswordError = null,
                errorMessage = null
            )
        }
    }

    fun onDisplayNameChange(name: String) {
        _uiState.update {
            it.copy(
                displayName = name,
                nameError = null,
                errorMessage = null
            )
        }
    }

    fun onPhoneChange(phone: String) {
        _uiState.update {
            it.copy(phoneNumber = phone, errorMessage = null)
        }
    }

    fun onRoleChange(role: String) {
        _uiState.update {
            it.copy(role = role)
        }
    }

    fun onCraftSpecialtyChange(specialty: String) {
        _uiState.update {
            it.copy(craftSpecialty = specialty)
        }
    }

    fun togglePasswordVisibility() {
        _uiState.update {
            it.copy(isPasswordVisible = !it.isPasswordVisible)
        }
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.update {
            it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible)
        }
    }

    fun toggleRememberSession(remember: Boolean) {
        _uiState.update {
            it.copy(rememberSession = remember)
        }
    }

    fun setAuthMode(mode: AuthMode) {
        _uiState.update {
            it.copy(
                authMode = mode,
                errorMessage = null,
                successMessage = null,
                emailError = null,
                passwordError = null,
                confirmPasswordError = null,
                nameError = null
            )
        }
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(errorMessage = null, successMessage = null)
        }
    }

    private fun validateLoginForm(): Boolean {
        var isValid = true
        val state = _uiState.value
        val cleanEmail = state.email.trim()

        if (cleanEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            _uiState.update { it.copy(emailError = "कृपया वैध ईमेल पता दर्ज करें") }
            isValid = false
        }

        if (state.password.isEmpty() || state.password.length < 6) {
            _uiState.update { it.copy(passwordError = "पासवर्ड कम से कम 6 अक्षरों का होना चाहिए") }
            isValid = false
        }

        return isValid
    }

    private fun validateRegisterForm(): Boolean {
        var isValid = true
        val state = _uiState.value
        val cleanEmail = state.email.trim()
        val cleanName = state.displayName.trim()

        if (cleanName.isEmpty()) {
            _uiState.update { it.copy(nameError = "कृपया नाम दर्ज करें") }
            isValid = false
        }

        if (cleanEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            _uiState.update { it.copy(emailError = "कृपया वैध ईमेल पता दर्ज करें") }
            isValid = false
        }

        if (state.password.length < 6) {
            _uiState.update { it.copy(passwordError = "पासवर्ड कम से कम 6 अक्षरों का होना चाहिए") }
            isValid = false
        }

        if (state.password != state.confirmPassword) {
            _uiState.update { it.copy(confirmPasswordError = "दोनों पासवर्ड मेल नहीं खाते") }
            isValid = false
        }

        return isValid
    }

    fun loginWithEmail(onSuccess: (AuthUser) -> Unit = {}) {
        if (!validateLoginForm()) return

        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

        viewModelScope.launch {
            val result = authRepository.signInWithEmail(
                email = state.email.trim(),
                pass = state.password
            )

            result.fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "सफलतापूर्वक लॉगिन हुआ (${user.email})",
                            password = "",
                            confirmPassword = ""
                        )
                    }
                    onSuccess(user)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "लॉगिन असफल रहा।"
                        )
                    }
                }
            )
        }
    }

    fun registerWithEmail(onSuccess: (AuthUser) -> Unit = {}) {
        if (!validateRegisterForm()) return

        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

        viewModelScope.launch {
            val result = authRepository.registerWithEmail(
                email = state.email.trim(),
                pass = state.password,
                displayName = state.displayName.trim(),
                role = state.role,
                phoneNumber = state.phoneNumber.trim().ifEmpty { null },
                craftSpecialty = state.craftSpecialty
            )

            result.fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "खाता सफलतापूर्वक बन गया! KAUSHVANI में स्वागत है।",
                            password = "",
                            confirmPassword = ""
                        )
                    }
                    onSuccess(user)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "पंजीकरण में समस्या आई।"
                        )
                    }
                }
            )
        }
    }

    fun loginAnonymously(displayName: String, role: String, onSuccess: (AuthUser) -> Unit = {}) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

        viewModelScope.launch {
            val result = authRepository.signInAnonymously(
                displayName = displayName.ifBlank { if (role == "BUYER") "Verified Buyer" else "Master Artisan" },
                role = role
            )

            result.fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "1-टैप गेस्ट सत्र शुरू हुआ (${user.displayName})",
                            displayName = user.displayName ?: ""
                        )
                    }
                    onSuccess(user)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "गेस्ट लॉगिन असफल रहा।"
                        )
                    }
                }
            )
        }
    }

    fun sendPasswordResetEmail() {
        val email = _uiState.value.email.trim()
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(emailError = "कृपया पासवर्ड रीसेट के लिए वैध ईमेल दर्ज करें") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

        viewModelScope.launch {
            val result = authRepository.sendPasswordResetEmail(email)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "पासवर्ड रीसेट लिंक आपके ईमेल ($email) पर भेज दी गई है!"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "पासवर्ड रीसेट लिंक भेजने में विफल।"
                        )
                    }
                }
            )
        }
    }

    fun sendEmailVerification() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

        viewModelScope.launch {
            val result = authRepository.sendEmailVerification()
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "ईमेल सत्यापन लिंक भेज दी गई है। कृपया अपना इनबॉक्स देखें!"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "सत्यापन लिंक भेजने में विफल।"
                        )
                    }
                }
            )
        }
    }

    fun signOut(onComplete: () -> Unit = {}) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    password = "",
                    confirmPassword = "",
                    successMessage = "सत्र सफलतापूर्वक समाप्त (Signed Out)"
                )
            }
            onComplete()
        }
    }
}
