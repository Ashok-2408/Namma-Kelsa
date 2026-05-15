package com.nammakelsa.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "namma_kelsa_prefs")

class PreferencesManager(context: Context) {
    private val dataStore = context.dataStore

    private object Keys {
        val USER_ID = stringPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_PHONE = stringPreferencesKey("user_phone")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_ADDRESS = stringPreferencesKey("user_address")
        val USER_CITY = stringPreferencesKey("user_city")
        val USER_ROLE = stringPreferencesKey("user_role")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val HAS_ONBOARDED = booleanPreferencesKey("has_onboarded")
        val LANGUAGE_SELECTED = booleanPreferencesKey("language_selected")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val USER_INFO_COLLECTED = booleanPreferencesKey("user_info_collected")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val FCM_TOKEN = stringPreferencesKey("fcm_token")
    }

    val userId: Flow<String?> = dataStore.data.map { it[Keys.USER_ID] }
    val userName: Flow<String?> = dataStore.data.map { it[Keys.USER_NAME] }
    val userPhone: Flow<String?> = dataStore.data.map { it[Keys.USER_PHONE] }
    val userRole: Flow<String?> = dataStore.data.map { it[Keys.USER_ROLE] }
    val isLoggedIn: Flow<Boolean> = dataStore.data.map { it[Keys.IS_LOGGED_IN] ?: false }
    val hasOnboarded: Flow<Boolean> = dataStore.data.map { it[Keys.HAS_ONBOARDED] ?: false }
    val languageSelected: Flow<Boolean> = dataStore.data.map { it[Keys.LANGUAGE_SELECTED] ?: false }
    val appLanguage: Flow<String?> = dataStore.data.map { it[Keys.APP_LANGUAGE] }
    val userInfoCollected: Flow<Boolean> = dataStore.data.map { it[Keys.USER_INFO_COLLECTED] ?: false }
    val isDarkMode: Flow<Boolean?> = dataStore.data.map { it[Keys.DARK_MODE] }
    val fcmToken: Flow<String?> = dataStore.data.map { it[Keys.FCM_TOKEN] }

    suspend fun getUserId(): String = dataStore.data.first()[Keys.USER_ID] ?: ""
    suspend fun getUserName(): String = dataStore.data.first()[Keys.USER_NAME] ?: ""
    suspend fun getUserPhone(): String = dataStore.data.first()[Keys.USER_PHONE] ?: ""
    suspend fun getUserEmail(): String = dataStore.data.first()[Keys.USER_EMAIL] ?: ""
    suspend fun getUserAddress(): String = dataStore.data.first()[Keys.USER_ADDRESS] ?: ""
    suspend fun getUserCity(): String = dataStore.data.first()[Keys.USER_CITY] ?: ""
    suspend fun getUserRole(): String = dataStore.data.first()[Keys.USER_ROLE] ?: ""
    suspend fun getAppLanguage(): String = dataStore.data.first()[Keys.APP_LANGUAGE] ?: "en"

    suspend fun saveUserId(uid: String) { dataStore.edit { it[Keys.USER_ID] = uid } }
    suspend fun saveUserName(name: String) { dataStore.edit { it[Keys.USER_NAME] = name } }
    suspend fun saveUserPhone(phone: String) { dataStore.edit { it[Keys.USER_PHONE] = phone } }
    suspend fun saveUserEmail(email: String) { dataStore.edit { it[Keys.USER_EMAIL] = email } }
    suspend fun saveUserAddress(address: String) { dataStore.edit { it[Keys.USER_ADDRESS] = address } }
    suspend fun saveUserCity(city: String) { dataStore.edit { it[Keys.USER_CITY] = city } }
    suspend fun saveUserRole(role: String) { dataStore.edit { it[Keys.USER_ROLE] = role } }
    suspend fun setLoggedIn(loggedIn: Boolean) { dataStore.edit { it[Keys.IS_LOGGED_IN] = loggedIn } }
    suspend fun setHasOnboarded(onboarded: Boolean) { dataStore.edit { it[Keys.HAS_ONBOARDED] = onboarded } }
    suspend fun setLanguageSelected(selected: Boolean) { dataStore.edit { it[Keys.LANGUAGE_SELECTED] = selected } }
    suspend fun setAppLanguage(language: String) { dataStore.edit { it[Keys.APP_LANGUAGE] = language } }
    suspend fun setUserInfoCollected(collected: Boolean) { dataStore.edit { it[Keys.USER_INFO_COLLECTED] = collected } }
    suspend fun setDarkMode(enabled: Boolean) { dataStore.edit { it[Keys.DARK_MODE] = enabled } }
    suspend fun saveFcmToken(token: String) { dataStore.edit { it[Keys.FCM_TOKEN] = token } }

    suspend fun clearAll() { dataStore.edit { it.clear() } }
}
