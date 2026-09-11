package com.example.data.db

import androidx.room.*
import com.example.data.model.BuyerInquiryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BuyerInquiryDao {
    @Query("SELECT * FROM buyer_inquiries ORDER BY timestamp DESC")
    fun getAllInquiries(): Flow<List<BuyerInquiryEntity>>

    @Query("SELECT * FROM buyer_inquiries ORDER BY timestamp DESC")
    suspend fun getAllInquiriesList(): List<BuyerInquiryEntity>

    @Query("SELECT * FROM buyer_inquiries WHERE timestamp > :since ORDER BY timestamp DESC")
    suspend fun getInquiriesModifiedSince(since: Long): List<BuyerInquiryEntity>

    @Query("SELECT * FROM buyer_inquiries WHERE status = :status ORDER BY timestamp DESC")
    fun getInquiriesByStatus(status: String): Flow<List<BuyerInquiryEntity>>

    @Query("SELECT * FROM buyer_inquiries WHERE productId = :productId ORDER BY timestamp DESC")
    fun getInquiriesForProduct(productId: Long): Flow<List<BuyerInquiryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInquiry(inquiry: BuyerInquiryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInquiries(inquiries: List<BuyerInquiryEntity>)

    @Update
    suspend fun updateInquiry(inquiry: BuyerInquiryEntity)

    @Query("UPDATE buyer_inquiries SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("UPDATE buyer_inquiries SET status = :status, artisanResponse = :response WHERE id = :id")
    suspend fun respondToInquiry(id: Long, status: String, response: String)

    @Query("DELETE FROM buyer_inquiries WHERE id = :id")
    suspend fun deleteInquiryById(id: Long)
}
