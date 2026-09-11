package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.components.RegionalLanguage
import com.example.ui.components.SupportedRegionalLanguages
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "kaarigar_user_preferences")

/**
 * Data class representing a persisted user session across app restarts.
 */
data class UserSession(
    val uid: String,
    val email: String? = null,
    val displayName: String? = null,
    val phoneNumber: String? = null,
    val photoUrl: String? = null,
    val role: String = "ARTISAN",
    val isAnonymous: Boolean = false,
    val isLoggedIn: Boolean = true,
    val lastLoginTimestamp: Long = System.currentTimeMillis()
)

/**
 * DataStore Repository managing user profile preferences,
 * including regional language preference, speech recognizer locale,
 * persistent user authentication session, and AI translation configurations.
 */
class UserPreferencesRepository(private val context: Context) {

    companion object {
        val KEY_PREFERRED_LANGUAGE_CODE = stringPreferencesKey("preferred_language_code")
        val KEY_PREFERRED_LANGUAGE_NAME = stringPreferencesKey("preferred_language_name")
        val KEY_PREFERRED_LOCALE_TAG = stringPreferencesKey("preferred_locale_tag")
        val KEY_PREFERRED_NATIVE_NAME = stringPreferencesKey("preferred_native_name")

        // User Auth Session Persistence Keys
        val KEY_SESSION_UID = stringPreferencesKey("session_uid")
        val KEY_SESSION_EMAIL = stringPreferencesKey("session_email")
        val KEY_SESSION_DISPLAY_NAME = stringPreferencesKey("session_display_name")
        val KEY_SESSION_PHONE_NUMBER = stringPreferencesKey("session_phone_number")
        val KEY_SESSION_PHOTO_URL = stringPreferencesKey("session_photo_url")
        val KEY_SESSION_ROLE = stringPreferencesKey("session_role")
        val KEY_SESSION_IS_ANONYMOUS = booleanPreferencesKey("session_is_anonymous")
        val KEY_SESSION_IS_LOGGED_IN = booleanPreferencesKey("session_is_logged_in")
        val KEY_SESSION_LAST_LOGIN = longPreferencesKey("session_last_login_timestamp")
    }

    /**
     * Observable Flow of user's authentication session persisted in DataStore.
     */
    val userSessionFlow: Flow<UserSession?> = context.userPreferencesDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val uid = preferences[KEY_SESSION_UID]
            val isLoggedIn = preferences[KEY_SESSION_IS_LOGGED_IN] ?: false

            if (!uid.isNullOrBlank() && isLoggedIn) {
                UserSession(
                    uid = uid,
                    email = preferences[KEY_SESSION_EMAIL],
                    displayName = preferences[KEY_SESSION_DISPLAY_NAME],
                    phoneNumber = preferences[KEY_SESSION_PHONE_NUMBER],
                    photoUrl = preferences[KEY_SESSION_PHOTO_URL],
                    role = preferences[KEY_SESSION_ROLE] ?: "ARTISAN",
                    isAnonymous = preferences[KEY_SESSION_IS_ANONYMOUS] ?: false,
                    isLoggedIn = true,
                    lastLoginTimestamp = preferences[KEY_SESSION_LAST_LOGIN] ?: System.currentTimeMillis()
                )
            } else {
                null
            }
        }

    /**
     * Saves user authentication session upon successful login/registration.
     */
    suspend fun saveUserSession(session: UserSession) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[KEY_SESSION_UID] = session.uid
            session.email?.let { preferences[KEY_SESSION_EMAIL] = it } ?: preferences.remove(KEY_SESSION_EMAIL)
            session.displayName?.let { preferences[KEY_SESSION_DISPLAY_NAME] = it } ?: preferences.remove(KEY_SESSION_DISPLAY_NAME)
            session.phoneNumber?.let { preferences[KEY_SESSION_PHONE_NUMBER] = it } ?: preferences.remove(KEY_SESSION_PHONE_NUMBER)
            session.photoUrl?.let { preferences[KEY_SESSION_PHOTO_URL] = it } ?: preferences.remove(KEY_SESSION_PHOTO_URL)
            preferences[KEY_SESSION_ROLE] = session.role
            preferences[KEY_SESSION_IS_ANONYMOUS] = session.isAnonymous
            preferences[KEY_SESSION_IS_LOGGED_IN] = true
            preferences[KEY_SESSION_LAST_LOGIN] = session.lastLoginTimestamp
        }
    }

    /**
     * Clears user authentication session upon sign out.
     */
    suspend fun clearUserSession() {
        context.userPreferencesDataStore.edit { preferences ->
            preferences.remove(KEY_SESSION_UID)
            preferences.remove(KEY_SESSION_EMAIL)
            preferences.remove(KEY_SESSION_DISPLAY_NAME)
            preferences.remove(KEY_SESSION_PHONE_NUMBER)
            preferences.remove(KEY_SESSION_PHOTO_URL)
            preferences.remove(KEY_SESSION_ROLE)
            preferences.remove(KEY_SESSION_IS_ANONYMOUS)
            preferences[KEY_SESSION_IS_LOGGED_IN] = false
        }
    }

    /**
     * Retrieves currently persisted session snapshot.
     */
    suspend fun getPersistedUserSession(): UserSession? {
        return try {
            userSessionFlow.first()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Observable Flow of user's preferred language loaded from persistent Jetpack DataStore.
     * Defaults to Hindi (hi-IN) if no preference has been saved.
     */
    val preferredLanguageFlow: Flow<RegionalLanguage> = context.userPreferencesDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val code = preferences[KEY_PREFERRED_LANGUAGE_CODE]
            val name = preferences[KEY_PREFERRED_LANGUAGE_NAME]
            val localeTag = preferences[KEY_PREFERRED_LOCALE_TAG]

            resolveLanguage(code = code, name = name, localeTag = localeTag)
        }

    /**
     * Persist the selected language and SpeechRecognizer locale tag in DataStore.
     */
    suspend fun savePreferredLanguage(language: RegionalLanguage) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[KEY_PREFERRED_LANGUAGE_CODE] = language.code
            preferences[KEY_PREFERRED_LANGUAGE_NAME] = language.name
            preferences[KEY_PREFERRED_LOCALE_TAG] = language.localeTag
            preferences[KEY_PREFERRED_NATIVE_NAME] = language.nativeName
        }
    }

    /**
     * Synchronous / one-shot retrieval of the currently saved language preference.
     */
    suspend fun getSavedLanguage(): RegionalLanguage {
        return try {
            preferredLanguageFlow.first()
        } catch (e: Exception) {
            SupportedRegionalLanguages.first()
        }
    }

    private fun resolveLanguage(code: String?, name: String?, localeTag: String?): RegionalLanguage {
        if (!code.isNullOrBlank()) {
            val matched = SupportedRegionalLanguages.find { it.code.equals(code, ignoreCase = true) }
            if (matched != null) return matched
        }
        if (!name.isNullOrBlank()) {
            val matched = SupportedRegionalLanguages.find { it.name.equals(name, ignoreCase = true) }
            if (matched != null) return matched
        }
        if (!localeTag.isNullOrBlank()) {
            val matched = SupportedRegionalLanguages.find { it.localeTag.equals(localeTag, ignoreCase = true) }
            if (matched != null) return matched
        }
        return SupportedRegionalLanguages.first() // Default Hindi (hi-IN)
    }
}
