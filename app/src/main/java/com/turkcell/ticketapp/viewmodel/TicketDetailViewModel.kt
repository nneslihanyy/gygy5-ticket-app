package com.turkcell.ticketapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.core.domain.ticket.Ticket
import com.turkcell.core.domain.ticket.TicketRepository
import com.turkcell.ticketapp.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TicketDetailUiState(
    val isLoading: Boolean = true,
    val ticket: Ticket? = null,
    val error: String? = null,
)

class TicketDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val ticketRepository: TicketRepository,
) : ViewModel() {

    private val ticketId: String = savedStateHandle["ticketId"]!!

    private val _state = MutableStateFlow(TicketDetailUiState())
    val state: StateFlow<TicketDetailUiState> = _state.asStateFlow()

    init { loadTicket() }

    fun loadTicket() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            ticketRepository.getTicketById(ticketId).fold(
                onSuccess = { ticket -> _state.update { it.copy(isLoading = false, ticket = ticket) } },
                onFailure = { e -> _state.update { it.copy(isLoading = false, error = e.toUserMessage()) } }
            )
        }
    }
}