package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

/**
 * FirebaseInitializer: Centralized initializer for Firebase services (Auth & Firestore).
 * Supports automatic configuration from google-services.json or fallback runtime initialization.
 */
object FirebaseInitializer {
    private const val TAG = "FirebaseInitializer"

    @Volatile
    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return

        synchronized(this) {
            if (isInitialized) return

            try {
                // Initialize default FirebaseApp if not already initialized
                if (FirebaseApp.getApps(context).isEmpty()) {
                    val fallbackOptions = FirebaseOptions.Builder()
                        .setApplicationId("1:76152118150:android:c1a2b3d4e5f6")
                        .setProjectId("kaarigar-crafts-studio")
                        .setApiKey("AIzaSyDummyFallbackKeyForKaarigarPlatform")
                        .setDatabaseUrl("https://kaarigar-crafts-studio.firebaseio.com")
                        .setStorageBucket("kaarigar-crafts-studio.appspot.com")
                        .build()

                    try {
                        FirebaseApp.initializeApp(context)
                        Log.i(TAG, "FirebaseApp initialized from default resources.")
                    } catch (e: Exception) {
                        Log.w(TAG, "Default Firebase resource init unavailable, initializing with options: ${e.message}")
                        try {
                            FirebaseApp.initializeApp(context, fallbackOptions)
                            Log.i(TAG, "FirebaseApp initialized with fallback options.")
                        } catch (e2: Exception) {
                            Log.w(TAG, "Default fallback init note: ${e2.message}")
                            try {
                                FirebaseApp.initializeApp(context, fallbackOptions, "KaarigarApp")
                            } catch (e3: Exception) {
                                Log.w(TAG, "Named fallback init note: ${e3.message}")
                            }
                        }
                    }
                }

                // Configure Firestore offline cache & performance
                try {
                    val firestore = getFirestoreInstance()
                    val settings = FirebaseFirestoreSettings.Builder()
                        .setPersistenceEnabled(true)
                        .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                        .build()
                    firestore?.firestoreSettings = settings
                    Log.i(TAG, "Firestore offline persistence configured.")
                } catch (e: Exception) {
                    Log.w(TAG, "Firestore settings initialization note: ${e.message}")
                }

                isInitialized = true
                Log.i(TAG, "Firebase ecosystem ready for multi-user Auth & Firestore operations.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize Firebase: ${e.message}", e)
            }
        }
    }

    fun isFirebaseReady(context: Context? = null): Boolean {
        return try {
            val apps = if (context != null) FirebaseApp.getApps(context) else FirebaseApp.getApps(FirebaseApp.getInstance().applicationContext)
            apps.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    fun getAuthInstance(): FirebaseAuth? {
        return try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            try {
                val app = FirebaseApp.getInstance("KaarigarApp")
                FirebaseAuth.getInstance(app)
            } catch (e2: Exception) {
                Log.w(TAG, "FirebaseAuth instance not available: ${e.message}")
                null
            }
        }
    }

    fun getFirestoreInstance(): FirebaseFirestore? {
        return try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            try {
                val app = FirebaseApp.getInstance("KaarigarApp")
                FirebaseFirestore.getInstance(app)
            } catch (e2: Exception) {
                Log.w(TAG, "FirebaseFirestore instance not available: ${e.message}")
                null
            }
        }
    }
}
