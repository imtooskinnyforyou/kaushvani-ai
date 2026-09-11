package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.firebase.FirestoreService
import com.example.data.model.ProductEntity
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.AuthRepository
import com.example.data.repository.FirebaseAuthRepository
import com.example.data.repository.ProductFirestoreRepository
import com.example.data.repository.ProductFirestoreRepositoryImpl
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * UI State for the Product Catalog and Real-Time Cloud Sync
 */
sealed interface ProductUiState {
    object Loading : ProductUiState
    data class Success(
        val products: List<ProductEntity>,
        val filteredCount: Int,
        val totalCount: Int,
        val isCloudConnected: Boolean = true
    ) : ProductUiState
    data class Empty(val message: String = "कोई शिल्प उत्पाद नहीं मिला") : ProductUiState
    data class Error(val message: String) : ProductUiState
}

/**
 * Cloud Sync State indicator for real-time Firestore synchronization
 */
sealed interface SyncStatus {
    object Idle : SyncStatus
    object Syncing : SyncStatus
    data class Synced(val timestamp: Long = System.currentTimeMillis()) : SyncStatus
    data class Offline(val reason: String = "ऑफ़लाइन मोड (Local Room Cache)") : SyncStatus
    data class Error(val errorMessage: String) : SyncStatus
}

/**
 * ProductViewModel manages real-time catalog items, craft filtering,
 * search, and bidirectional synchronization between Room DB and Google Cloud Firestore.
 */
