package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.TypeConverters
import com.example.data.db.StringListConverters
import com.example.data.model.BuyerInquiryEntity
import com.example.data.model.CatalogSyncQueueEntity
import com.example.data.model.ProductEntity
import com.example.data.model.ProductHistoryEntity
import com.example.data.model.QueuedAiTaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProductEntity::class,
        BuyerInquiryEntity::class,
        com.example.data.model.UserActivityEventEntity::class,
        ProductHistoryEntity::class,
        CatalogSyncQueueEntity::class,
        QueuedAiTaskEntity::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(StringListConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun buyerInquiryDao(): BuyerInquiryDao
    abstract fun userActivityEventDao(): UserActivityEventDao
    abstract fun productHistoryDao(): ProductHistoryDao
    abstract fun catalogSyncQueueDao(): CatalogSyncQueueDao
    abstract fun queuedAiTaskDao(): QueuedAiTaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kaarigar_database"
                )
                    .addCallback(AppDatabaseCallback(CoroutineScope(SupervisorJob() + Dispatchers.IO)))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kaarigar_database"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(
                            database.productDao(),
                            database.buyerInquiryDao(),
                            database.userActivityEventDao(),
                            database.productHistoryDao()
                        )
                    }
                }
            }
        }

        suspend fun populateInitialData(
            productDao: ProductDao,
            inquiryDao: BuyerInquiryDao,
            eventDao: UserActivityEventDao? = null,
            historyDao: ProductHistoryDao? = null
        ) {
            // Seed sample authentic Indian handicrafts
            val sampleProducts = listOf(
                ProductEntity(
                    id = 1,
                    title = "Handcrafted Earthen Terracotta Decorative Vase",
                    regionalTitle = "हाथ से बना सजावटी मिट्टी का कलश",
                    description = "Masterfully sculpted from natural Gangetic alluvium river clay on a traditional potter wheel. Features hand-carved tribal sun and flora motifs, baked in an authentic wood-fired kiln with a natural ochre glaze.",
                    regionalDescription = "गंगा की शुद्ध प्राकृतिक मिट्टी से चाक पर गढ़ा हुआ पारंपरिक कलश। इस पर हस्तनिर्मित जनजातीय नक्काशी और प्राकृतिक धूप-पकी चमक है।",
                    category = "Pottery",
                    craftType = "Gorakhpur Terracotta",
                    region = "Gorakhpur, Uttar Pradesh",
                    rawMaterialCost = 140.0,
                    laborHours = 6.5,
                    hourlyWageRate = 180.0,
                    packagingCost = 60.0,
                    fairMinPrice = 1370.0,
                    fairMaxPrice = 1750.0,
                    retailPrice = 1499.0,
                    wholesalePrice = 850.0,
                    minOrderQuantity = 15,
                    stockAvailable = 45,
                    imageUri = "sample_terracotta",
                    imageStyleFilter = "Studio Earthen",
                    voiceTranscript = "यह हमारा पुश्तैनी काम है, दो दिन लगते हैं इसे चाक पर ढालकर और बारीक नक्काशी करके पकाने में। 100% प्राकृतिक मिट्टी का बना है।",
                    originalLanguage = "Hindi",
                    isGiTagged = true,
                    tags = "Terracotta, Home Decor, Eco-friendly, GI Tag, Clay Art, Handcrafted",
                    materialsUsed = "Natural Gangetic clay, wood ash, organic earthen pigments",
                    dimensions = "30cm H x 18cm Dia",
                    weightKg = 1.4,
                    careInstructions = "Wipe with a soft dry cloth. Do not soak in water.",
                    artisanName = "Ram Prasad Prajapati",
                    artisanLocation = "Bhiti Rawat, Gorakhpur, UP",
                    status = "PUBLISHED",
                    viewCount = 142,
                    inquiryCount = 4
                ),
                ProductEntity(
                    id = 2,
                    title = "Royal Crimson & Gold Pure Katan Silk Banarasi Dupatta",
                    regionalTitle = "शाही बनारसी कतान सिल्क दुपट्टा (ज़री वर्क)",
                    description = "Handwoven on a heritage pit loom using 100% pure Mulberry silk yarn and electroplated gold Zari threads. Adorned with centuries-old floral Kadwa booti patterns and grand Meenakari borders.",
                    regionalDescription = "शुद्ध रेशम और असली ज़री तारों से हथकरघे पर 12 दिनों में बुना हुआ उत्कृष्ट दुपट्टा। विवाह और उत्सवों के लिए उत्तम।",
                    category = "Handloom & Textiles",
                    craftType = "Banarasi Silk Weaving",
                    region = "Varanasi, Uttar Pradesh",
                    rawMaterialCost = 2800.0,
                    laborHours = 48.0,
                    hourlyWageRate = 200.0,
                    packagingCost = 150.0,
                    fairMinPrice = 12550.0,
                    fairMaxPrice = 16800.0,
                    retailPrice = 13500.0,
                    wholesalePrice = 8800.0,
                    minOrderQuantity = 5,
                    stockAvailable = 12,
                    imageUri = "sample_banarasi",
                    imageStyleFilter = "Silk Glow",
                    voiceTranscript = "बनारस के बुनकर परिवार से हैं। इस दुपट्टे को हथकरघे पर दो हफ्ते में तैयार किया गया है। शुद्ध कतान रेशम का धागा इस्तेमाल हुआ है।",
                    originalLanguage = "Hindi",
                    isGiTagged = true,
                    tags = "Banarasi Silk, Handloom, Zari, Wedding Wear, GI Certified, Varanasi",
                    materialsUsed = "100% Mulberry Katan Silk, Gold-coated Zari thread",
                    dimensions = "2.5m L x 0.9m W",
                    weightKg = 0.35,
                    careInstructions = "Strictly dry clean only. Store wrapped in pure muslin cloth.",
                    artisanName = "Mohammad Aslam Ansari",
                    artisanLocation = "Madanpura, Varanasi, UP",
                    status = "PUBLISHED",
                    viewCount = 288,
                    inquiryCount = 7
                ),
                ProductEntity(
                    id = 3,
                    title = "Tribal Lost-Wax Cast Brass Dhokra Horse Figurine",
                    regionalTitle = "पारंपरिक ढोकरा पीतल शिल्प घोड़ा",
                    description = "Created using the 4,000-year-old non-ferrous lost-wax metal casting technique. Each piece is unique, hand-modeled using natural beeswax wires and cast in recycled scrap brass with an antique oxidized patina.",
                    regionalDescription = "प्राचीन मोम-ढालाई विधि द्वारा निर्मित ठोस पीतल की जनजातीय अश्व मूर्ति। बस्तर की समृद्ध लोक कला का प्रतीक।",
                    category = "Metalcraft",
                    craftType = "Bastar Dhokra Art",
                    region = "Kondagaon, Bastar, Chhattisgarh",
                    rawMaterialCost = 350.0,
                    laborHours = 14.0,
                    hourlyWageRate = 160.0,
                    packagingCost = 80.0,
                    fairMinPrice = 2670.0,
                    fairMaxPrice = 3400.0,
                    retailPrice = 2899.0,
                    wholesalePrice = 1650.0,
                    minOrderQuantity = 10,
                    stockAvailable = 22,
                    imageUri = "sample_dhokra",
                    imageStyleFilter = "Antique Metal",
                    voiceTranscript = "यह बस्तर का पारंपरिक ढोकरा शिल्प है। मोम के धागे बनाकर साँचा तैयार किया जाता है और पीतल की ढलाई की जाती है।",
                    originalLanguage = "Hindi",
                    isGiTagged = true,
                    tags = "Dhokra, Brass Art, Bastar Tribal, Lost Wax, Antique Finish, Metalcraft",
                    materialsUsed = "Solid brass alloy, beeswax mold residue, antique patina",
                    dimensions = "18cm H x 15cm L x 6cm W",
                    weightKg = 0.85,
                    careInstructions = "Clean with soft brass cleaner or lemon oil. Avoid chemical abrasives.",
                    artisanName = "Sukhdev Baghel",
                    artisanLocation = "Kondagaon, Bastar, CG",
                    status = "PUBLISHED",
                    viewCount = 95,
                    inquiryCount = 2
                ),
                ProductEntity(
                    id = 4,
                    title = "Authentic Madhubani Hand-Painted Tree of Life Silk Canvas",
                    regionalTitle = "मधुबनी हस्तचित्रित 'जीवन वृक्ष' कैनवास",
                    description = "Original Mithila folk artwork rendered using fine bamboo nibs and natural vegetable dyes (indigo, turmeric, madder). Depicts the auspicious Tree of Life harboring songbirds and deer, symbolizing harmony.",
                    regionalDescription = "प्राकृतिक रंगों और बाँस की कलम से कपड़े पर बनाई गई पारंपरिक मिथिला पेंटिंग। समृद्धि और प्रकृति का प्रतीक।",
                    category = "Paintings & Folk Art",
                    craftType = "Mithila / Madhubani Painting",
                    region = "Ranti Village, Madhubani, Bihar",
                    rawMaterialCost = 400.0,
                    laborHours = 22.0,
                    hourlyWageRate = 175.0,
                    packagingCost = 90.0,
                    fairMinPrice = 4340.0,
                    fairMaxPrice = 5800.0,
                    retailPrice = 4650.0,
                    wholesalePrice = 2900.0,
                    minOrderQuantity = 3,
                    stockAvailable = 8,
                    imageUri = "sample_madhubani",
                    imageStyleFilter = "Vibrant Pigments",
                    voiceTranscript = "यह पेंटिंग प्राकृतिक रंगों जैसे हल्दी, नीम और नील से बनाई गई है। हर एक रेखा हाथ से बाँस की सींक से खींची गई है।",
                    originalLanguage = "Hindi",
                    isGiTagged = true,
                    tags = "Madhubani, Mithila Art, Folk Painting, Vegetable Dyes, Tree of Life, Bihar GI",
                    materialsUsed = "Tussar silk fabric base, organic plant & mineral pigments",
                    dimensions = "60cm x 45cm",
                    weightKg = 0.25,
                    careInstructions = "Frame under UV-protective glass. Keep away from direct moisture.",
                    artisanName = "Devaki Devi",
                    artisanLocation = "Ranti, Madhubani, Bihar",
                    status = "PUBLISHED",
                    viewCount = 180,
                    inquiryCount = 5
                ),
                ProductEntity(
                    id = 5,
                    title = "Hand-Carved Sheesham Floral Wooden Keepsake Box",
                    regionalTitle = "सहारनपुर नक्काशीदार शीशम लकड़ी का बक्सा",
                    description = "Hand-carved by master wood artisans using seasoned Indian Sheesham (Rosewood) with intricate Mughal Jaali lattice cutouts and polished brass floral inlay work.",
                    regionalDescription = "सहारनपुर की प्रसिद्ध शीशम की लकड़ी पर बारीक हाथ की नक्काशी और पीतल के तारों की जड़ाई से बना सुंदर संदूक।",
                    category = "Woodcraft",
                    craftType = "Saharanpur Wood Carving",
                    region = "Saharanpur, Uttar Pradesh",
                    rawMaterialCost = 320.0,
                    laborHours = 10.0,
                    hourlyWageRate = 160.0,
                    packagingCost = 70.0,
                    fairMinPrice = 1990.0,
                    fairMaxPrice = 2800.0,
                    retailPrice = 2399.0,
                    wholesalePrice = 1350.0,
                    minOrderQuantity = 12,
                    stockAvailable = 30,
                    imageUri = "sample_woodcraft",
                    imageStyleFilter = "Warm Timber",
                    voiceTranscript = "यह सहारनपुर की शीशम की लकड़ी से बना है, जिस पर पीतल की बारीक नक्काशी और हाथ का काम है।",
                    originalLanguage = "Hindi",
                    isGiTagged = true,
                    tags = "Woodcraft, Sheesham, Hand Carved, Brass Inlay, Keepsake Box, Home Decor",
                    materialsUsed = "Seasoned Sheesham Rosewood, Solid Brass wire inlay, Natural Lacquer",
                    dimensions = "20cm x 15cm x 10cm",
                    weightKg = 0.9,
                    careInstructions = "Wipe with beeswax polish once in 6 months. Keep away from direct water.",
                    artisanName = "Irfan Ahmed",
                    artisanLocation = "Khatakheri, Saharanpur, UP",
                    status = "PUBLISHED",
                    viewCount = 110,
                    inquiryCount = 3
                ),
                ProductEntity(
                    id = 6,
                    title = "Cuttack Tarakasi Silver Filigree Peacock Pendant Set",
                    regionalTitle = "कटक तारकशी शुद्ध चांदी मोर पेंडेंट सेट",
                    description = "Delicately handcrafted using 500-year-old Cuttack Tarakasi technique. Drawn fine 92.5 pure silver wires are twisted and soldered into a magnificent dancing peacock motif with matching jhumkas.",
                    regionalDescription = "कटक की 500 वर्ष पुरानी तारकशी कला द्वारा शुद्ध चांदी के महीन तारों से गढ़ा गया मोर पेंडेंट व झुमकी सेट।",
                    category = "Jewellery",
                    craftType = "Cuttack Silver Filigree",
                    region = "Cuttack, Odisha",
                    rawMaterialCost = 1800.0,
                    laborHours = 18.0,
                    hourlyWageRate = 220.0,
                    packagingCost = 120.0,
                    fairMinPrice = 5880.0,
                    fairMaxPrice = 7800.0,
                    retailPrice = 6499.0,
                    wholesalePrice = 4200.0,
                    minOrderQuantity = 5,
                    stockAvailable = 15,
                    imageUri = "sample_filigree",
                    imageStyleFilter = "Silver Luster",
                    voiceTranscript = "कटक की तारकशी कला में चांदी के बारीक तार बनाकर मोर डिजाइन गढ़ी गई है। शुद्ध 92.5 चांदी है।",
                    originalLanguage = "Odia",
                    isGiTagged = true,
                    tags = "Jewellery, Silver Filigree, Tarakasi, Odisha GI, Peacock, Handmade Jewelry",
                    materialsUsed = "92.5% Sterling Silver wire, Rhodium plating",
                    dimensions = "Pendant 5cm x 3cm, Earrings 4cm",
                    weightKg = 0.045,
                    careInstructions = "Store in airtight plastic pouch with anti-tarnish strip.",
                    artisanName = "Pradeep Sahoo",
                    artisanLocation = "Nayasarak, Cuttack, Odisha",
                    status = "PUBLISHED",
                    viewCount = 220,
                    inquiryCount = 6
                ),
                ProductEntity(
                    id = 7,
                    title = "Jaipur Cobalt Blue Pottery Floral Decorative Wall Plate",
                    regionalTitle = "जयपुर ब्लू पॉटरी हस्तनिर्मित सजावटी वॉल प्लेट",
                    description = "Authentic GI-tagged Jaipur Blue Pottery made without clay using ground quartz stone, Fuller's earth, and natural gum. Hand-painted in vibrant Persian cobalt and turquoise floral arabesques.",
                    regionalDescription = "बिना मिट्टी के क्वार्ट्ज पत्थर से बनी पारंपरिक जयपुर ब्लू पॉटरी प्लेट। कोबाल्ट नीले रंगों में हाथ से चित्रित।",
                    category = "Home Decor",
                    craftType = "Jaipur Blue Pottery",
                    region = "Jaipur, Rajasthan",
                    rawMaterialCost = 280.0,
                    laborHours = 8.0,
                    hourlyWageRate = 170.0,
                    packagingCost = 90.0,
                    fairMinPrice = 1730.0,
                    fairMaxPrice = 2400.0,
                    retailPrice = 1999.0,
                    wholesalePrice = 1100.0,
                    minOrderQuantity = 10,
                    stockAvailable = 28,
                    imageUri = "sample_bluepottery",
                    imageStyleFilter = "Turquoise Glaze",
                    voiceTranscript = "यह पारंपरिक जयपुर ब्लू पॉटरी की वॉल प्लेट है, जो क्वार्ट्ज पत्थर और कांच के पाउडर से तैयार की जाती है।",
                    originalLanguage = "Hindi",
                    isGiTagged = true,
                    tags = "Home Decor, Blue Pottery, Jaipur Craft, Wall Plate, Persian Motif, Quartz",
                    materialsUsed = "Quartz stone powder, recycled glass, natural gum, cobalt oxide",
                    dimensions = "25cm Diameter x 3cm Depth",
                    weightKg = 0.75,
                    careInstructions = "Fragile ceramic. Clean with damp microfiber cloth.",
                    artisanName = "Gopal Lal Kumhar",
                    artisanLocation = "Kot Jewar, Jaipur, Rajasthan",
                    status = "PUBLISHED",
                    viewCount = 165,
                    inquiryCount = 4
                ),
                ProductEntity(
                    id = 8,
                    title = "Hand-Spun Khadi Cotton Indigo Block Print Table Runner",
                    regionalTitle = "हाथ से काता हुआ खादी सूती ब्लॉक प्रिंट रनर",
                    description = "Spun on traditional charkha looms and hand block printed in Bagru using natural fermented indigo dye and hand-carved teakwood blocks. Chemical-free and sustainable.",
                    regionalDescription = "चरखे पर काते सूती धागे और बगरू के प्राकृतिक नील रंग से ब्लॉक प्रिंट किया हुआ पर्यावरण-अनुकूल टेबल रनर।",
                    category = "Textiles",
                    craftType = "Bagru Hand Block Printing",
                    region = "Bagru, Rajasthan",
                    rawMaterialCost = 220.0,
                    laborHours = 7.0,
                    hourlyWageRate = 150.0,
                    packagingCost = 50.0,
                    fairMinPrice = 1320.0,
                    fairMaxPrice = 1850.0,
                    retailPrice = 1499.0,
                    wholesalePrice = 850.0,
                    minOrderQuantity = 20,
                    stockAvailable = 50,
                    imageUri = "sample_textiles",
                    imageStyleFilter = "Indigo Loom",
                    voiceTranscript = "हाथ से काते खादी कपड़े पर बगरू के प्राकृतिक रंगों से हाथ के ठप्पों (ब्लॉक) द्वारा छपाई की गई है।",
                    originalLanguage = "Hindi",
                    isGiTagged = true,
                    tags = "Textiles, Khadi, Block Print, Bagru, Indigo, Eco-friendly, Home Living",
                    materialsUsed = "100% Organic Handspun Cotton Khadi, Natural Indigo",
                    dimensions = "180cm L x 35cm W",
                    weightKg = 0.3,
                    careInstructions = "Gentle hand wash in cold water with mild detergent.",
                    artisanName = "Kailash Chhipa",
                    artisanLocation = "Bagru, Jaipur, Rajasthan",
                    status = "PUBLISHED",
                    viewCount = 135,
                    inquiryCount = 3
                )
            )

            sampleProducts.forEach { productDao.insertProduct(it) }

            // Seed sample B2B buyer inquiries
            val sampleInquiries = listOf(
                BuyerInquiryEntity(
                    id = 1,
                    productId = 1,
                    productTitle = "Handcrafted Earthen Terracotta Decorative Vase",
                    buyerName = "Ananya Singhania",
                    buyerCompany = "FabHeritage Lifestyle Boutiques",
                    buyerType = "Wholesale Boutique",
                    requestedQuantity = 50,
                    offeredPricePerUnit = 850.0,
                    message = "Namaste! We are a chain of 14 lifestyle stores in Delhi NCR & Mumbai. We love your terracotta vase designs for our upcoming Diwali festive collection. Can you supply 50 units by next month?",
                    buyerPhone = "+91 98112 34567",
                    buyerEmail = "sourcing@fabheritage.in",
                    buyerCity = "New Delhi, Delhi",
                    requiredDate = "15 Sep 2026",
                    status = "NEW"
                ),
                BuyerInquiryEntity(
                    id = 2,
                    productId = 2,
                    productTitle = "Royal Crimson & Gold Pure Katan Silk Banarasi Dupatta",
                    buyerName = "Rohit Mehta",
                    buyerCompany = "IndiCrafts Global Exports Ltd.",
                    buyerType = "Exporter",
                    requestedQuantity = 20,
                    offeredPricePerUnit = 8800.0,
                    message = "We export authentic GI-tagged handlooms to retail partners in London and Dubai. Looking for 20 pieces with Silk Mark and GI certification documentation. Please share delivery timeline.",
                    buyerPhone = "+91 98200 67890",
                    buyerEmail = "rohit@indicrafts.com",
                    buyerCity = "Mumbai, Maharashtra",
                    requiredDate = "30 Sep 2026",
                    status = "RESPONDED",
                    artisanResponse = "नमस्ते रोहित जी! हम सिल्क मार्क और जीआई सर्टिफिकेशन के साथ 20 पीस 25 सितम्बर तक तैयार कर सकते हैं।"
                ),
                BuyerInquiryEntity(
                    id = 3,
                    productId = 3,
                    productTitle = "Tribal Lost-Wax Cast Brass Dhokra Horse Figurine",
                    buyerName = "Pooja Hegde",
                    buyerCompany = "Kalakriti Corporate Gifting Solutions",
                    buyerType = "Corporate Gifting",
                    requestedQuantity = 100,
                    offeredPricePerUnit = 1650.0,
                    message = "We require 100 handcrafted Dhokra figurines for an annual corporate summit gift hamper. Custom wooden box packaging needed. Can we discuss wholesale pricing and lead time?",
                    buyerPhone = "+91 97400 12345",
                    buyerEmail = "pooja@kalakritigifts.com",
                    buyerCity = "Bengaluru, Karnataka",
                    requiredDate = "10 Oct 2026",
                    status = "ACCEPTED"
                )
            )

            sampleInquiries.forEach { inquiryDao.insertInquiry(it) }

            // Seed initial activity events to demonstrate offline tracking and sync
            eventDao?.let { dao ->
                val sampleEvents = listOf(
                    com.example.data.model.UserActivityEventEntity(
                        id = 1,
                        eventType = "PRODUCT_CREATED",
                        title = "शिल्प दर्ज किया गया (Product Created)",
                        description = "Handcrafted Earthen Terracotta Decorative Vase • Gorakhpur Terracotta",
                        category = "CATALOG",
                        timestamp = System.currentTimeMillis() - 86400000L * 2,
                        isSynced = true,
                        syncedAt = System.currentTimeMillis() - 86400000L * 2
                    ),
                    com.example.data.model.UserActivityEventEntity(
                        id = 2,
                        eventType = "INQUIRY_RESPONDED",
                        title = "पूछताछ का उत्तर दिया (Inquiry Responded)",
                        description = "Responded to Rohit Mehta (IndiCrafts Global Exports) for 20 Banarasi Dupattas",
                        category = "INQUIRY",
                        timestamp = System.currentTimeMillis() - 86400000L,
                        isSynced = true,
                        syncedAt = System.currentTimeMillis() - 86400000L
                    ),
                    com.example.data.model.UserActivityEventEntity(
                        id = 3,
                        eventType = "PRICING_CALCULATED",
                        title = "उचित मूल्य गणना की गई (Fair Pricing Computed)",
                        description = "Dynamic fair pricing computed for Royal Crimson Pure Katan Silk Dupatta (Retail ₹13,500)",
                        category = "PRICING",
                        timestamp = System.currentTimeMillis() - 3600000L * 5,
                        isSynced = false,
                        syncedAt = null
                    )
                )
                sampleEvents.forEach { dao.insertEvent(it) }
            }

            // Seed historical product sales and material cost records to power AI Dynamic Pricing
            historyDao?.let { dao ->
                val sampleHistories = listOf(
                    // Product 1: Terracotta Vase - shows material cost inflation and festive surge
                    ProductHistoryEntity(
                        id = 1,
                        productId = 1,
                        productTitle = "Handcrafted Earthen Terracotta Decorative Vase",
                        category = "Pottery",
                        recordedDate = System.currentTimeMillis() - 86400000L * 300, // ~10 months ago
                        salePeriod = "Diwali Festive 2025",
                        materialCost = 100.0,
                        laborHours = 6.0,
                        hourlyWageRate = 160.0,
                        packagingCost = 50.0,
                        sellingPrice = 1650.0,
                        suggestedRetailPrice = 1699.0,
                        unitsSold = 38,
                        unitsInStock = 5,
                        salesChannel = "Direct Craft Fair",
                        demandIndex = 1.45,
                        competitorMarketAverage = 1800.0,
                        profitMarginPercent = 38.5,
                        notes = "Diwali festive exhibition peak sales. High buyer demand for authentic Gangetic clay."
                    ),
                    ProductHistoryEntity(
                        id = 2,
                        productId = 1,
                        productTitle = "Handcrafted Earthen Terracotta Decorative Vase",
                        category = "Pottery",
                        recordedDate = System.currentTimeMillis() - 86400000L * 180, // ~6 months ago
                        salePeriod = "Spring Craft Haat 2026",
                        materialCost = 120.0,
                        laborHours = 6.5,
                        hourlyWageRate = 175.0,
                        packagingCost = 55.0,
                        sellingPrice = 1450.0,
                        suggestedRetailPrice = 1499.0,
                        unitsSold = 22,
                        unitsInStock = 15,
                        salesChannel = "E-Commerce Buyer",
                        demandIndex = 1.05,
                        competitorMarketAverage = 1600.0,
                        profitMarginPercent = 27.8,
                        notes = "Moderate post-spring demand. Clay and firewood transport costs increased 20%."
                    ),
                    ProductHistoryEntity(
                        id = 3,
                        productId = 1,
                        productTitle = "Handcrafted Earthen Terracotta Decorative Vase",
                        category = "Pottery",
                        recordedDate = System.currentTimeMillis() - 86400000L * 45, // ~1.5 months ago
                        salePeriod = "Monsoon Low Demand 2026",
                        materialCost = 140.0,
                        laborHours = 6.5,
                        hourlyWageRate = 180.0,
                        packagingCost = 60.0,
                        sellingPrice = 1350.0,
                        suggestedRetailPrice = 1399.0,
                        unitsSold = 14,
                        unitsInStock = 25,
                        salesChannel = "B2B Wholesale",
                        demandIndex = 0.85,
                        competitorMarketAverage = 1450.0,
                        profitMarginPercent = 16.5,
                        notes = "Off-season monsoon humidity slowed drying. Raw material and fuel surged to ₹140."
                    ),

                    // Product 2: Banarasi Silk Dupatta - shows pure silk yarn price rise and wedding season surge
                    ProductHistoryEntity(
                        id = 4,
                        productId = 2,
                        productTitle = "Royal Crimson & Gold Pure Katan Silk Banarasi Dupatta",
                        category = "Handloom & Textiles",
                        recordedDate = System.currentTimeMillis() - 86400000L * 240, // ~8 months ago
                        salePeriod = "Winter Wedding Peak 2025",
                        materialCost = 2350.0,
                        laborHours = 45.0,
                        hourlyWageRate = 180.0,
                        packagingCost = 120.0,
                        sellingPrice = 14800.0,
                        suggestedRetailPrice = 15000.0,
                        unitsSold = 16,
                        unitsInStock = 3,
                        salesChannel = "Wholesale Boutique",
                        demandIndex = 1.55,
                        competitorMarketAverage = 16500.0,
                        profitMarginPercent = 32.4,
                        notes = "Wedding bridal season surge. High willingness to pay for authentic handloom Silk Mark."
                    ),
                    ProductHistoryEntity(
                        id = 5,
                        productId = 2,
                        productTitle = "Royal Crimson & Gold Pure Katan Silk Banarasi Dupatta",
                        category = "Handloom & Textiles",
                        recordedDate = System.currentTimeMillis() - 86400000L * 120, // ~4 months ago
                        salePeriod = "Summer Season 2026",
                        materialCost = 2600.0,
                        laborHours = 48.0,
                        hourlyWageRate = 190.0,
                        packagingCost = 130.0,
                        sellingPrice = 12800.0,
                        suggestedRetailPrice = 13200.0,
                        unitsSold = 9,
                        unitsInStock = 8,
                        salesChannel = "Direct Craft Fair",
                        demandIndex = 0.90,
                        competitorMarketAverage = 14000.0,
                        profitMarginPercent = 19.8,
                        notes = "Mulberry silk yarn prices escalated in Varanasi yarn markets. Slower summer bridal sales."
                    ),
                    ProductHistoryEntity(
                        id = 6,
                        productId = 2,
                        productTitle = "Royal Crimson & Gold Pure Katan Silk Banarasi Dupatta",
                        category = "Handloom & Textiles",
                        recordedDate = System.currentTimeMillis() - 86400000L * 20,
                        salePeriod = "Upcoming Festive Pre-Orders 2026",
                        materialCost = 2800.0,
                        laborHours = 48.0,
                        hourlyWageRate = 200.0,
                        packagingCost = 150.0,
                        sellingPrice = 13500.0,
                        suggestedRetailPrice = 14200.0,
                        unitsSold = 12,
                        unitsInStock = 12,
                        salesChannel = "Exporter",
                        demandIndex = 1.20,
                        competitorMarketAverage = 15500.0,
                        profitMarginPercent = 23.6,
                        notes = "Zari gold electroplate raw cost peaked at ₹2,800. Export order inquiries rising."
                    ),

                    // Product 3: Dhokra Brass Figurine - raw metal market fluctuation
                    ProductHistoryEntity(
                        id = 7,
                        productId = 3,
                        productTitle = "Tribal Lost-Wax Cast Brass Dhokra Horse Figurine",
                        category = "Metalcraft",
                        recordedDate = System.currentTimeMillis() - 86400000L * 200,
                        salePeriod = "National Tribal Expo 2025",
                        materialCost = 270.0,
                        laborHours = 13.0,
                        hourlyWageRate = 150.0,
                        packagingCost = 60.0,
                        sellingPrice = 2450.0,
                        suggestedRetailPrice = 2500.0,
                        unitsSold = 28,
                        unitsInStock = 6,
                        salesChannel = "Direct Craft Fair",
                        demandIndex = 1.30,
                        competitorMarketAverage = 2700.0,
                        profitMarginPercent = 35.2,
                        notes = "Tribal handicraft expo in Delhi. Excellent turnover at ₹2,450."
                    ),
                    ProductHistoryEntity(
                        id = 8,
                        productId = 3,
                        productTitle = "Tribal Lost-Wax Cast Brass Dhokra Horse Figurine",
                        category = "Metalcraft",
                        recordedDate = System.currentTimeMillis() - 86400000L * 50,
                        salePeriod = "B2B Corporate Gifting 2026",
                        materialCost = 350.0,
                        laborHours = 14.0,
                        hourlyWageRate = 160.0,
                        packagingCost = 80.0,
                        sellingPrice = 2899.0,
                        suggestedRetailPrice = 2999.0,
                        unitsSold = 22,
                        unitsInStock = 22,
                        salesChannel = "Corporate Gifting",
                        demandIndex = 1.15,
                        competitorMarketAverage = 3200.0,
                        profitMarginPercent = 29.5,
                        notes = "Brass metal scrap rose 30%. Corporate orders accepted at ₹1,650 wholesale / ₹2,899 retail."
                    ),

                    // Product 4: Madhubani Canvas
                    ProductHistoryEntity(
                        id = 9,
                        productId = 4,
                        productTitle = "Authentic Madhubani Hand-Painted Tree of Life Silk Canvas",
                        category = "Paintings & Folk Art",
                        recordedDate = System.currentTimeMillis() - 86400000L * 150,
                        salePeriod = "Folk Art Gallery Exhibition 2026",
                        materialCost = 330.0,
                        laborHours = 20.0,
                        hourlyWageRate = 165.0,
                        packagingCost = 70.0,
                        sellingPrice = 4200.0,
                        suggestedRetailPrice = 4500.0,
                        unitsSold = 10,
                        unitsInStock = 4,
                        salesChannel = "Boutique Gallery",
                        demandIndex = 1.25,
                        competitorMarketAverage = 4800.0,
                        profitMarginPercent = 31.8,
                        notes = "High appreciation for natural plant pigments (indigo and turmeric)."
                    ),
                    ProductHistoryEntity(
                        id = 10,
                        productId = 4,
                        productTitle = "Authentic Madhubani Hand-Painted Tree of Life Silk Canvas",
                        category = "Paintings & Folk Art",
                        recordedDate = System.currentTimeMillis() - 86400000L * 30,
                        salePeriod = "Heritage Lifestyle Showcase 2026",
                        materialCost = 400.0,
                        laborHours = 22.0,
                        hourlyWageRate = 175.0,
                        packagingCost = 90.0,
                        sellingPrice = 4650.0,
                        suggestedRetailPrice = 4800.0,
                        unitsSold = 8,
                        unitsInStock = 8,
                        salesChannel = "E-Commerce Buyer",
                        demandIndex = 1.10,
                        competitorMarketAverage = 5200.0,
                        profitMarginPercent = 27.4,
                        notes = "Tussar silk fabric base price rose to ₹400. Solid artisan profit retained."
                    )
                )

                sampleHistories.forEach { dao.insertHistory(it) }
            }
        }
    }
}
