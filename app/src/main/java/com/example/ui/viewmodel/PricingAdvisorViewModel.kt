package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.PricingAdvisorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PricingAdvisorUiState {
    data object Loading : PricingAdvisorUiState
    data class Success(val result: PricingAdvisorResult) : PricingAdvisorUiState
    data class Error(val message: String) : PricingAdvisorUiState
}

class PricingAdvisorViewModel(
    private val pricingAdvisorRepository: PricingAdvisorRepository
) : ViewModel() {

    private val _inputState = MutableStateFlow(NewProductPricingInput())
    val inputState: StateFlow<NewProductPricingInput> = _inputState.asStateFlow()

    private val _uiState = MutableStateFlow<PricingAdvisorUiState>(PricingAdvisorUiState.Loading)
    val uiState: StateFlow<PricingAdvisorUiState> = _uiState.asStateFlow()

    private val _selectedTierType = MutableStateFlow(PricingTierType.RECOMMENDED_RETAIL)
    val selectedTierType: StateFlow<PricingTierType> = _selectedTierType.asStateFlow()

    private val _appliedPriceEvent = MutableStateFlow<Double?>(null)
    val appliedPriceEvent: StateFlow<Double?> = _appliedPriceEvent.asStateFlow()

    init {
        loadAdvice()
    }

    fun setInitialData(
        title: String = "",
        category: String = "Pottery",
        craftType: String = "Terracotta",
        materialCost: Double = 250.0,
        laborHours: Double = 4.0,
        hourlyWageRate: Double = 180.0,
        packagingCost: Double = 50.0,
        isGiTagged: Boolean = false
    ) {
        _inputState.value = NewProductPricingInput(
            title = title,
            category = category,
            craftType = craftType,
            materialCost = materialCost,
            laborHours = laborHours,
            hourlyWageRate = hourlyWageRate,
            packagingCost = packagingCost,
            isGiTagged = isGiTagged
        )
        loadAdvice()
    }

    fun updateMaterialCost(cost: Double) {
        _inputState.value = _inputState.value.copy(materialCost = cost.coerceAtLeast(0.0))
        loadAdvice()
    }

    fun adjustMaterialCostBy(delta: Double) {
        val newCost = (_inputState.value.materialCost + delta).coerceAtLeast(0.0)
        updateMaterialCost(newCost)
    }

    fun updateLaborHours(hours: Double) {
        _inputState.value = _inputState.value.copy(laborHours = hours.coerceAtLeast(0.25))
        loadAdvice()
    }

    fun adjustLaborHoursBy(delta: Double) {
        val newHours = (_inputState.value.laborHours + delta).coerceAtLeast(0.5)
        updateLaborHours(newHours)
    }

    fun updateCategory(category: String) {
        _inputState.value = _inputState.value.copy(category = category)
        loadAdvice()
    }

    fun updateCraftType(craftType: String) {
        _inputState.value = _inputState.value.copy(craftType = craftType)
        loadAdvice()
    }

    fun updateTitle(title: String) {
        _inputState.value = _inputState.value.copy(title = title)
        loadAdvice()
    }

    fun toggleGiTag(isGi: Boolean) {
        _inputState.value = _inputState.value.copy(isGiTagged = isGi)
        loadAdvice()
    }

    fun selectTier(tierType: PricingTierType) {
        _selectedTierType.value = tierType
    }

    fun applySelectedTierPrice(): Double? {
        val current = (_uiState.value as? PricingAdvisorUiState.Success)?.result ?: return null
        val chosenTier = current.tiers.find { it.tierType == _selectedTierType.value }
            ?: current.recommendedTier
        _appliedPriceEvent.value = chosenTier.recommendedPrice
        return chosenTier.recommendedPrice
    }

    fun resetAppliedEvent() {
        _appliedPriceEvent.value = null
    }

    fun reloadAdvice() {
        loadAdvice()
    }

    private fun loadAdvice() {
        viewModelScope.launch {
            _uiState.value = PricingAdvisorUiState.Loading
            try {
                val result = pricingAdvisorRepository.generatePricingAdvice(_inputState.value)
                _uiState.value = PricingAdvisorUiState.Success(result)
            } catch (e: Exception) {
                _uiState.value = PricingAdvisorUiState.Error(
                    e.localizedMessage ?: "मूल्य निर्धारण सलाह प्राप्त करने में विफल (Failed to generate pricing advice)"
                )
            }
        }
    }
}
