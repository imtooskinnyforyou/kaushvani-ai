package com.example.di

import com.example.data.ai.DynamicPricingService
import com.example.data.ai.DynamicPricingServiceImpl
import com.example.data.ai.GeminiMetadataService
import com.example.data.ai.GeminiMetadataServiceImpl
import com.example.data.ai.HistoricalDynamicPricingEngine
import com.example.data.ai.HistoricalDynamicPricingEngineImpl
import com.example.data.api.KaarigarApiService
import com.example.data.api.RetrofitClient
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.CraftTaxonomyRepository
import com.example.data.voice.VoiceCatalogParser
import com.example.data.voice.VoiceToTextService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * AppModule defines core application-wide singletons including coroutine dispatchers,
 * networking services, preferences, voice-to-text processing, and craft taxonomy repositories.
 */
val appModule = module {
    single<CoroutineDispatcher>(named("ioDispatcher")) { Dispatchers.IO }
    single<CoroutineDispatcher>(named("mainDispatcher")) { Dispatchers.Main }
    single<CoroutineDispatcher>(named("defaultDispatcher")) { Dispatchers.Default }

    single { UserPreferencesRepository(androidContext()) }

    single<KaarigarApiService> { RetrofitClient.apiService }
    single<DynamicPricingService> { DynamicPricingServiceImpl() }
    single<HistoricalDynamicPricingEngine> { HistoricalDynamicPricingEngineImpl() }
    single<GeminiMetadataService> { GeminiMetadataServiceImpl() }
    single { CraftTaxonomyRepository }

    single { VoiceToTextService(androidContext()) }
    single { VoiceCatalogParser(get()) }
}

