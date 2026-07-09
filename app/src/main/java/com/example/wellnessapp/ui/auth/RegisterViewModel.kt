// Team5
// @author: Lei Tianyou

/*
 * Scope: Login and registration flow
 */

package com.example.wellnessapp.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.wellnessapp.data.model.RegisterRequest
import com.example.wellnessapp.data.model.UserResponse
import com.example.wellnessapp.data.repository.AuthRepository
import com.example.wellnessapp.util.UiState
import com.example.wellnessapp.util.ValidationUtils
import kotlinx.coroutines.launch

class RegisterViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository(application)

    private val _uiState = MutableLiveData<UiState<UserResponse>>(UiState.Idle)
    val uiState: LiveData<UiState<UserResponse>> = _uiState

    fun register(username: String, email: String, password: String, confirmPassword: String) {
        val trimmedUsername = username.trim()
        val trimmedEmail = email.trim()
        ValidationUtils.validateRegister(
            username = trimmedUsername,
            email = trimmedEmail,
            password = password,
            confirmPassword = confirmPassword
        )?.let { message ->
            _uiState.value = UiState.Error(message)
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            authRepository.registerResult(
                RegisterRequest(
                    username = trimmedUsername,
                    email = trimmedEmail,
                    password = password
                )
            )
                .onSuccess { user ->
                    _uiState.value = UiState.Success(user)
                }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Registration failed")
                }
        }
    }
}
