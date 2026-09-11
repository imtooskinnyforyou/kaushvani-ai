package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.ProductEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.math.sqrt

data class SemanticSearchResult(
    val product: ProductEntity,
    val score: Float, // 0.0f to 1.0f
    val matchExplanation: String,
    val isSemanticMatch: Boolean
)

object SemanticSearchEngine {
    private const val TAG = "SemanticSearchEngine"
    private const val EMBEDDING_MODEL = "gemini-embedding-2-preview"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    // In-memory cache for computed product embeddings to minimize API latency
    private val productEmbeddingCache = ConcurrentHashMap<Long, FloatArray>()
    private val queryEmbeddingCache = ConcurrentHashMap<String, FloatArray>()

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Performs semantic vector search over a list of products using Gemini embeddings
     * with an offline semantic fallback.
     */
    suspend fun search(
        query: String,
        products: List<ProductEntity>,
        selectedCategory: String = "All",
        minPrice: Double = 0.0,
        maxPrice: Double = Double.MAX_VALUE,
        selectedMaterial: String = "All",
        selectedLocation: String = "All",
        selectedCraftType: String = "All"
    ): List<SemanticSearchResult> = withContext(Dispatchers.Default) {
        if (products.isEmpty()) return@withContext emptyList()

        // 1. Apply Hard Metadata Filters first
        val filterFiltered = products.filter { product ->
            val matchesCategory = when (selectedCategory) {
                "All" -> true
                "Textiles" -> product.category.contains("Textile", ignoreCase = true) || product.category.contains("Handloom", ignoreCase = true) || product.category.contains("Silk", ignoreCase = true)
                "Handloom" -> product.category.contains("Handloom", ignoreCase = true) || product.craftType.contains("Weav", ignoreCase = true)
                "Pottery" -> product.category.contains("Pottery", ignoreCase = true) || product.craftType.contains("Terracotta", ignoreCase = true)
                "Woodcraft" -> product.category.contains("Wood", ignoreCase = true) || product.materialsUsed.contains("Wood", ignoreCase = true)
                "Metalcraft" -> product.category.contains("Metal", ignoreCase = true) || product.craftType.contains("Dhokra", ignoreCase = true) || product.materialsUsed.contains("Brass", ignoreCase = true)
                "Jewellery" -> product.category.contains("Jewel", ignoreCase = true) || product.craftType.contains("Filigree", ignoreCase = true) || product.category.contains("Jewelry", ignoreCase = true)
                "Home Decor" -> product.category.contains("Decor", ignoreCase = true) || product.tags.contains("Decor", ignoreCase = true) || product.category.contains("Pottery", ignoreCase = true) || product.category.contains("Painting", ignoreCase = true)
                "Other" -> !listOf("Textiles", "Handloom", "Pottery", "Woodcraft", "Metalcraft", "Jewellery", "Home Decor").any { product.category.contains(it, ignoreCase = true) }
                else -> product.category.equals(selectedCategory, ignoreCase = true)
            }

            val matchesPrice = product.wholesalePrice in minPrice..maxPrice || product.retailPrice in minPrice..maxPrice
            val matchesMaterial = selectedMaterial == "All" || product.materialsUsed.contains(selectedMaterial, ignoreCase = true) || product.tags.contains(selectedMaterial, ignoreCase = true)
            val matchesLocation = selectedLocation == "All" || product.region.contains(selectedLocation, ignoreCase = true) || product.artisanLocation.contains(selectedLocation, ignoreCase = true)
            val matchesCraftType = selectedCraftType == "All" || product.craftType.contains(selectedCraftType, ignoreCase = true) || product.tags.contains(selectedCraftType, ignoreCase = true)

            matchesCategory && matchesPrice && matchesMaterial && matchesLocation && matchesCraftType
        }

        if (query.isBlank()) {
            return@withContext filterFiltered.map {
                SemanticSearchResult(it, 1.0f, "Matched active filters", false)
            }
        }

        // 2. Perform Vector Embedding / Hybrid Semantic Search
        val apiKey = getApiKey()
        var scoredResults: List<SemanticSearchResult>? = null

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val queryVector = getOrFetchQueryEmbedding(query, apiKey)
                if (queryVector != null) {
                    scoredResults = filterFiltered.map { product ->
                        val prodVector = getOrFetchProductEmbedding(product, apiKey)
                        val vectorScore = if (prodVector != null) {
                            cosineSimilarity(queryVector, prodVector)
                        } else {
                            0f
                        }

                        // Local heuristic semantic boost
                        val domainScore = calculateDomainSemanticScore(query, product)
                        val finalScore = (vectorScore * 0.65f) + (domainScore.first * 0.35f)

                        SemanticSearchResult(
                            product = product,
                            score = finalScore.coerceIn(0f, 1f),
                            matchExplanation = domainScore.second.ifBlank { "Semantic vector similarity (${(finalScore * 100).toInt()}%)" },
                            isSemanticMatch = finalScore > 0.45f
                        )
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini vector embedding search fallback: ${e.message}")
            }
        }

        // 3. Fallback to Deep Craft Knowledge Graph Semantic Engine if API is unavailable or offline
        if (scoredResults == null) {
            scoredResults = filterFiltered.map { product ->
                val (score, explanation) = calculateDomainSemanticScore(query, product)
                SemanticSearchResult(
                    product = product,
                    score = score,
                    matchExplanation = explanation,
                    isSemanticMatch = score > 0.40f
                )
            }
        }

        // Filter and sort by semantic relevance
        scoredResults
            .filter { it.score > 0.15f }
            .sortedByDescending { it.score }
    }

