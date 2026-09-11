package com.example.di

import com.example.data.firebase.FirebaseInitializer
import com.example.ui.viewmodel.ArtisanSalesAnalyticsViewModel
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.DashboardViewModel
import com.example.ui.viewmodel.HunarSetuViewModel
import com.example.ui.viewmodel.InquiryViewModel
import com.example.ui.viewmodel.PricingAdvisorViewModel
import com.example.ui.viewmodel.ProductHistoryPricingViewModel
import com.example.ui.viewmodel.ProductViewModel
import com.example.ui.viewmodel.UserProfileViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * ViewModelModule configures dependency injection bindings for all application ViewModels,
 * providing singleton instances of Firebase Firestore, Authentication, Repositories, and Managers.
 */
val viewModelModule = module {
    viewModel {
        HunarSetuViewModel(
            application = androidApplication(),
            userPreferencesRepository = get(),
            firestoreService = get(),
            authRepository = get(),
            productFirestoreRepo = get(),
            inquiryFirestoreRepo = get(),
            userProfileFirestoreRepo = get(),
            dashboardRepo = get(),
            artisanRepo = get(),
            firebaseAuthMgr = get(),
            firebaseAuth = FirebaseInitializer.getAuthInstance(),
            firestore = FirebaseInitializer.getFirestoreInstance(),
            voiceToTextService = get(),
            voiceCatalogParser = get()
        )
    }

    viewModel {
        DashboardViewModel(
            application = androidApplication(),
            repository = get(),
            authRepo = get(),
            firestoreService = get(),
            firebaseAuth = FirebaseInitializer.getAuthInstance(),
            firestore = FirebaseInitializer.getFirestoreInstance()
        )
    }

    viewModel {
        AuthViewModel(
            application = androidApplication(),
            authRepository = get(),
            userPreferencesRepo = get(),
            firebaseAuth = FirebaseInitializer.getAuthInstance(),
            firestoreService = get(),
            firebaseAuthManager = get()
        )
    }

    viewModel {
        ProductViewModel(
            application = androidApplication(),
            repository = get(),
            firestoreService = get(),
            authRepository = get(),
            firestore = FirebaseInitializer.getFirestoreInstance()
        )
    }

    viewModel {
        InquiryViewModel(
            application = androidApplication(),
            repository = get(),
            firestoreService = get(),
            authRepository = get(),
            firestore = FirebaseInitializer.getFirestoreInstance()
        )
    }

    viewModel {
        UserProfileViewModel(
            application = androidApplication(),
            repository = get(),
            authRepo = get(),
            firestoreService = get(),
            firebaseAuth = FirebaseInitializer.getAuthInstance(),
            firestore = FirebaseInitializer.getFirestoreInstance()
        )
    }

    viewModel {
        ProductHistoryPricingViewModel(
            productHistoryRepository = get(),
            productDao = get(),
            pricingEngine = get()
        )
    }

    viewModel {
        ArtisanSalesAnalyticsViewModel(
            analyticsRepository = get()
        )
    }

    viewModel {
        PricingAdvisorViewModel(
            pricingAdvisorRepository = get()
        )
    }
}

