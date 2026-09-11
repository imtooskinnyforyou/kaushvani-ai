package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.firebase.FirestoreService
import com.example.data.model.BuyerInquiryEntity
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.AuthRepository
import com.example.data.repository.FirebaseAuthRepository
import com.example.data.repository.InquiryFirestoreRepository
import com.example.data.repository.InquiryFirestoreRepositoryImpl
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * UI State for Buyer Inquiries and Real-Time Multi-User Negotiation
 */
sealed interface InquiryUiState {
    object Loading : InquiryUiState
    data class Success(
        val inquiries: List<BuyerInquiryEntity>,
        val filteredCount: Int,
        val newInquiriesCount: Int,
        val respondedInquiriesCount: Int,
        val totalCount: Int,
        val isCloudConnected: Boolean = true
    ) : InquiryUiState
    data class Empty(val message: String = "कोई पूछताछ उपलब्ध नहीं है") : InquiryUiState
    data class Error(val message: String) : InquiryUiState
}

/**
 * Quick Response Template for Artisans
 */
data class QuickResponseTemplate(
    val id: String,
    val hindiText: String,
    val englishText: String,
    val suggestedStatus: String = "RESPONDED"
)

val StandardArtisanResponseTemplates = listOf(
    QuickResponseTemplate(
        id = "ready_dispatch",
        hindiText = "शिल्प तैयार है। 5-7 कार्य दिवसों में सुरक्षित पैकिंग के साथ भेजा जा सकता है।",
        englishText = "Item is ready. Can be dispatched in 5-7 working days with safe protective packaging.",
        suggestedStatus = "RESPONDED"
    ),
    QuickResponseTemplate(
        id = "bulk_discount",
        hindiText = "थोक ऑर्डर के लिए हम 10-15% विशेष छूट दे सकते हैं। अग्रिम भुगतान आवश्यक है।",
        englishText = "We can offer a 10-15% discount for bulk volume. 50% advance required.",
        suggestedStatus = "NEGOTIATING"
    ),
    QuickResponseTemplate(
        id = "custom_made",
        hindiText = "यह हस्तनिर्मित शिल्प है। निर्माण में 12-15 दिन लगेंगे। डिज़ाइन कस्टमाइज़ किया जा सकता है।",
        englishText = "Handmade on order. Takes 12-15 days to craft. Custom dimensions possible.",
        suggestedStatus = "RESPONDED"
    ),
    QuickResponseTemplate(
        id = "order_accepted",
        hindiText = "आपका ऑर्डर स्वीकार कर लिया गया है। जल्द ही डिलीवरी ट्रैकिंग साझा की जाएगी।",
        englishText = "Order accepted! Dispatch details and tracking will be shared shortly.",
        suggestedStatus = "ACCEPTED"
    )
)

/**
 * InquiryViewModel manages real-time buyer inquiries, RFQs, negotiations,
 * and responses synchronized with Google Cloud Firestore and Room DB.
 */
