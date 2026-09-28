package com.splitezapp.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitezapp.data.api.ApiClient
import com.splitezapp.data.models.*
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
    var needsEmailVerification by mutableStateOf(false)
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
            needsEmailVerification = false
            try {
                val resp = ApiClient.api.register(
                    RegisterRequest(email, password, firstName, lastName)
                )
                if (resp.needsVerification) {
                    needsEmailVerification = true
                } else {
                    ApiClient.setTokens(resp.accessToken!!, resp.refreshToken!!)
                    currentUser = ApiClient.api.getMe()
                    isLoggedIn = true
                }
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

    fun updateCurrency(currency: String) {
        viewModelScope.launch {
            try {
                val updated = ApiClient.api.updateMe(UpdateUserRequest(currency = currency))
                currentUser = updated
            } catch (_: Exception) {}
        }
    }

    fun updateProfile(firstName: String, lastName: String?, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                val updated = ApiClient.api.updateMe(UpdateUserRequest(firstName = firstName, lastName = lastName))
                currentUser = updated
                onDone(true)
            } catch (e: Exception) {
                error = e.message ?: "Update failed"
                onDone(false)
            }
            isLoading = false
        }
    }

    fun changePassword(current: String, new: String, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                ApiClient.api.changePassword(ChangePasswordRequest(current, new))
                onDone(true)
            } catch (e: Exception) {
                error = e.message ?: "Password change failed"
                onDone(false)
            }
            isLoading = false
        }
    }

    fun deleteAccount(onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                ApiClient.api.deleteMe()
            } catch (_: Exception) {}
            ApiClient.clearTokens()
            currentUser = null
            isLoggedIn = false
            onDone()
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
