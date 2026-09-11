package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Artisan-first 5 Simple Product Questions Component.
 *
 * Mandate:
 * 1. "What is this product called?" (Voice input + text)
 * 2. "What material did you use?" (Selectable chips + voice)
 * 3. "How much time did it take to make?" (Hours/days selector)
 * 4. "What is the approximate cost of the raw material?" (₹ amount input with presets)
 * 5. "Who is this mainly for?" (Retail / Wholesale / Both)
 *
 * Smart Skip: If information was already obtained from voice or image, marks answered with a checkmark.
 */

data class SimpleQuestionsData(
    val productName: String = "",
    val material: String = "Clay",
    val laborHours: Double = 4.0,
    val rawMaterialCost: Double = 250.0,
    val targetBuyer: String = "Both", // "Retail", "Wholesale", "Both"
    val isNamePreFilled: Boolean = false,
    val isMaterialPreFilled: Boolean = false,
    val isLaborPreFilled: Boolean = false,
    val isCostPreFilled: Boolean = false,
    val isTargetPreFilled: Boolean = false
)

@Composable
fun SimpleProductQuestionsCard(
    data: SimpleQuestionsData,
    onDataChange: (SimpleQuestionsData) -> Unit,
    onStartVoiceForField: (questionNumber: Int) -> Unit,
    isListeningVoice: Boolean = false,
    activeVoiceField: Int = 0,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("simple_questions_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF6F0)),
        border = BorderStroke(1.dp, TerracottaPrimary.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = TerracottaPrimary,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "शिल्प के 5 आसान सवाल (Quick Answers)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextPrimary
                        )
                        Text(
                            text = "बोलकर या बटन दबाकर तुरंत उत्तर दें — कोई कठिन फॉर्म नहीं",
                            fontSize = 11.sp,
                            color = ArtisanTextSecondary
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFE5DCD0), thickness = 1.dp)

            // QUESTION 1: What is this product called?
            QuestionBlock(
                number = 1,
                titleEnglish = "What is this product called?",
                titleHindi = "इस शिल्प का क्या नाम है?",
                isPreFilled = data.isNamePreFilled && data.productName.isNotBlank()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = data.productName,
                        onValueChange = { onDataChange(data.copy(productName = it, isNamePreFilled = false)) },
                        placeholder = { Text("उदा. टेराकोटा नक्काशी फूलदान", fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("question_name_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TerracottaPrimary,
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    FilledIconButton(
                        onClick = { onStartVoiceForField(1) },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (isListeningVoice && activeVoiceField == 1) Color(0xFFDC2626) else TerracottaPrimary
                        ),
                        modifier = Modifier
                            .size(50.dp)
                            .testTag("question_name_mic_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "बोलकर नाम बताएं (Speak Name)",
                            tint = Color.White
                        )
                    }
                }
            }

            // QUESTION 2: What material did you use?
            val materialPresets = listOf(
                Pair("Clay", "मिट्टी / क्ले"),
                Pair("Terracotta", "टेराकोटा"),
                Pair("Silk", "रेशम / सिल्क"),
                Pair("Cotton", "सूती / खादी"),
                Pair("Wood", "लकड़ी"),
                Pair("Brass", "पीतल / धातु"),
                Pair("Jute", "जूट / घास"),
                Pair("Stone", "पत्थर"),
                Pair("Leather", "चमड़ा")
            )

            QuestionBlock(
                number = 2,
                titleEnglish = "What material did you use?",
                titleHindi = "आपने किस सामग्री का उपयोग किया?",
                isPreFilled = data.isMaterialPreFilled && data.material.isNotBlank()
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(materialPresets) { (matEng, matHi) ->
                        val isSelected = data.material.equals(matEng, ignoreCase = true) ||
                                data.material.contains(matHi, ignoreCase = true)
                        Surface(
                            modifier = Modifier
                                .semantics {
                                    role = Role.RadioButton
                                    selected = isSelected
                                    contentDescription = "सामग्री: $matHi ($matEng)"
                                }
                                .clickable {
                                    onDataChange(data.copy(material = matEng, isMaterialPreFilled = false))
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) TerracottaPrimary else Color.White,
                            border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Text(
                                text = matHi,
                                color = if (isSelected) Color.White else ArtisanTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // QUESTION 3: How much time did it take to make?
            val timePresets = listOf(
                Pair(2.0, "2 घंटे (2 hrs)"),
                Pair(4.0, "4 घंटे (Half day)"),
                Pair(8.0, "1 दिन (8 hrs)"),
                Pair(16.0, "2 दिन (16 hrs)"),
                Pair(32.0, "4 दिन (Craft pack)")
            )

            QuestionBlock(
                number = 3,
                titleEnglish = "How much time did it take to make?",
                titleHindi = "इसे बनाने में कितना समय लगा?",
                isPreFilled = data.isLaborPreFilled
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(timePresets) { (hrs, label) ->
                        val isSelected = (data.laborHours == hrs)
                        Surface(
                            modifier = Modifier
                                .semantics {
                                    role = Role.RadioButton
                                    selected = isSelected
                                    contentDescription = label
                                }
                                .clickable {
                                    onDataChange(data.copy(laborHours = hrs, isLaborPreFilled = false))
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) ForestSuccess else Color.White,
                            border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else ArtisanTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // QUESTION 4: Approximate cost of raw material?
            val costPresets = listOf(100.0, 250.0, 500.0, 1000.0, 2000.0)

            QuestionBlock(
                number = 4,
                titleEnglish = "What is the approximate cost of the raw material?",
                titleHindi = "कच्चे माल का अनुमानित खर्च कितना था?",
                isPreFilled = data.isCostPreFilled
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(costPresets) { cost ->
                            val isSelected = (data.rawMaterialCost == cost)
                            Surface(
                                modifier = Modifier
                                    .semantics {
                                        role = Role.RadioButton
                                        selected = isSelected
                                        contentDescription = "लागत ₹${cost.toInt()}"
                                    }
                                    .clickable {
                                        onDataChange(data.copy(rawMaterialCost = cost, isCostPreFilled = false))
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) TerracottaPrimary else Color.White,
                                border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFCBD5E1))
                            ) {
                                Text(
                                    text = "₹${cost.toInt()}",
                                    color = if (isSelected) Color.White else ArtisanTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    // Custom input
                    OutlinedTextField(
                        value = if (data.rawMaterialCost > 0) data.rawMaterialCost.toInt().toString() else "",
                        onValueChange = { str ->
                            val parsed = str.toDoubleOrNull() ?: 0.0
                            onDataChange(data.copy(rawMaterialCost = parsed, isCostPreFilled = false))
                        },
                        prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
                        placeholder = { Text("अन्य राशि लिखें (Or type amount)", fontSize = 12.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("question_cost_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }
            }

            // QUESTION 5: Who is this mainly for? (Retail / Wholesale / Both)
            val buyerOptions = listOf(
                Triple("Retail", "Retail (खुदरा ग्राहक)", "D2C direct consumers at exhibitions or online store"),
                Triple("Wholesale", "Wholesale (थोक B2B)", "B2B bulk orders for boutiques, emporiums & gift sets"),
                Triple("Both", "Both (दोनों खुदरा व थोक)", "Standard catalog listing for all buyers")
            )

            QuestionBlock(
                number = 5,
                titleEnglish = "Who is this mainly for?",
                titleHindi = "यह मुख्य रूप से किसके लिए है?",
                isPreFilled = data.isTargetPreFilled
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    buyerOptions.forEach { (code, title, _) ->
                        val isSelected = data.targetBuyer.equals(code, ignoreCase = true)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .semantics {
                                    role = Role.RadioButton
                                    selected = isSelected
                                    contentDescription = title
                                }
                                .clickable {
                                    onDataChange(data.copy(targetBuyer = code, isTargetPreFilled = false))
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) DeepNavy else Color.White,
                            border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (code) {
                                        "Retail" -> "खुदरा (Retail)"
                                        "Wholesale" -> "थोक (Bulk B2B)"
                                        else -> "दोनों (Both)"
                                    },
                                    color = if (isSelected) Color.White else ArtisanTextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionBlock(
    number: Int,
    titleEnglish: String,
    titleHindi: String,
    isPreFilled: Boolean,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                color = if (isPreFilled) ForestSuccess else TerracottaPrimary,
                shape = CircleShape,
                modifier = Modifier.size(20.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isPreFilled) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    } else {
                        Text(
                            text = "$number",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                text = "$titleHindi ($titleEnglish)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ArtisanTextPrimary
            )

            if (isPreFilled) {
                Surface(
                    color = Color(0xFFDCFCE7),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "स्वतः भरा (Auto-filled)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF15803D),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }

        content()
    }
}
