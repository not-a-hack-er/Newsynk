package com.abpvt.newsapp.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abpvt.newsapp.utils.SessionManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.UserProfileChangeRequest

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val sessionManager = SessionManager(application)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState

    private val _profileUpdateSuccess = MutableStateFlow<Boolean>(false)
    val profileUpdateSuccess: StateFlow<Boolean> = _profileUpdateSuccess

    init {
        // Check for existing valid session
        if (sessionManager.isSessionValid()) {
            _authState.value = AuthState.Authenticated
        }
    }

    fun login(email: String, pass: String, rememberMe: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = auth.signInWithEmailAndPassword(email, pass).await()
                val userId = result.user?.uid ?: ""
                
                // Save session if remember me is checked
                if (rememberMe) {
                    sessionManager.saveSession(email, userId, rememberMe)
                }
                
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                _error.value = e.message ?: "Login failed"
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(name: String, email: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = auth.createUserWithEmailAndPassword(email, pass).await()
                val user = result.user
                val userId = user?.uid ?: ""
                
                // Set the display name right after registration
                if (user != null && name.isNotBlank()) {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()
                    user.updateProfile(profileUpdates).await()
                }
                
                // Auto-save session on registration
                sessionManager.saveSession(email, userId, true)
                
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                _error.value = e.message ?: "Registration failed"
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        auth.signOut()
        sessionManager.clearSession()
        _authState.value = AuthState.Unauthenticated
    }

    fun updateUsername(newName: String) {
        if (newName.isBlank()) {
            _error.value = "Username cannot be empty"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _profileUpdateSuccess.value = false
            
            try {
                val user = auth.currentUser
                if (user != null) {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(newName)
                        .build()
                    user.updateProfile(profileUpdates).await()
                    _profileUpdateSuccess.value = true
                } else {
                    _error.value = "No user logged in"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update profile"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun resetProfileUpdateState() {
        _profileUpdateSuccess.value = false
    }
}

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Authenticated : AuthState()
    data class Error(val message: String) : AuthState()
}
