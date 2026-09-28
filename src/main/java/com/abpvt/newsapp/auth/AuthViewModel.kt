package com.abpvt.newsapp.auth

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abpvt.newsapp.notifications.DailyDigestScheduler
import com.abpvt.newsapp.notifications.NewsFirebaseMessagingService
import com.abpvt.newsapp.notifications.NotificationsPrefs
import com.abpvt.newsapp.utils.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val auth           = FirebaseAuth.getInstance()
    private val sessionManager = SessionManager(application)
    private val prefs          = application.getSharedPreferences(
        NotificationsPrefs.PREFS_NAME, Context.MODE_PRIVATE
    )

    // ── Loading / Error ──────────────────────────────────────────────────────
    private val _isLoading           = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error               = MutableStateFlow<String?>(null)
    val error: StateFlow<String?>     = _error

    // ── Auth state ───────────────────────────────────────────────────────────
    private val _authState            = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState

    private val _profileUpdateSuccess = MutableStateFlow(false)
    val profileUpdateSuccess: StateFlow<Boolean> = _profileUpdateSuccess

    private val _resetEmailSent = MutableStateFlow(false)
    val resetEmailSent: StateFlow<Boolean> = _resetEmailSent

    // ── User name ────────────────────────────────────────────────────────────
    private val _currentUserName = MutableStateFlow(
        auth.currentUser?.let { user ->
            user.displayName?.takeIf { it.isNotBlank() } ?: user.email?.substringBefore("@")
        } ?: ""
    )
    val currentUserName: StateFlow<String> = _currentUserName

    // ── Profile photo ────────────────────────────────────────────────────────
    private val _profilePhotoUri = MutableStateFlow<Uri?>(
        prefs.getString(NotificationsPrefs.KEY_PROFILE_PHOTO_URI, null)?.let { Uri.parse(it) }
    )
    val profilePhotoUri: StateFlow<Uri?> = _profilePhotoUri

    // ── Avatar colour ────────────────────────────────────────────────────────
    private val _avatarColor = MutableStateFlow(
        prefs.getLong(NotificationsPrefs.KEY_AVATAR_COLOR, 0xFF1565C0.toInt().toLong())
    )
    val avatarColor: StateFlow<Long> = _avatarColor

    // ── Notification category toggles ────────────────────────────────────────
    private fun boolFlow(key: String, default: Boolean = true) =
        MutableStateFlow(prefs.getBoolean(key, default))

    private val _notifBreaking      = boolFlow(NotificationsPrefs.KEY_NOTIF_BREAKING)
    val notifBreaking: StateFlow<Boolean> = _notifBreaking

    private val _notifSports        = boolFlow(NotificationsPrefs.KEY_NOTIF_SPORTS)
    val notifSports: StateFlow<Boolean> = _notifSports

    private val _notifTech          = boolFlow(NotificationsPrefs.KEY_NOTIF_TECH)
    val notifTech: StateFlow<Boolean> = _notifTech

    private val _notifBusiness      = boolFlow(NotificationsPrefs.KEY_NOTIF_BUSINESS)
    val notifBusiness: StateFlow<Boolean> = _notifBusiness

    private val _notifHealth        = boolFlow(NotificationsPrefs.KEY_NOTIF_HEALTH)
    val notifHealth: StateFlow<Boolean> = _notifHealth

    private val _notifWorld         = boolFlow(NotificationsPrefs.KEY_NOTIF_WORLD)
    val notifWorld: StateFlow<Boolean> = _notifWorld

    private val _notifEntertainment = boolFlow(NotificationsPrefs.KEY_NOTIF_ENTERTAINMENT)
    val notifEntertainment: StateFlow<Boolean> = _notifEntertainment

    private val _notifComments      = boolFlow(NotificationsPrefs.KEY_NOTIF_COMMENTS)
    val notifComments: StateFlow<Boolean> = _notifComments

    private val _notifDigest        = boolFlow(NotificationsPrefs.KEY_NOTIF_DIGEST)
    val notifDigest: StateFlow<Boolean> = _notifDigest

    private val _digestHour = MutableStateFlow(
        prefs.getInt(NotificationsPrefs.KEY_DIGEST_HOUR, 8)
    )
    val digestHour: StateFlow<Int> = _digestHour

    private val _analyticsConsent = MutableStateFlow(
        prefs.getBoolean(NotificationsPrefs.KEY_ANALYTICS_CONSENT, false)
    )
    val analyticsConsent: StateFlow<Boolean> = _analyticsConsent

    // ── Quiet hours ───────────────────────────────────────────────────────────
    private val _quietHoursEnabled = MutableStateFlow(
        prefs.getBoolean(NotificationsPrefs.KEY_QUIET_HOURS_ENABLED, false)
    )
    val quietHoursEnabled: StateFlow<Boolean> = _quietHoursEnabled

    private val _quietStartHour = MutableStateFlow(
        prefs.getInt(NotificationsPrefs.KEY_QUIET_START_HOUR, 23)
    )
    val quietStartHour: StateFlow<Int> = _quietStartHour

    private val _quietEndHour = MutableStateFlow(
        prefs.getInt(NotificationsPrefs.KEY_QUIET_END_HOUR, 7)
    )
    val quietEndHour: StateFlow<Int> = _quietEndHour

    // ── Firebase auth state listener ─────────────────────────────────────────
    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        if (user != null) {
            // Firebase says someone is signed in — reflect that
            if (_authState.value !is AuthState.Authenticated) {
                _authState.value = AuthState.Authenticated
            }
            // Keep userName in sync if it's blank
            if (_currentUserName.value.isBlank()) {
                _currentUserName.value =
                    user.displayName?.takeIf { it.isNotBlank() }
                        ?: user.email?.substringBefore("@") ?: ""
            }
        } else {
            // Firebase says no one is signed in — only update state if we're
            // not already in the middle of a login attempt
            if (_authState.value is AuthState.Authenticated) {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    init {
        // If Firebase has a cached user but our session is expired / "Remember Me"
        // was off, sign out immediately BEFORE registering the listener so the
        // listener sees the correct signed-out state on first fire.
        if (auth.currentUser != null && !sessionManager.isSessionValid()) {
            auth.signOut()
        }
        // Register the listener — fires synchronously with current auth state.
        auth.addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }


    // ── Auth ─────────────────────────────────────────────────────────────────

    fun login(email: String, pass: String, rememberMe: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = auth.signInWithEmailAndPassword(email, pass).await()
                val userId = result.user?.uid ?: ""
                if (rememberMe) sessionManager.saveSession(email, userId, rememberMe)
                _currentUserName.value = auth.currentUser?.let { user ->
                    user.displayName?.takeIf { it.isNotBlank() } ?: user.email?.substringBefore("@")
                } ?: ""
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                _error.value = e.message ?: "Login failed"
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            } finally { _isLoading.value = false }
        }
    }

    fun register(name: String, email: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = auth.createUserWithEmailAndPassword(email, pass).await()
                val user   = result.user
                if (user != null && name.isNotBlank()) {
                    user.updateProfile(
                        UserProfileChangeRequest.Builder().setDisplayName(name).build()
                    ).await()
                    _currentUserName.value = name
                }
                sessionManager.saveSession(email, user?.uid ?: "", true)
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                _error.value = e.message ?: "Registration failed"
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            } finally { _isLoading.value = false }
        }
    }

    fun logout() {
        auth.signOut()
        sessionManager.clearSession()
        _authState.value  = AuthState.Unauthenticated
        _currentUserName.value = ""
    }

    fun signInWithGoogle(idToken: String, rememberMe: Boolean = true) {
        viewModelScope.launch {
            _isLoading.value = true; _error.value = null
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                val user = result.user
                if (user != null) {
                    val userId = user.uid
                    // Google sign-in is always treated as "remember me" —
                    // the user explicitly chose their Google account, so persist the session.
                    sessionManager.saveSession(user.email ?: "", userId, true)
                    _currentUserName.value = user.displayName?.takeIf { it.isNotBlank() } ?: user.email?.substringBefore("@") ?: ""
                    _authState.value = AuthState.Authenticated
                } else {
                    _error.value = "Google Sign-In failed"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Google Sign-In failed"
                _authState.value = AuthState.Error(e.message ?: "Google Sign-In failed")
            } finally { _isLoading.value = false }
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _resetEmailSent.value = false
            try {
                auth.sendPasswordResetEmail(email).await()
                _resetEmailSent.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to send reset email"
            } finally { _isLoading.value = false }
        }
    }

    fun resetPasswordState() {
        _resetEmailSent.value = false
        _error.value = null
    }

    fun clearError() {
        _error.value = null
    }

    fun setExternalError(msg: String) {
        _error.value = msg
    }

    // ── Profile updates ───────────────────────────────────────────────────────

    fun updateUsername(newName: String) {
        if (newName.isBlank()) { _error.value = "Username cannot be empty"; return }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _profileUpdateSuccess.value = false
            try {
                val user = auth.currentUser ?: run { _error.value = "No user logged in"; return@launch }
                user.updateProfile(
                    UserProfileChangeRequest.Builder().setDisplayName(newName).build()
                ).await()
                _currentUserName.value = newName
                _profileUpdateSuccess.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update profile"
            } finally { _isLoading.value = false }
        }
    }

    fun resetProfileUpdateState() { _profileUpdateSuccess.value = false }

    fun setProfilePhoto(uri: Uri) {
        _profilePhotoUri.value = uri
        prefs.edit().putString(NotificationsPrefs.KEY_PROFILE_PHOTO_URI, uri.toString()).apply()
    }

    fun clearProfilePhoto() {
        _profilePhotoUri.value = null
        prefs.edit().remove(NotificationsPrefs.KEY_PROFILE_PHOTO_URI).apply()
    }

    fun setAvatarColor(color: Long) {
        _avatarColor.value = color
        prefs.edit().putLong(NotificationsPrefs.KEY_AVATAR_COLOR, color).apply()
    }

    // ── Notification toggles ──────────────────────────────────────────────────

    fun setNotificationCategory(prefKey: String, topic: String, enabled: Boolean) {
        prefs.edit().putBoolean(prefKey, enabled).apply()
        // Update the corresponding StateFlow
        when (prefKey) {
            NotificationsPrefs.KEY_NOTIF_BREAKING      -> _notifBreaking.value      = enabled
            NotificationsPrefs.KEY_NOTIF_SPORTS        -> _notifSports.value        = enabled
            NotificationsPrefs.KEY_NOTIF_TECH          -> _notifTech.value          = enabled
            NotificationsPrefs.KEY_NOTIF_BUSINESS      -> _notifBusiness.value      = enabled
            NotificationsPrefs.KEY_NOTIF_HEALTH        -> _notifHealth.value        = enabled
            NotificationsPrefs.KEY_NOTIF_WORLD         -> _notifWorld.value         = enabled
            NotificationsPrefs.KEY_NOTIF_ENTERTAINMENT -> _notifEntertainment.value = enabled
            NotificationsPrefs.KEY_NOTIF_COMMENTS      -> _notifComments.value      = enabled
            NotificationsPrefs.KEY_NOTIF_DIGEST        -> {
                _notifDigest.value = enabled
                val ctx = getApplication<Application>()
                if (enabled) DailyDigestScheduler.schedule(ctx, _digestHour.value)
                else         DailyDigestScheduler.cancel(ctx)
            }
        }
        if (topic.isNotBlank()) {
            if (enabled) FirebaseMessaging.getInstance().subscribeToTopic(topic)
            else FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
        }
    }

    // ── Quiet hours ───────────────────────────────────────────────────────────

    fun setQuietHoursEnabled(enabled: Boolean) {
        _quietHoursEnabled.value = enabled
        prefs.edit().putBoolean(NotificationsPrefs.KEY_QUIET_HOURS_ENABLED, enabled).apply()
    }

    fun setQuietStartHour(hour: Int) {
        _quietStartHour.value = hour
        prefs.edit().putInt(NotificationsPrefs.KEY_QUIET_START_HOUR, hour).apply()
    }

    fun setQuietEndHour(hour: Int) {
        _quietEndHour.value = hour
        prefs.edit().putInt(NotificationsPrefs.KEY_QUIET_END_HOUR, hour).apply()
    }

    fun setDigestHour(hour: Int) {
        val safe = (hour + 24) % 24
        _digestHour.value = safe
        prefs.edit().putInt(NotificationsPrefs.KEY_DIGEST_HOUR, safe).apply()
        if (_notifDigest.value) DailyDigestScheduler.schedule(getApplication(), safe)
    }

    fun setAnalyticsConsent(enabled: Boolean) {
        _analyticsConsent.value = enabled
        prefs.edit().putBoolean(NotificationsPrefs.KEY_ANALYTICS_CONSENT, enabled).apply()
        FirebaseAnalytics.getInstance(getApplication()).setAnalyticsCollectionEnabled(enabled)
    }
}

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Authenticated   : AuthState()
    data class Error(val message: String) : AuthState()
}
