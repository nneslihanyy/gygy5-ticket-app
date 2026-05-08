package com.turkcell.ticketapp.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.core.domain.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val token: String? = null,
    val error: String? = null
)

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)

            authRepository.login(email, password)
                .onSuccess { session ->
                    _uiState.value = LoginUiState(
                        token = session.accessToken
                    )
                }
                .onFailure { throwable ->
                    _uiState.value = LoginUiState(
                        error = throwable.message ?: "Bilinmeyen hata"
                    )
                }
        }
    }
}