    private suspend fun getOrFetchQueryEmbedding(query: String, apiKey: String): FloatArray? {
        val cached = queryEmbeddingCache[query.lowercase().trim()]
        if (cached != null) return cached

        val embedding = fetchEmbeddingFromGemini(query, apiKey)
        if (embedding != null) {
            queryEmbeddingCache[query.lowercase().trim()] = embedding
        }
        return embedding
    }

    private suspend fun getOrFetchProductEmbedding(product: ProductEntity, apiKey: String): FloatArray? {
        val cached = productEmbeddingCache[product.id]
        if (cached != null) return cached

        val textToEmbed = buildString {
            append(product.title)
            append(". ")
            append(product.regionalTitle)
            append(". Category: ")
            append(product.category)
            append(". Craft Type: ")
            append(product.craftType)
            append(". Materials: ")
            append(product.materialsUsed)
            append(". Region: ")
            append(product.region)
            append(". Tags: ")
            append(product.tags)
            append(". Description: ")
            append(product.description)
        }

        val embedding = fetchEmbeddingFromGemini(textToEmbed, apiKey)
        if (embedding != null) {
            productEmbeddingCache[product.id] = embedding
        }
        return embedding
    }

    private suspend fun fetchEmbeddingFromGemini(text: String, apiKey: String): FloatArray? = withContext(Dispatchers.IO) {
        try {
            val url = "${BASE_URL}${EMBEDDING_MODEL}:embedContent?key=$apiKey"
            val requestJson = JSONObject().apply {
                put("model", "models/$EMBEDDING_MODEL")
                put("content", JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", text.take(1500))))
                })
            }

            val body = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(body).build()
            val response = httpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext null
            }

            val responseStr = response.body?.string() ?: return@withContext null
            val obj = JSONObject(responseStr)
            val embeddingObj = obj.optJSONObject("embedding") ?: return@withContext null
            val valuesArray = embeddingObj.optJSONArray("values") ?: return@withContext null

