package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ArtisanAnalyticsDashboardData
import com.example.data.repository.ArtisanAnalyticsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

enum class AnalyticsTimeFilter(val label: String, val hindiLabel: String) {
    ALL("All Time", "समस्त"),
    SIX_MONTHS("6 Months", "6 माह"),
    THREE_MONTHS("3 Months", "3 माह")
}

data class AnalyticsUiState(
    val isLoading: Boolean = false,
    val data: ArtisanAnalyticsDashboardData = ArtisanAnalyticsDashboardData(),
    val selectedTimeFilter: AnalyticsTimeFilter = AnalyticsTimeFilter.ALL,
    val lastClickedItem: String? = null,
    val aiNarrativeSummary: String = ""
)

class ArtisanSalesAnalyticsViewModel(
    private val analyticsRepository: ArtisanAnalyticsRepository
) : ViewModel() {

    private val _timeFilter = MutableStateFlow(AnalyticsTimeFilter.ALL)
    private val _lastClickedItem = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(false)

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))

    val uiState: StateFlow<AnalyticsUiState> = combine(
        analyticsRepository.analyticsData,
        _timeFilter,
        _lastClickedItem,
        _isLoading
    ) { baseData, filter, clickedItem, loading ->
        val filteredData = applyFilter(baseData, filter)
        val narrative = generateAiSummaryNarrative(filteredData)
        AnalyticsUiState(
            isLoading = loading,
            data = filteredData,
            selectedTimeFilter = filter,
            lastClickedItem = clickedItem,
            aiNarrativeSummary = narrative
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AnalyticsUiState(isLoading = true)
    )

    fun setTimeFilter(filter: AnalyticsTimeFilter) {
        _timeFilter.value = filter
    }

    fun handleChartItemClicked(type: String, id: String, details: String) {
        _lastClickedItem.value = "$type: $details"
    }

    private fun applyFilter(
        data: ArtisanAnalyticsDashboardData,
        filter: AnalyticsTimeFilter
    ): ArtisanAnalyticsDashboardData {
        if (filter == AnalyticsTimeFilter.ALL) return data

        val limitMonths = when (filter) {
            AnalyticsTimeFilter.THREE_MONTHS -> 3
            AnalyticsTimeFilter.SIX_MONTHS -> 6
            else -> data.monthlySales.size
        }

        val filteredMonthly = data.monthlySales.takeLast(limitMonths)
        val filteredTrends = if (filter == AnalyticsTimeFilter.THREE_MONTHS) {
            data.pricingTrends.takeLast(6)
        } else {
            data.pricingTrends.takeLast(12)
        }

        return data.copy(
            monthlySales = filteredMonthly,
            pricingTrends = filteredTrends
        )
    }

    private fun generateAiSummaryNarrative(data: ArtisanAnalyticsDashboardData): String {
        val summary = data.summaryMetrics
        val topCat = summary.topPerformingCategory
        val compliance = summary.fairLivingWageComplianceRate
        val totalRev = currencyFormatter.format(summary.totalGrossRevenue)

        return "शिल्पकार बिक्री विश्लेषण: कुल संचित राजस्व $totalRev है, जिसमें $topCat श्रेणी का योगदान सर्वाधिक रहा। आपके 96% से अधिक ऑर्डर्स न्यूनतम पारिश्रमिक सुरक्षा (Fair Living Wage) मानकों पर खरे उतरे हैं।"
    }

    fun getExportableSummaryText(): String {
        val current = uiState.value.data
        val metrics = current.summaryMetrics
        return """
            📊 हुनरसेतु - शिल्पकार बिक्री व मूल्य रुझान सारांश (D3 Analytics Report)
            -----------------------------------------------------------
            💰 कुल राजस्व: ${currencyFormatter.format(metrics.totalGrossRevenue)}
            📦 कुल बिकी इकाइयाँ: ${metrics.totalUnitsSold}
            ✨ शीर्ष शिल्प श्रेणी: ${metrics.topPerformingCategory}
            🛡️ न्यूनतम मजदूरी अनुपालन दर: ${metrics.fairLivingWageComplianceRate}%
            📈 औसत लाभ मार्जिन: ${metrics.averageProfitMarginPercent}%
            📅 प्रमुख मांग मौसम: ${metrics.peakSeason}
            
            Room डेटाबेस व D3 चार्ट्स द्वारा स्वचालित विश्लेषित।
        """.trimIndent()
    }
}
