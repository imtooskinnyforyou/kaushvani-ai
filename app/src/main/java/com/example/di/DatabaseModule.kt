package com.example.di

import com.example.data.db.AppDatabase
import com.example.data.db.BuyerInquiryDao
import com.example.data.db.CatalogSyncQueueDao
import com.example.data.db.ProductDao
import com.example.data.db.ProductHistoryDao
import com.example.data.db.QueuedAiTaskDao
import com.example.data.db.UserActivityEventDao
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * DatabaseModule provides singletons for SQLite Room database and all DAOs.
 */
val databaseModule = module {
    single<AppDatabase> { AppDatabase.getDatabase(androidContext()) }
    single<ProductDao> { get<AppDatabase>().productDao() }
    single<BuyerInquiryDao> { get<AppDatabase>().buyerInquiryDao() }
    single<UserActivityEventDao> { get<AppDatabase>().userActivityEventDao() }
    single<ProductHistoryDao> { get<AppDatabase>().productHistoryDao() }
    single<CatalogSyncQueueDao> { get<AppDatabase>().catalogSyncQueueDao() }
    single<QueuedAiTaskDao> { get<AppDatabase>().queuedAiTaskDao() }
}
