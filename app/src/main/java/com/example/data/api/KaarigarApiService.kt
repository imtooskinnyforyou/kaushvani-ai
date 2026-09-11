package com.example.data.api

import com.example.data.api.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * KaarigarApiService: Retrofit interface defining the REST API contracts
 * for cloud synchronization of products, buyer inquiries, artisan analytics, and system health.
 */
interface KaarigarApiService {

    // --- SYSTEM & HEALTH ---

    @GET("health")
    suspend fun checkHealth(): Response<BackendHealthResponse>

    // --- PRODUCTS API ---

    @GET("products")
    suspend fun getProducts(
        @Query("category") category: String? = null,
        @Query("craftType") craftType: String? = null,
        @Query("region") region: String? = null,
        @Query("search") query: String? = null,
        @Query("limit") limit: Int? = 100
    ): Response<List<ProductDto>>

    @GET("products/{id}")
    suspend fun getProductById(@Path("id") id: Long): Response<ProductDto>

    @POST("products")
    suspend fun createProduct(@Body product: ProductDto): Response<ProductDto>

    @PUT("products/{id}")
    suspend fun updateProduct(@Path("id") id: Long, @Body product: ProductDto): Response<ProductDto>

    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") id: Long): Response<Unit>

    @POST("products/sync")
    suspend fun syncProducts(@Body products: List<ProductDto>): Response<List<ProductDto>>

    @POST("products/batch-sync")
    suspend fun batchSyncProducts(
        @Body request: ProductBatchSyncRequest
    ): Response<ProductBatchSyncResponse>


    // --- INQUIRIES API ---

    @GET("inquiries")
    suspend fun getInquiries(
        @Query("status") status: String? = null,
        @Query("artisanPhone") artisanPhone: String? = null
    ): Response<List<BuyerInquiryDto>>

    @GET("inquiries/{id}")
    suspend fun getInquiryById(@Path("id") id: Long): Response<BuyerInquiryDto>

    @GET("inquiries/product/{productId}")
    suspend fun getInquiriesForProduct(@Path("productId") productId: Long): Response<List<BuyerInquiryDto>>

    @POST("inquiries")
    suspend fun createInquiry(@Body inquiry: BuyerInquiryDto): Response<BuyerInquiryDto>

    @PATCH("inquiries/{id}/status")
    suspend fun updateInquiryStatus(
        @Path("id") id: Long,
        @Body request: UpdateInquiryStatusRequest
    ): Response<BuyerInquiryDto>

    @POST("inquiries/{id}/respond")
    suspend fun respondToInquiry(
        @Path("id") id: Long,
        @Body request: RespondInquiryRequest
    ): Response<BuyerInquiryDto>

    @DELETE("inquiries/{id}")
    suspend fun deleteInquiry(@Path("id") id: Long): Response<Unit>

    @POST("inquiries/batch-sync")
    suspend fun batchSyncInquiries(
        @Body request: InquiryBatchSyncRequest
    ): Response<InquiryBatchSyncResponse>


    // --- ANALYTICS & ACTIVITY EVENTS API ---

    @GET("analytics/events")
    suspend fun getActivityEvents(): Response<List<UserActivityEventDto>>

    @POST("analytics/events")
    suspend fun logActivityEvent(
        @Body event: UserActivityEventDto
    ): Response<UserActivityEventDto>

    @POST("analytics/events/sync")
    suspend fun syncActivityEvents(
        @Body request: SyncActivityEventsRequest
    ): Response<SyncActivityEventsResponse>
}

