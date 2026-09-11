package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.FirestoreService
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.AuthRepository
import com.example.data.repository.DashboardMetrics
import com.example.data.repository.DashboardRepository
import com.example.data.repository.DashboardRepositoryImpl
import com.example.data.repository.FirebaseAuthRepository
import com.example.data.repository.InquiryFirestoreRepositoryImpl
import com.example.data.repository.ProductFirestoreRepositoryImpl
import com.example.data.repository.UserProfileFirestoreRepositoryImpl
import com.example.data.db.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * DashboardViewModel manages aggregated business metrics, real-time Firestore catalog statistics,
 * buyer inquiry volume, sales revenue, and fair pricing analytics for KAARIGAR.
 */
class DashboardViewModel(
    application: Application,
    repository: DashboardRepository? = null,
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

    val dashboardRepository: DashboardRepository = repository ?: run {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        val productRepo = ProductFirestoreRepositoryImpl(
            firestoreService = fsService,
            productDao = db.productDao(),
            authRepository = authRepository
        )
        val inquiryRepo = InquiryFirestoreRepositoryImpl(
            firestoreService = fsService,
            buyerInquiryDao = db.buyerInquiryDao(),
            productDao = db.productDao(),
            authRepository = authRepository
        )
        val profileRepo = UserProfileFirestoreRepositoryImpl(
            firestoreService = fsService,
            userPreferencesRepository = userPrefs,
            authRepository = authRepository
        )
        DashboardRepositoryImpl(
            firestoreService = fsService,
            productFirestoreRepository = productRepo,
            inquiryFirestoreRepository = inquiryRepo,
            userProfileFirestoreRepository = profileRepo,
            authRepository = authRepository
        )
    }

    // Real-time live dashboard metrics
    val dashboardMetrics: StateFlow<DashboardMetrics> = dashboardRepository.dashboardMetrics

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _refreshStatus = MutableStateFlow<String?>(null)
    val refreshStatus: StateFlow<String?> = _refreshStatus.asStateFlow()

    fun refreshDashboard() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val result = dashboardRepository.refreshDashboard()
            _isRefreshing.value = false
            if (result.isSuccess) {
                _refreshStatus.value = "डैशबोर्ड आँकड़े अपडेट हुए (Dashboard Synchronized)"
            } else {
                _refreshStatus.value = "सिंक त्रुटि: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun clearRefreshStatus() {
        _refreshStatus.value = null
    }
}
