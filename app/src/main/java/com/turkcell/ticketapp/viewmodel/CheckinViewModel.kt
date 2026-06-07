package com.turkcell.ticketapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.core.domain.checkin.CheckinRepository
import com.turkcell.core.domain.checkin.CheckinResult
import com.turkcell.ticketapp.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface CheckinState {
    data object Idle : CheckinState
    data object Loading : CheckinState
    data class Success(val result: CheckinResult) : CheckinState
    data class Error(val message: String) : CheckinState
}

data class CheckinUiState(
    val scanState: CheckinState = CheckinState.Idle,
)

class CheckinViewModel(
    private val checkinRepository: CheckinRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CheckinUiState())
    val state: StateFlow<CheckinUiState> = _state.asStateFlow()

    fun onQrScanned(qrCode: String) {
        if (qrCode.isBlank()) return
        val current = _state.value
        if (current.scanState is CheckinState.Loading) return

        _state.update { it.copy(scanState = CheckinState.Loading) }

        viewModelScope.launch {
            checkinRepository.scan(qrCode).fold(
                onSuccess = { result ->
                    _state.update { it.copy(scanState = CheckinState.Success(result)) }
                },
                onFailure = { error ->
                    _state.update { it.copy(scanState = CheckinState.Error(error.toUserMessage())) }
                },
            )
        }
    }

    fun resetState() {
        _state.update { it.copy(scanState = CheckinState.Idle) }
    }
}
