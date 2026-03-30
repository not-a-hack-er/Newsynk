package com.abpvt.newsapp.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth

/**
 * Manages user session persistence using SharedPreferences.
 * Handles "Remember Me" functionality with 7-day session expiry.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "newsapp_session"
        private const val KEY_EMAIL = "user_email"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_LOGIN_TIMESTAMP = "login_timestamp"
        private const val KEY_REMEMBER_ME = "remember_me"

        // 7 days in milliseconds
        private const val SESSION_DURATION = 7 * 24 * 60 * 60 * 1000L
    }

    /**
     * Save user session after successful login.
     */
    fun saveSession(email: String, userId: String, rememberMe: Boolean) {
        prefs.edit().apply {
            putString(KEY_EMAIL, email)
            putString(KEY_USER_ID, userId)
            putLong(KEY_LOGIN_TIMESTAMP, System.currentTimeMillis())
            putBoolean(KEY_REMEMBER_ME, rememberMe)
            apply()
        }
    }

    /**
     * Check if a valid session exists.
     * Returns true if:
     * 1. Remember Me was enabled
     * 2. The 7-day window hasn't expired
     * 3. Firebase still has an active current user (not revoked/deleted)
     */
    fun isSessionValid(): Boolean {
        val rememberMe = prefs.getBoolean(KEY_REMEMBER_ME, false)
        if (!rememberMe) return false

        val loginTimestamp = prefs.getLong(KEY_LOGIN_TIMESTAMP, 0)
        if (loginTimestamp == 0L) return false

        val sessionAge = System.currentTimeMillis() - loginTimestamp
        if (sessionAge >= SESSION_DURATION) return false

        // Also verify Firebase still has an authenticated user (handles revoked accounts)
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        return firebaseUser != null
    }

    fun getUserEmail(): String? = prefs.getString(KEY_EMAIL, null)

    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)

    /**
     * Clear all session data (logout).
     */
    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun isRememberMeEnabled(): Boolean = prefs.getBoolean(KEY_REMEMBER_ME, false)
}
