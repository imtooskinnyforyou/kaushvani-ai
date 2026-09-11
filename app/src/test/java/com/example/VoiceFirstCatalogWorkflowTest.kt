package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.model.ProductEntity
import com.example.data.voice.MultilingualVoiceCatalogResult
import com.example.data.voice.MultilingualVoiceCatalogService
import com.example.ui.components.SupportedRegionalLanguages
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class VoiceFirstCatalogWorkflowTest {

    private lateinit var database: AppDatabase
    private lateinit var service: MultilingualVoiceCatalogService

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        service = MultilingualVoiceCatalogService()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testVoiceAudioDescriptionParsedIntoStructuredNameAndDescription() = runTest {
        // Given: An artisan voice recording description in Hindi
        val spokenHindiAudioDescription = "यह हाथ से बना हुआ मिट्टी का सुराही है जिस पर नक्काशी की गई है। इसमें पानी प्राकृतिक रूप से ठंडा रहता है।"
        val language = SupportedRegionalLanguages.find { it.name == "Hindi" } ?: SupportedRegionalLanguages[0]

        // When: Parsed via MultilingualVoiceCatalogService (using deterministic parser fallback when offline)
        val parsedCatalog: MultilingualVoiceCatalogResult = service.createDeterministicFallback(
            transcript = spokenHindiAudioDescription,
            language = language,
            artisanState = "Uttar Pradesh",
            artisanCluster = "Gorakhpur",
            categoryHint = "Pottery"
        )

        // Then: Structured name (title) and description fields are populated correctly
        assertNotNull(parsedCatalog)
        assertTrue(parsedCatalog.title.isNotBlank())
        assertTrue(parsedCatalog.description.isNotBlank())
        assertEquals("Pottery", parsedCatalog.category)

        // Ensure strict authenticity integrity: No invented certifications
        assertFalse(parsedCatalog.isGiTagged)
        assertEquals("", parsedCatalog.governmentCertification)

        // When: Persisted to local database
        val product = ProductEntity(
            title = parsedCatalog.title,
            regionalTitle = parsedCatalog.regionalTitle,
            description = parsedCatalog.description,
            regionalDescription = parsedCatalog.craftStory,
            category = parsedCatalog.category,
            craftType = parsedCatalog.craftType,
            region = "Gorakhpur, Uttar Pradesh",
            rawMaterialCost = parsedCatalog.rawMaterialCost,
            laborHours = parsedCatalog.laborHours,
            hourlyWageRate = parsedCatalog.hourlyLivingWageRate,
            packagingCost = 60.0,
            fairMinPrice = parsedCatalog.wholesalePrice,
            fairMaxPrice = parsedCatalog.retailPrice,
            retailPrice = parsedCatalog.retailPrice,
            wholesalePrice = parsedCatalog.wholesalePrice,
            stockAvailable = parsedCatalog.stockAvailable,
            imageUri = "sample_terracotta",
            imagePaths = listOf("sample_terracotta", "file:///sample/voice_photo_1.jpg"),
            voiceTranscript = parsedCatalog.rawTranscript,
            originalLanguage = parsedCatalog.detectedLanguage,
            isGiTagged = parsedCatalog.isGiTagged,
            tags = parsedCatalog.tags.joinToString(", "),
            materialsUsed = parsedCatalog.material,
            dimensions = parsedCatalog.dimensions,
            weightKg = 0.8,
            careInstructions = parsedCatalog.careInstructions,
            artisanName = "Ram Prasad Prajapati",
            artisanLocation = "Gorakhpur, Uttar Pradesh",
            status = "PUBLISHED"
        )
        val insertedId = database.productDao().insert(product)
        assertTrue(insertedId > 0)

        // Then: Room entity preserves structured name, description, and image paths
        val savedProduct = database.productDao().getProductById(insertedId)
        assertNotNull(savedProduct)
        assertEquals(parsedCatalog.title, savedProduct?.name)
        assertEquals(parsedCatalog.title, savedProduct?.title)
        assertEquals(parsedCatalog.description, savedProduct?.description)
        assertTrue(savedProduct?.imagePaths?.isNotEmpty() == true)
        assertEquals(2, savedProduct?.imagePaths?.size)
    }
}
