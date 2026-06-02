package com.turkcell.ticketapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.core.domain.auth.AuthRepository // Çıkış işlemi için import eklendi
import com.turkcell.core.domain.event.Event
import com.turkcell.core.domain.event.EventRepository
import com.turkcell.core.domain.ticket.Ticket
import com.turkcell.core.domain.ticket.TicketRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isEventsLoading: Boolean = false,
    val events: List<Event> = emptyList(),
    val eventsError: String? = null,

    val isTicketsLoading: Boolean = false,
    val tickets: List<Ticket> = emptyList(),
    val ticketsError: String? = null,

    // --- PULL TO REFRESH İÇİN EKLENEN STATE ---
    val isRefreshing: Boolean = false
)

class HomeViewModel(
    private val eventRepository: EventRepository,
    private val ticketRepository: TicketRepository,
    private val authRepository: AuthRepository // --- Sadece bu bağımlılık eklendi ---
) : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        loadEvents()
        loadTickets()
    }

    fun loadEvents() {
        if (_state.value.isEventsLoading) return

        _state.update { it.copy(isEventsLoading = true, eventsError = null) }

        viewModelScope.launch {
            eventRepository.getEvents().fold(
                onSuccess = { list ->
                    _state.update { it.copy(events = list, isEventsLoading = false, eventsError = null) }
                },
                onFailure = { e ->
                    _state.update { it.copy(isEventsLoading = false, eventsError = e.message ?: "Etkinlikler yüklenemedi.") }
                }
            )
        }
    }

    fun loadTickets() {
        if (_state.value.isTicketsLoading) return

        _state.update { it.copy(isTicketsLoading = true, ticketsError = null) }

        viewModelScope.launch {
            ticketRepository.getMyTickets().fold(
                onSuccess = { list ->
                    // --- SIRALAMA DÜZELTMESİ ---
                    // Son alınan biletlerin (genelde id'si büyük veya listede son olanların)
                    // en üstte görünmesi için listeyi tersten (reversed) kaydediyoruz.
                    _state.update { it.copy(tickets = list.reversed(), isTicketsLoading = false, ticketsError = null) }
                },
                onFailure = { e ->
                    _state.update { it.copy(isTicketsLoading = false, ticketsError = e.message ?: "Biletler yüklenemedi.") }
                }
            )
        }
    }

    // --- PULL TO REFRESH FONKSİYONU ---
    fun refresh() {
        _state.update { it.copy(isRefreshing = true, eventsError = null, ticketsError = null) }

        viewModelScope.launch {
            // İki isteği de asenkron (paralel) başlatıp ikisinin de bitmesini bekliyoruz
            val eventsDeferred = async { eventRepository.getEvents() }
            val ticketsDeferred = async { ticketRepository.getMyTickets() }

            val eventsResult = eventsDeferred.await()
            val ticketsResult = ticketsDeferred.await()

            _state.update { currentState ->
                var updatedState = currentState.copy(isRefreshing = false)

                eventsResult.fold(
                    onSuccess = { list ->
                        updatedState = updatedState.copy(events = list)
                    },
                    onFailure = { e ->
                        updatedState = updatedState.copy(eventsError = e.message ?: "Etkinlikler yüklenemedi.")
                    }
                )

                ticketsResult.fold(
                    onSuccess = { list ->
                        // Burada da yeni biletlerin üste gelmesi için listeyi tersten (reversed) ekliyoruz
                        updatedState = updatedState.copy(tickets = list.reversed())
                    },
                    onFailure = { e ->
                        updatedState = updatedState.copy(ticketsError = e.message ?: "Biletler yüklenemedi.")
                    }
                )

                updatedState
            }
        }
    }

    // --- HOCANIN İSTEDİĞİ LOGOUT FONKSİYONU ---
    // Çıkış butonuna basıldığında tetiklenecek tek yeni fonksiyon
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}