package com.splitezapp.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitezapp.data.api.ApiClient
import com.splitezapp.data.models.LoginRequest
import com.splitezapp.data.models.RegisterRequest
import com.splitezapp.data.models.UserProfile
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    var isLoggedIn by mutableStateOf(ApiClient.isLoggedIn)
        private set
    var currentUser by mutableStateOf<UserProfile?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun checkAuth() {
        if (!ApiClient.isLoggedIn) return
        viewModelScope.launch {
            try {
                currentUser = ApiClient.api.getMe()
                isLoggedIn = true
            } catch (_: Exception) {
                isLoggedIn = false
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                val tokens = ApiClient.api.login(LoginRequest(email, password))
                ApiClient.setTokens(tokens.accessToken, tokens.refreshToken)
                currentUser = ApiClient.api.getMe()
                isLoggedIn = true
            } catch (e: Exception) {
                error = e.message ?: "Login failed"
            }
            isLoading = false
        }
    }

    fun register(email: String, password: String, firstName: String, lastName: String?) {
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                val tokens = ApiClient.api.register(
                    RegisterRequest(email, password, firstName, lastName)
                )
                ApiClient.setTokens(tokens.accessToken, tokens.refreshToken)
                currentUser = ApiClient.api.getMe()
                isLoggedIn = true
            } catch (e: Exception) {
                error = e.message ?: "Registration failed"
            }
            isLoading = false
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            try {
                ApiClient.api.forgotPassword(
                    com.splitezapp.data.models.ForgotPasswordRequest(email)
                )
            } catch (_: Exception) {}
        }
    }

    fun logout() {
        viewModelScope.launch {
            try { ApiClient.api.logout() } catch (_: Exception) {}
            ApiClient.clearTokens()
            currentUser = null
            isLoggedIn = false
        }
    }
}