            val floatArray = FloatArray(valuesArray.length())
            for (i in 0 until valuesArray.length()) {
                floatArray[i] = valuesArray.getDouble(i).toFloat()
            }
            floatArray
        } catch (e: Exception) {
            Log.e(TAG, "Embedding error: ${e.message}")
            null
        }
    }

    private fun cosineSimilarity(vecA: FloatArray, vecB: FloatArray): Float {
        if (vecA.size != vecB.size || vecA.isEmpty()) return 0f
        var dotProduct = 0.0f
        var normA = 0.0f
        var normB = 0.0f
        for (i in vecA.indices) {
            dotProduct += vecA[i] * vecB[i]
            normA += vecA[i] * vecA[i]
            normB += vecB[i] * vecB[i]
        }
        if (normA <= 0.0f || normB <= 0.0f) return 0f
        return (dotProduct / (sqrt(normA) * sqrt(normB))).coerceIn(0f, 1f)
    }

    /**
     * Domain Semantic Ontology & Taxonomy matcher for Indian Handicrafts & Handlooms.
     * Evaluates concept clusters like:
     * - "traditional red handwoven saree" -> matches "Royal Crimson & Gold Pure Katan Silk Banarasi Dupatta"
     */
    private fun calculateDomainSemanticScore(query: String, product: ProductEntity): Pair<Float, String> {
        val q = query.lowercase().trim()
        val tokens = q.split(Regex("[\\s,;+]+")).filter { it.isNotBlank() }

        val searchableDoc = buildString {
            append(product.title.lowercase())
            append(" ")
            append(product.regionalTitle.lowercase())
            append(" ")
            append(product.category.lowercase())
            append(" ")
            append(product.craftType.lowercase())
            append(" ")
            append(product.materialsUsed.lowercase())
            append(" ")
            append(product.region.lowercase())
            append(" ")
            append(product.tags.lowercase())
            append(" ")
            append(product.description.lowercase())
        }

        var totalWeight = 0f
        var matchedWeight = 0f
        val matchReasons = mutableListOf<String>()

        for (token in tokens) {
            val weight = 1.0f
            totalWeight += weight

            // 1. Direct lexical match
            if (searchableDoc.contains(token)) {
                matchedWeight += weight
                matchReasons.add("Direct match '$token'")
                continue
            }

            // 2. Semantic Cluster Expansions
            val semanticCluster = findSemanticCluster(token)
            var clusterMatched = false
            for (synonym in semanticCluster) {
                if (searchableDoc.contains(synonym)) {
                    matchedWeight += (weight * 0.92f)
                    matchReasons.add("Semantic match: '$token' ~ '$synonym'")
                    clusterMatched = true
                    break
                }
            }

            if (!clusterMatched) {
                // Partial prefix or soundex match
                if (token.length >= 4 && searchableDoc.contains(token.take(4))) {
                    matchedWeight += (weight * 0.4f)
                }
            }
        }

        val baseScore = if (totalWeight > 0) (matchedWeight / totalWeight) else 0f

        // Boost if craft technique or GI Tag reinforces the query
        var boost = 0f
        if (product.isGiTagged && (q.contains("traditional") || q.contains("authentic") || q.contains("pure") || q.contains("heritage"))) {
            boost += 0.15f
        }

        val finalScore = (baseScore + boost).coerceIn(0f, 1f)
        val explanation = matchReasons.take(2).joinToString(", ")

        return Pair(finalScore, explanation)
    }

    /**
     * Semantic thesaurus of Indian handicraft concepts, colors, attire, techniques, and materials.
     */
    private fun findSemanticCluster(token: String): List<String> {
        val t = token.lowercase()
        return when {
            // Colors
            t in listOf("red", "crimson", "maroon", "lal", "scarlet", "ruby", "sindoori", "vermilion") ->
                listOf("crimson", "red", "maroon", "scarlet", "ruby", "zari", "katan", "rose", "vermilion", "terracotta")

            t in listOf("gold", "golden", "zari", "peetal", "brass", "sona", "yellow") ->
                listOf("gold", "zari", "brass", "golden", "kadwa", "yellow", "ochre", "dhokra")

            t in listOf("blue", "neel", "indigo", "azure", "navy") ->
                listOf("indigo", "blue", "neel", "pottery", "jaipur blue", "sapphire")

            t in listOf("green", "hara", "emerald", "olive") ->
                listOf("green", "emerald", "olive", "flora", "plant")

            // Attire & Textiles
            t in listOf("saree", "sari", "dupatta", "drape", "poshak", "pallu", "stole", "shawl", "cloth", "dress") ->
                listOf("dupatta", "saree", "sari", "katan", "silk", "handloom", "zari", "textile", "drape", "kadwa", "chanderi", "tussar", "banarasi")

            t in listOf("handwoven", "handloom", "woven", "loom", "pitloom", "shuttle", "bunkar", "weaver") ->
                listOf("handwoven", "handloom", "pit loom", "silk", "katan", "khadi", "weaving", "cotton", "kadwa", "zari")

            t in listOf("traditional", "heritage", "ethnic", "folk", "ancestral", "authentic", "vintage", "desi", "shilp") ->
                listOf("heritage", "traditional", "centuries-old", "authentic", "tribal", "gi tag", "gi tagged", "folk", "masterfully", "ancient", "handcrafted")

            // Materials
            t in listOf("clay", "mitti", "earthen", "terracotta", "mud", "ceramic", "pottery") ->
                listOf("terracotta", "clay", "alluvium", "earthen", "potter", "pottery", "kiln", "wood-fired")

            t in listOf("brass", "metal", "bronze", "peetal", "copper", "dhokra", "iron") ->
                listOf("brass", "dhokra", "lost-wax", "metal", "alloy", "oxidized", "patina")

            t in listOf("wood", "wooden", "lakdi", "carved", "teak", "sheesham") ->
                listOf("wood", "wooden", "carving", "hand-carved", "timber", "shisham", "teakwood")

            t in listOf("silk", "resham", "katan", "tussar", "mulberry", "muga") ->
                listOf("silk", "katan", "mulberry", "tussar", "zari", "banarasi", "chanderi")

            // Decor & Utility
            t in listOf("vase", "pot", "kalash", "matka", "container", "jar") ->
                listOf("vase", "kalash", "pottery", "vessel", "decorative", "planter")

            t in listOf("statue", "figurine", "idol", "sculpture", "murti", "horse") ->
                listOf("figurine", "sculpture", "horse", "idol", "cast", "statue", "dhokra")

            t in listOf("painting", "canvas", "art", "artwork", "chitra") ->
                listOf("painting", "canvas", "mithila", "madhubani", "folk art", "pigment", "bamboo nib")

            t in listOf("jewellery", "jewelry", "necklace", "earring", "bangle", "ornament", "gahna") ->
                listOf("jewellery", "jewelry", "filigree", "tarkashi", "meenakari", "silver", "beads", "ornament")

            else -> emptyList()
        }
    }
}