class InquiryViewModel(
    application: Application,
    repository: InquiryFirestoreRepository? = null,
    val firestoreService: FirestoreService? = null,
    val authRepository: AuthRepository? = null,
    val firestore: com.google.firebase.firestore.FirebaseFirestore? = null
) : AndroidViewModel(application) {

    private val inquiryRepository: InquiryFirestoreRepository = repository ?: run {
        val db = AppDatabase.getDatabase(application.applicationContext)
        val fsService = firestoreService ?: FirestoreService(application.applicationContext, firestore)
        val userPrefs = UserPreferencesRepository(application.applicationContext)
        val authRepo = authRepository ?: FirebaseAuthRepository(
            context = application.applicationContext,
            userPreferencesRepository = userPrefs,
            firestoreService = fsService
        )
        InquiryFirestoreRepositoryImpl(
            firestoreService = fsService,
            buyerInquiryDao = db.buyerInquiryDao(),
            productDao = db.productDao(),
            authRepository = authRepo
        )
    }

    companion object {
        private const val TAG = "InquiryViewModel"
    }

    // Filters
    private val _statusFilter = MutableStateFlow("ALL")
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _userRole = MutableStateFlow("ARTISAN") // "ARTISAN" or "BUYER"
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    private val _selectedInquiry = MutableStateFlow<BuyerInquiryEntity?>(null)
    val selectedInquiry: StateFlow<BuyerInquiryEntity?> = _selectedInquiry.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    // Master stream: real-time Firestore stream with Room fallback
    val allInquiries: StateFlow<List<BuyerInquiryEntity>> = inquiryRepository.observeInquiriesForUser(
        userId = "",
        isArtisan = true
    )
        .onStart { _syncStatus.value = SyncStatus.Syncing }
        .onEach { _syncStatus.value = SyncStatus.Synced() }
        .catch { e ->
            Log.e(TAG, "Error in real-time inquiry stream: ${e.message}")
            _syncStatus.value = SyncStatus.Error(e.message ?: "Sync error")
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    // UI State with combined filtering
    val uiState: StateFlow<InquiryUiState> = combine(
        allInquiries,
        _statusFilter,
        _searchQuery
    ) { inquiries, status, query ->
        if (inquiries.isEmpty()) {
            return@combine InquiryUiState.Empty("अभी तक कोई पूछताछ प्राप्त नहीं हुई है।")
        }

        val newCount = inquiries.count { it.status == "NEW" }
        val respondedCount = inquiries.count { it.status == "RESPONDED" || it.status == "ACCEPTED" }

        val filtered = inquiries.filter { inquiry ->
            val matchesStatus = status == "ALL" || inquiry.status.equals(status, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                    inquiry.productTitle.contains(query, ignoreCase = true) ||
                    inquiry.buyerName.contains(query, ignoreCase = true) ||
                    inquiry.buyerCity.contains(query, ignoreCase = true) ||
                    inquiry.buyerPhone.contains(query, ignoreCase = true) ||
                    inquiry.message.contains(query, ignoreCase = true)

            matchesStatus && matchesQuery
        }

        if (filtered.isEmpty()) {
            InquiryUiState.Empty("चयनित फ़िल्टर के अनुसार कोई पूछताछ नहीं मिली")
        } else {
            InquiryUiState.Success(
                inquiries = filtered,
                filteredCount = filtered.size,
                newInquiriesCount = newCount,
                respondedInquiriesCount = respondedCount,
                totalCount = inquiries.size,
                isCloudConnected = true
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = InquiryUiState.Loading
    )

    // ============================================================================
    // USER ACTIONS
    // ============================================================================

    fun setStatusFilter(status: String) {
        _statusFilter.value = status
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setUserRole(role: String) {
        _userRole.value = role
    }

    fun selectInquiry(inquiry: BuyerInquiryEntity?) {
        _selectedInquiry.value = inquiry
    }

    fun submitInquiry(
        inquiry: BuyerInquiryEntity,
        buyerId: String? = null,
        artisanId: String? = null,
        onComplete: (Boolean, Long?, String?) -> Unit = { _, _, _ -> }
    ) {
        viewModelScope.launch {
            _syncStatus.value = SyncStatus.Syncing
            val result = inquiryRepository.submitInquiry(inquiry, buyerId, artisanId)
            if (result.isSuccess) {
                val newId = result.getOrNull()
                _syncStatus.value = SyncStatus.Synced()
                onComplete(true, newId, null)
            } else {
                val error = result.exceptionOrNull()?.message ?: "पूछताछ भेजने में त्रुटि"
                _syncStatus.value = SyncStatus.Error(error)
                onComplete(false, null, error)
            }
        }
    }

    fun respondToInquiry(
        inquiryId: Long,
        responseText: String,
        status: String = "RESPONDED",
        onComplete: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            _syncStatus.value = SyncStatus.Syncing
            val result = inquiryRepository.respondToInquiry(inquiryId, responseText, status)
            if (result.isSuccess) {
                _syncStatus.value = SyncStatus.Synced()
                if (_selectedInquiry.value?.id == inquiryId) {
                    _selectedInquiry.value = _selectedInquiry.value?.copy(
                        artisanResponse = responseText,
                        status = status
                    )
                }
                onComplete(true, null)
            } else {
                val error = result.exceptionOrNull()?.message ?: "उत्तर भेजने में त्रुटि"
                _syncStatus.value = SyncStatus.Error(error)
                onComplete(false, error)
            }
        }
    }

    fun updateStatus(
        inquiryId: Long,
        status: String,
        onComplete: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            _syncStatus.value = SyncStatus.Syncing
            val result = inquiryRepository.updateInquiryStatus(inquiryId, status)
            if (result.isSuccess) {
                _syncStatus.value = SyncStatus.Synced()
                if (_selectedInquiry.value?.id == inquiryId) {
                    _selectedInquiry.value = _selectedInquiry.value?.copy(status = status)
                }
                onComplete(true, null)
            } else {
                val error = result.exceptionOrNull()?.message ?: "स्थिति बदलने में त्रुटि"
                _syncStatus.value = SyncStatus.Error(error)
                onComplete(false, error)
            }
        }
    }

    fun deleteInquiry(
        inquiryId: Long,
        onComplete: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            _syncStatus.value = SyncStatus.Syncing
            val result = inquiryRepository.deleteInquiry(inquiryId)
            if (result.isSuccess) {
                _syncStatus.value = SyncStatus.Synced()
                if (_selectedInquiry.value?.id == inquiryId) {
                    _selectedInquiry.value = null
                }
                onComplete(true, null)
            } else {
                val error = result.exceptionOrNull()?.message ?: "पूछताछ हटाने में त्रुटि"
                _syncStatus.value = SyncStatus.Error(error)
                onComplete(false, error)
            }
        }
    }
}
