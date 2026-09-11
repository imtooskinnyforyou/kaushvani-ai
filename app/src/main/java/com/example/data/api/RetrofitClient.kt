package com.example.data.api

import android.util.Log
import com.example.data.api.model.BackendHealthResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * RetrofitClient: Central network client providing production-grade HTTP configuration,
 * dynamic authorization, timeout management, and safe execution wrappers for the Kaarigar API.
 */
object RetrofitClient {
    private const val TAG = "KaarigarRetrofitClient"
    
    // Default Backend REST API endpoint (configurable)
    const val DEFAULT_BASE_URL = "https://api.kaarigar.crafts.gov.in/v1/"
    
    @Volatile
    private var currentBaseUrl: String = DEFAULT_BASE_URL

    @Volatile
    private var authToken: String? = null

    val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    // Dynamic Authorization and Metadata Header Interceptor
    private val authHeaderInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .header("X-App-Client", "Kaarigar-Android")
            .header("X-Client-Version", "1.1.0")
            .header("X-Platform", "Android-Compose")

        authToken?.let { token ->
            if (token.isNotBlank()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }
        }

        chain.proceed(requestBuilder.build())
    }

    private val loggingInterceptor by lazy {
        HttpLoggingInterceptor { message ->
            Log.d(TAG, message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
    }

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authHeaderInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Volatile
    private var cachedApiService: KaarigarApiService? = null

    val apiService: KaarigarApiService
        get() {
            return cachedApiService ?: synchronized(this) {
                cachedApiService ?: createApiService(currentBaseUrl).also { cachedApiService = it }
            }
        }

    private fun createApiService(baseUrl: String): KaarigarApiService {
        val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(normalizedUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(KaarigarApiService::class.java)
    }

    /**
     * Dynamically update the backend Base URL (e.g., when switching staging/production or local dev backend).
     */
    fun updateBaseUrl(newBaseUrl: String) {
        synchronized(this) {
            currentBaseUrl = newBaseUrl
            cachedApiService = createApiService(newBaseUrl)
            Log.i(TAG, "Backend Base URL updated to: $newBaseUrl")
        }
    }

    /**
     * Set or clear the bearer authentication token.
     */
    fun setAuthToken(token: String?) {
        authToken = token
    }

    fun getBaseUrl(): String = currentBaseUrl

    /**
     * Safely executes an API call returning a Kotlin Result with detailed error reporting.
     */
    suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Result<T> = withContext(Dispatchers.IO) {
        try {
            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    @Suppress("UNCHECKED_CAST")
                    Result.success(Unit as T)
                }
            } else {
                val errorMsg = "HTTP ${response.code()}: ${response.message()}"
                Log.w(TAG, "API call failed with code ${response.code()}: $errorMsg")
                Result.failure(ApiException(response.code(), errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network exception during API call: ${e.message}", e)
            Result.failure(e)
        }
    }
}

class ApiException(val code: Int, message: String) : Exception(message)

