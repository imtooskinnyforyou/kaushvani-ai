package com.example

import android.app.Application
import android.util.Log
import com.example.data.firebase.FirebaseInitializer
import com.example.di.appModule
import com.example.di.databaseModule
import com.example.di.firebaseModule
import com.example.di.repositoryModule
import com.example.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class KaarigarApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 1. Initialize Firebase services
        try {
            FirebaseInitializer.initialize(this)
        } catch (e: Exception) {
            Log.e("KaarigarApplication", "Firebase initialization error: ${e.message}", e)
        }

        // 2. Start Koin Dependency Injection
        try {
            startKoin {
                androidLogger(Level.ERROR)
                androidContext(this@KaarigarApplication)
                modules(
                    listOf(
                        appModule,
                        firebaseModule,
                        databaseModule,
                        repositoryModule,
                        viewModelModule
                    )
                )
            }
            Log.i("KaarigarApplication", "Koin Dependency Injection initialized successfully.")
        } catch (e: Exception) {
            Log.e("KaarigarApplication", "Koin initialization error: ${e.message}", e)
        }

        // 3. Schedule WorkManager periodic background sync worker
        try {
            com.example.data.sync.WorkManagerSyncScheduler.schedulePeriodicSync(
                context = this,
                repeatIntervalMinutes = 60
            )
            Log.i("KaarigarApplication", "Scheduled periodic WorkManager catalog sync.")
        } catch (e: Exception) {
            Log.e("KaarigarApplication", "Failed to schedule WorkManager sync: ${e.message}", e)
        }
    }
}
