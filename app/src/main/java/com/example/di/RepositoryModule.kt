package com.example.di

import com.example.data.repository.ArtisanAnalyticsRepository
import com.example.data.repository.ArtisanAnalyticsRepositoryImpl
import com.example.data.repository.ArtisanRepository
import com.example.data.repository.AuthRepository
import com.example.data.repository.FirebaseAuthRepository
import com.example.data.repository.InquiryFirestoreRepository
import com.example.data.repository.InquiryFirestoreRepositoryImpl
import com.example.data.repository.PricingAdvisorRepository
import com.example.data.repository.PricingAdvisorRepositoryImpl
import com.example.data.repository.ProductFirestoreRepository
import com.example.data.repository.ProductFirestoreRepositoryImpl
import com.example.data.repository.ProductHistoryRepository
import com.example.data.repository.ProductHistoryRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * RepositoryModule injects cleanly abstracted repository implementations with
 * lifecycle support for offline Room cache + real-time Firestore sync.
 */
val repositoryModule = module {
    single<ProductHistoryRepository> {
        ProductHistoryRepositoryImpl(historyDao = get())
    }
    single<ArtisanAnalyticsRepository> {
        ArtisanAnalyticsRepositoryImpl(
            historyDao = get(),
            productDao = get()
        )
    }
    single<PricingAdvisorRepository> {
        PricingAdvisorRepositoryImpl(
            productDao = get(),
            productHistoryDao = get()
        )
    }
    single<AuthRepository> {
        FirebaseAuthRepository(
            context = androidContext(),
            userPreferencesRepository = get(),
            firestoreService = get(),
            firebaseAuthInstance = com.example.data.firebase.FirebaseInitializer.getAuthInstance(),
            ioDispatcher = get(named("ioDispatcher"))
        )
    }

    single<ProductFirestoreRepository> {
        ProductFirestoreRepositoryImpl(
            firestoreService = get(),
            productDao = get(),
            authRepository = get(),
            ioDispatcher = get(named("ioDispatcher"))
        )
    }

    single<InquiryFirestoreRepository> {
        InquiryFirestoreRepositoryImpl(
            firestoreService = get(),
            buyerInquiryDao = get(),
            productDao = get(),
            authRepository = get(),
            ioDispatcher = get(named("ioDispatcher"))
        )
    }

    single<com.example.data.repository.UserProfileFirestoreRepository> {
        com.example.data.repository.UserProfileFirestoreRepositoryImpl(
            firestoreService = get(),
            userPreferencesRepository = get(),
            authRepository = get(),
            ioDispatcher = get(named("ioDispatcher"))
        )
    }

    single<com.example.data.repository.DashboardRepository> {
        com.example.data.repository.DashboardRepositoryImpl(
            firestoreService = get(),
            productFirestoreRepository = get(),
            inquiryFirestoreRepository = get(),
            userProfileFirestoreRepository = get(),
            authRepository = get(),
            ioDispatcher = get(named("ioDispatcher"))
        )
    }

    single<ArtisanRepository> {
        ArtisanRepository(
            productDao = get(),
            buyerInquiryDao = get(),
            eventDao = get(),
            apiService = get(),
            firestoreService = get(),
            firebaseAuthManager = get(),
            productFirestoreRepository = get(),
            inquiryFirestoreRepository = get(),
            geminiMetadataService = get(),
            dynamicPricingService = get(),
            syncQueueDao = get(),
            queuedAiTaskDao = get(),
            context = androidContext(),
            ioDispatcher = get(named("ioDispatcher"))
        )
    }
}