class ProductViewModel(
    application: Application,
    repository: ProductFirestoreRepository? = null,
    val firestoreService: FirestoreService? = null,
    val authRepository: AuthRepository? = null,
    val firestore: com.google.firebase.firestore.FirebaseFirestore? = null
) : AndroidViewModel(application) {

    private val productRepository: ProductFirestoreRepository = repository ?: run {
        val db = AppDatabase.getDatabase(application.applicationContext)
        val fsService = firestoreService ?: FirestoreService(application.applicationContext, firestore)
        val userPrefs = UserPreferencesRepository(application.applicationContext)
        val authRepo = authRepository ?: FirebaseAuthRepository(
            context = application.applicationContext,
            userPreferencesRepository = userPrefs,
            firestoreService = fsService
        )
        ProductFirestoreRepositoryImpl(
            firestoreService = fsService,
            productDao = db.productDao(),
            authRepository = authRepo
        )
    }

    companion object {
        private const val TAG = "ProductViewModel"
    }

    // Filter & Search criteria
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _selectedCraftType = MutableStateFlow<String?>(null)
    val selectedCraftType: StateFlow<String?> = _selectedCraftType.asStateFlow()

    private val _selectedStatus = MutableStateFlow<String?>(null)
    val selectedStatus: StateFlow<String?> = _selectedStatus.asStateFlow()

    // Active selected product for detail pane
    private val _selectedProduct = MutableStateFlow<ProductEntity?>(null)
    val selectedProduct: StateFlow<ProductEntity?> = _selectedProduct.asStateFlow()

    // Sync status tracking
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    // Master stream: real-time Firestore stream with Room fallback
    val allProducts: StateFlow<List<ProductEntity>> = productRepository.observeAllProducts()
        .onStart { _syncStatus.value = SyncStatus.Syncing }
        .onEach { _syncStatus.value = SyncStatus.Synced() }
        .catch { e ->
            Log.e(TAG, "Error in real-time product stream: ${e.message}")
            _syncStatus.value = SyncStatus.Error(e.message ?: "Sync error")
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    // UI State combining real-time products with filters
    val uiState: StateFlow<ProductUiState> = combine(
        allProducts,
        _searchQuery,
        _selectedCategory,
        _selectedCraftType,
        _selectedStatus
    ) { products, query, category, craft, status ->
        if (products.isEmpty()) {
            return@combine ProductUiState.Empty("कोई उत्पाद उपलब्ध नहीं है। नया शिल्प जोड़ें।")
        }

        val filtered = products.filter { product ->
            val matchesQuery = query.isBlank() ||
                    product.title.contains(query, ignoreCase = true) ||
                    product.regionalTitle.contains(query, ignoreCase = true) ||
                    product.category.contains(query, ignoreCase = true) ||
                    product.craftType.contains(query, ignoreCase = true) ||
                    product.tags.contains(query, ignoreCase = true)

            val matchesCategory = category.isNullOrBlank() || category == "All" || product.category.equals(category, ignoreCase = true)
            val matchesCraft = craft.isNullOrBlank() || craft == "All" || product.craftType.equals(craft, ignoreCase = true)
            val matchesStatus = status.isNullOrBlank() || status == "All" || product.status.equals(status, ignoreCase = true)

            matchesQuery && matchesCategory && matchesCraft && matchesStatus
        }

        if (filtered.isEmpty()) {
            ProductUiState.Empty("खोज के अनुसार कोई परिणाम नहीं मिला")
        } else {
            ProductUiState.Success(
                products = filtered,
                filteredCount = filtered.size,
                totalCount = products.size,
                isCloudConnected = true
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = ProductUiState.Loading
    )

    // ============================================================================
    // USER ACTIONS & CRUD
    // ============================================================================

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun selectCraftType(craft: String?) {
        _selectedCraftType.value = craft
    }

    fun selectStatus(status: String?) {
        _selectedStatus.value = status
    }

    fun selectProduct(product: ProductEntity?) {
        _selectedProduct.value = product
    }

    fun loadProductById(productId: Long) {
        viewModelScope.launch {
            val result = productRepository.getProductById(productId)
            _selectedProduct.value = result.getOrNull()
        }
    }

    fun saveProduct(
        product: ProductEntity,
        artisanId: String? = null,
        onComplete: (Boolean, Long?, String?) -> Unit = { _, _, _ -> }
    ) {
        viewModelScope.launch {
            _syncStatus.value = SyncStatus.Syncing
            val result = productRepository.saveProduct(product, artisanId)
            if (result.isSuccess) {
                val newId = result.getOrNull()
                _syncStatus.value = SyncStatus.Synced()
                onComplete(true, newId, null)
            } else {
                val error = result.exceptionOrNull()?.message ?: "उत्पाद सहेजने में त्रुटि"
                _syncStatus.value = SyncStatus.Error(error)
                onComplete(false, null, error)
            }
        }
    }

    fun updateProduct(
        product: ProductEntity,
        onComplete: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            _syncStatus.value = SyncStatus.Syncing
            val result = productRepository.updateProduct(product)
            if (result.isSuccess) {
                _syncStatus.value = SyncStatus.Synced()
                if (_selectedProduct.value?.id == product.id) {
                    _selectedProduct.value = product
                }
                onComplete(true, null)
            } else {
                val error = result.exceptionOrNull()?.message ?: "अपडेट करने में त्रुटि"
                _syncStatus.value = SyncStatus.Error(error)
                onComplete(false, error)
            }
        }
    }

    fun deleteProduct(
        productId: Long,
        onComplete: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            _syncStatus.value = SyncStatus.Syncing
            val result = productRepository.deleteProduct(productId)
            if (result.isSuccess) {
                _syncStatus.value = SyncStatus.Synced()
                if (_selectedProduct.value?.id == productId) {
                    _selectedProduct.value = null
                }
                onComplete(true, null)
            } else {
                val error = result.exceptionOrNull()?.message ?: "हटाने में त्रुटि"
                _syncStatus.value = SyncStatus.Error(error)
                onComplete(false, error)
            }
        }
    }

    fun refreshProducts() {
        viewModelScope.launch {
            _syncStatus.value = SyncStatus.Syncing
            val result = productRepository.syncRemoteToLocal()
            if (result.isSuccess) {
                _syncStatus.value = SyncStatus.Synced()
            } else {
                _syncStatus.value = SyncStatus.Error(result.exceptionOrNull()?.message ?: "Sync error")
            }
        }
    }
}
