package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.db.ProductDao
import com.example.data.model.Product
import com.example.data.model.ProductEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ProductRoomEntityTest {

    private lateinit var database: AppDatabase
    private lateinit var productDao: ProductDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        productDao = database.productDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testProductEntityCreationAndLocalStorageWithNameDescriptionAndImagePaths() = runTest {
        // 1. Create Product with name, description, and multiple image paths
        val imagePaths = listOf(
            "file:///data/user/0/com.example/files/pottery_front.jpg",
            "file:///data/user/0/com.example/files/pottery_detail.jpg",
            "file:///data/user/0/com.example/files/pottery_bottom.jpg"
        )

        val product = Product(
            name = "Handmade Terracotta Diya Set",
            description = "Set of 6 natural clay oil lamps with hand-painted floral borders",
            imagePaths = imagePaths,
            category = "Pottery",
            retailPrice = 499.0
        )

        // Verify entity properties before persistence
        assertEquals("Handmade Terracotta Diya Set", product.name)
        assertEquals("Handmade Terracotta Diya Set", product.title)
        assertEquals("Set of 6 natural clay oil lamps with hand-painted floral borders", product.description)
        assertEquals(3, product.imagePaths.size)
        assertEquals(imagePaths[0], product.imagePaths[0])

        // 2. Insert into ProductDao
        val generatedId = productDao.insert(product)
        assertTrue(generatedId > 0)

        // 3. Retrieve by ID
        val retrieved = productDao.getProductById(generatedId)
        assertNotNull(retrieved)
        assertEquals(generatedId, retrieved?.id)
        assertEquals("Handmade Terracotta Diya Set", retrieved?.name)
        assertEquals("Handmade Terracotta Diya Set", retrieved?.title)
        assertEquals("Set of 6 natural clay oil lamps with hand-painted floral borders", retrieved?.description)
        assertEquals(3, retrieved?.imagePaths?.size)
        assertEquals(imagePaths, retrieved?.imagePaths)

        // 4. Test flow retrieval
        val allProducts = productDao.getAll().first()
        assertTrue(allProducts.any { it.id == generatedId && it.name == "Handmade Terracotta Diya Set" })

        // 5. Test update
        val updatedProduct = retrieved!!.copy(
            name = "Updated Terracotta Diya Collection",
            description = "Updated description with eco-friendly natural colors",
            imagePaths = listOf("file:///data/user/0/com.example/files/updated_diya.jpg")
        )
        productDao.update(updatedProduct)

        val retrievedAfterUpdate = productDao.getProduct(generatedId)
        assertEquals("Updated Terracotta Diya Collection", retrievedAfterUpdate?.name)
        assertEquals("Updated description with eco-friendly natural colors", retrievedAfterUpdate?.description)
        assertEquals(1, retrievedAfterUpdate?.imagePaths?.size)

        // 6. Test delete
        productDao.delete(retrievedAfterUpdate!!)
        val retrievedAfterDelete = productDao.getProductById(generatedId)
        assertNull(retrievedAfterDelete)
    }
}
