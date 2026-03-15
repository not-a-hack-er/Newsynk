package com.abpvt.newsapp.utils

import android.content.Context
import android.content.SharedPreferences

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
     * Returns true if remember me is enabled and session hasn't expired.
     */
    fun isSessionValid(): Boolean {
        val rememberMe = prefs.getBoolean(KEY_REMEMBER_ME, false)
        if (!rememberMe) return false
        
        val loginTimestamp = prefs.getLong(KEY_LOGIN_TIMESTAMP, 0)
        if (loginTimestamp == 0L) return false
        
        val currentTime = System.currentTimeMillis()
        val sessionAge = currentTime - loginTimestamp
        
        return sessionAge < SESSION_DURATION
    }
    
    /**
     * Get stored user email.
     */
    fun getUserEmail(): String? {
        return prefs.getString(KEY_EMAIL, null)
    }
    
    /**
     * Get stored user ID.
     */
    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }
    
    /**
     * Clear all session data (logout).
     */
    fun clearSession() {
        prefs.edit().clear().apply()
    }
    
    /**
     * Check if remember me was enabled.
     */
    fun isRememberMeEnabled(): Boolean {
        return prefs.getBoolean(KEY_REMEMBER_ME, false)
    }
}
