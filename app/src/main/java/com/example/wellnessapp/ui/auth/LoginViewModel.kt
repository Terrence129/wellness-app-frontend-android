package com.example.wellnessapp.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.wellnessapp.data.model.LoginRequest
import com.example.wellnessapp.data.model.LoginResponse
import com.example.wellnessapp.data.repository.AuthRepository
import com.example.wellnessapp.util.UiState
import com.example.wellnessapp.util.ValidationUtils
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository(application)

    private val _uiState = MutableLiveData<UiState<LoginResponse>>(UiState.Idle)
    val uiState: LiveData<UiState<LoginResponse>> = _uiState

    fun login(email: String, password: String) {
        val trimmedEmail = email.trim()
        ValidationUtils.validateLogin(trimmedEmail, password)?.let { message ->
            _uiState.value = UiState.Error(message)
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            authRepository.loginResult(LoginRequest(trimmedEmail, password))
                .onSuccess { response ->
                    _uiState.value = UiState.Success(response)
                }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Login failed")
                }
        }
    }
}
