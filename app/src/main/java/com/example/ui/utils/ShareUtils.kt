package com.example.ui.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.data.model.ProductEntity

object ShareUtils {
    fun shareProductToWhatsApp(context: Context, product: ProductEntity) {
        val shareMessage = """
            ✨ Check out this handcrafted item! ✨
            
            🏷️ *${product.title}*
            ${if (product.regionalTitle.isNotBlank()) "(${product.regionalTitle})" else ""}
            
            💰 *Retail Price:* ₹${product.retailPrice.toInt()}
            ${if (product.wholesalePrice > 0) "📦 *B2B Wholesale:* ₹${product.wholesalePrice.toInt()} (MOQ: ${product.minOrderQuantity})" else ""}
            
            👨🏽‍🎨 *Artisan:* ${product.artisanName}
            📍 *Location:* ${product.artisanLocation}
            
            ${product.description.take(150)}${if (product.description.length > 150) "..." else ""}
            
            View more details and place an order directly with the artisan on KAUSHVANI!
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            // Attempt to target WhatsApp explicitly
            setPackage("com.whatsapp")
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // WhatsApp is not installed, fallback to generic share chooser
            val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareMessage)
            }
            try {
                context.startActivity(Intent.createChooser(fallbackIntent, "Share Product via"))
            } catch (e2: Exception) {
                Toast.makeText(context, "No app found to share the product", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
