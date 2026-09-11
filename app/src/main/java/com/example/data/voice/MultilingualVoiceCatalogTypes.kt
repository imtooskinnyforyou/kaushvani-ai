package com.example.data.voice

/**
 * Explicit error categories covering all real failure modes specified in the requirements.
 */
sealed class VoiceCatalogError(
    val titleHindi: String,
    val titleEnglish: String,
    val descriptionHindi: String,
    val descriptionEnglish: String,
    val iconName: String,
    val canRetryAudio: Boolean,
    val suggestManualInput: Boolean
) {
    object MicrophoneDenied : VoiceCatalogError(
        titleHindi = "माइक्रोफ़ोन अनुमति आवश्यक है",
        titleEnglish = "Microphone Permission Required",
        descriptionHindi = "आवाज़ से उत्पाद का विवरण रिकॉर्ड करने के लिए ऐप को माइक्रोफ़ोन की अनुमति दें।",
        descriptionEnglish = "Please grant microphone permission to record your craft description.",
        iconName = "mic_off",
        canRetryAudio = true,
        suggestManualInput = true
    )

    object SilenceTimeout : VoiceCatalogError(
        titleHindi = "कोई आवाज़ नहीं सुनाई दी",
        titleEnglish = "No Speech Detected",
        descriptionHindi = "कृपया माइक्रोफ़ोन के पास आकर स्पष्ट आवाज़ में बोलें, या नीचे लिखकर बताएं।",
        descriptionEnglish = "We didn't catch any audio. Please speak closer to the mic or type manually.",
        iconName = "volume_mute",
        canRetryAudio = true,
        suggestManualInput = true
    )

    object RecognitionFailure : VoiceCatalogError(
        titleHindi = "स्पीच पहचान में त्रुटि",
        titleEnglish = "Voice Recognition Error",
        descriptionHindi = "ऑडियो स्पष्ट नहीं समझा जा सका। कृपया शांत वातावरण में दोबारा बोलें।",
        descriptionEnglish = "Speech recognition encountered an issue. Please try speaking again.",
        iconName = "error_outline",
        canRetryAudio = true,
        suggestManualInput = true
    )

    data class UnsupportedLanguage(val langName: String, val localeTag: String) : VoiceCatalogError(
        titleHindi = "$langName स्पीच इंजन अनुपलब्ध",
        titleEnglish = "$langName Speech Engine Unavailable",
        descriptionHindi = "इस फ़ोन में $langName ($localeTag) का वॉयस पैक इंस्टॉल नहीं है। आप नीचे हस्तलिखित विवरण लिख सकते हैं या हिंदी/अंग्रेजी चुन सकते हैं।",
        descriptionEnglish = "Device does not support $langName speech input. You can type in $langName or choose another language.",
        iconName = "translate",
        canRetryAudio = false,
        suggestManualInput = true
    )

    object NetworkFailure : VoiceCatalogError(
        titleHindi = "नेटवर्क कनेक्शन धीमा या बंद",
        titleEnglish = "Network Connection Issue",
        descriptionHindi = "इंटरनेट धीमा होने पर ऑफ़लाइन विश्लेषण सक्रिय हो जाता है। आप नीचे विवरण लिख भी सकते हैं।",
        descriptionEnglish = "Network timed out. Offline smart parser will be used, or you can type manually.",
        iconName = "wifi_off",
        canRetryAudio = true,
        suggestManualInput = true
    )
}
