package com.minlish.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "minlish_prefs")

class TokenManager(private val context: Context) {
    companion object {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val FULL_NAME = stringPreferencesKey("full_name")
        val EMAIL = stringPreferencesKey("email")
        val LEARNING_GOAL = stringPreferencesKey("learning_goal")
        val DAILY_GOAL = intPreferencesKey("daily_goal")
        val IS_ONBOARDED = booleanPreferencesKey("is_onboarded")
        val CURRENT_LEVEL_ID = stringPreferencesKey("current_level_id")
        val TARGET_LEVEL_ID = stringPreferencesKey("target_level_id")
        val AVATAR_URL = stringPreferencesKey("avatar_url")
    }

    val accessToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[ACCESS_TOKEN]
    }

    val refreshToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[REFRESH_TOKEN]
    }

    val fullName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[FULL_NAME] ?: "Guest"
    }

    val email: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[EMAIL] ?: ""
    }

    val learningGoal: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LEARNING_GOAL] ?: "TOEIC"
    }

    val dailyNewWordsGoal: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[DAILY_GOAL] ?: 10
    }

    val isOnboarded: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_ONBOARDED] ?: false
    }

    val currentLevelId: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CURRENT_LEVEL_ID] ?: ""
    }

    val targetLevelId: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[TARGET_LEVEL_ID] ?: ""
    }

    val avatarUrl: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[AVATAR_URL] ?: ""
    }

    suspend fun getAccessTokenBlocking(): String? {
        return context.dataStore.data.first()[ACCESS_TOKEN]
    }

    suspend fun getRefreshTokenBlocking(): String? {
        return context.dataStore.data.first()[REFRESH_TOKEN]
    }

    suspend fun saveAuthResponse(
        accessToken: String,
        refreshToken: String,
        fullName: String,
        email: String,
        learningGoal: String?,
        dailyGoal: Int,
        isOnboarded: Boolean,
        currentLevelId: String? = null,
        targetLevelId: String? = null,
        avatarUrl: String? = null
    ) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = accessToken
            preferences[REFRESH_TOKEN] = refreshToken
            preferences[FULL_NAME] = fullName
            preferences[EMAIL] = email
            preferences[LEARNING_GOAL] = learningGoal ?: "TOEIC"
            preferences[DAILY_GOAL] = dailyGoal
            preferences[IS_ONBOARDED] = isOnboarded
            preferences[CURRENT_LEVEL_ID] = currentLevelId ?: ""
            preferences[TARGET_LEVEL_ID] = targetLevelId ?: ""
            preferences[AVATAR_URL] = avatarUrl ?: ""
        }
    }

    suspend fun updateDailyGoal(goal: Int) {
        context.dataStore.edit { preferences ->
            preferences[DAILY_GOAL] = goal
        }
    }

    suspend fun updateFullName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[FULL_NAME] = name
        }
    }

    suspend fun updateAvatarUrl(avatarUrl: String) {
        context.dataStore.edit { preferences ->
            preferences[AVATAR_URL] = avatarUrl
        }
    }

    suspend fun updateLearningGoal(goal: String) {
        context.dataStore.edit { preferences ->
            preferences[LEARNING_GOAL] = goal
        }
    }

    suspend fun updateTargetLevelId(levelId: String) {
        context.dataStore.edit { preferences ->
            preferences[TARGET_LEVEL_ID] = levelId
        }
    }

    suspend fun updateCurrentLevelId(levelId: String) {
        context.dataStore.edit { preferences ->
            preferences[CURRENT_LEVEL_ID] = levelId
        }
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = accessToken
            preferences[REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun clearAuth() {
        context.dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN)
            preferences.remove(REFRESH_TOKEN)
            preferences.remove(FULL_NAME)
            preferences.remove(EMAIL)
            preferences.remove(IS_ONBOARDED)
            preferences.remove(CURRENT_LEVEL_ID)
            preferences.remove(TARGET_LEVEL_ID)
            preferences.remove(AVATAR_URL)
        }
    }
}
