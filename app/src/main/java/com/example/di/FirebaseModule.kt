package com.example.di

import com.example.data.firebase.FirebaseAuthManager
import com.example.data.firebase.FirebaseInitializer
import com.example.data.firebase.FirestoreService
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * FirebaseModule configures lifecycle and singleton management for Firebase Auth,
 * Firestore client, and initialization managers throughout KAARIGAR.
 */
val firebaseModule = module {
    // Core Initializer
    single { FirebaseInitializer }

    // Firebase Auth and Firestore Service Singletons with injected SDK instances and Dispatchers
    single {
        FirebaseAuthManager(
            context = androidContext(),
            authInstance = FirebaseInitializer.getAuthInstance(),
            ioDispatcher = get(named("ioDispatcher"))
        )
    }

    single {
        FirestoreService(
            context = androidContext(),
            firestoreInstance = FirebaseInitializer.getFirestoreInstance(),
            ioDispatcher = get(named("ioDispatcher"))
        )
    }
}

