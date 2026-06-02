package com.turkcell.ticketapp.viewmodel

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

data class MyTicketsUiState(
    val isLoading: Boolean = false,
    val tickets: List<Ticket> = emptyList(),
    val error: String? = null,
    val isRefreshing: Boolean = false,
) {
    val isEmpty: Boolean get() = !isLoading && error == null && tickets.isEmpty()
}

class MyTicketsViewModel(
    private val ticketRepository: TicketRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(MyTicketsUiState())
    val state: StateFlow<MyTicketsUiState> = _state.asStateFlow()

    init { loadTickets() }

    fun loadTickets() {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            ticketRepository.getMyTickets().fold(
                onSuccess = { list -> _state.update { it.copy(tickets = list, isLoading = false) } },
                onFailure = { e -> _state.update { it.copy(isLoading = false, error = e.toUserMessage()) } }
            )
        }
    }

    fun refresh() {
        _state.update { it.copy(isRefreshing = true, error = null) }
        viewModelScope.launch {
            ticketRepository.getMyTickets().fold(
                onSuccess = { list -> _state.update { it.copy(tickets = list, isRefreshing = false) } },
                onFailure = { e -> _state.update { it.copy(isRefreshing = false, error = e.toUserMessage()) } }
            )
        }
    }
}
