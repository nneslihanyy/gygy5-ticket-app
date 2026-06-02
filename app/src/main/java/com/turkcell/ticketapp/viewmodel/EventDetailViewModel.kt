package com.turkcell.ticketapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.core.domain.event.Event
import com.turkcell.core.domain.event.EventRepository
import com.turkcell.core.domain.purchase.PurchaseItem
import com.turkcell.core.domain.purchase.PurchaseRepository
import com.turkcell.ticketapp.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.min

data class EventDetailUiState(
    val isLoading: Boolean = true,
    val event: Event? = null,
    val error: String? = null,
    val quantities: Map<String, Int> = emptyMap(),
    val isPurchasing: Boolean = false,
    val pendingPurchaseId: String? = null,
    val showPaymentDialog: Boolean = false,
    val purchaseSuccess: Boolean = false,
    val purchaseError: String? = null,
) {
    val totalCents: Long
        get() {
            val event = this.event ?: return 0L
            return event.ticketTypes.sumOf { tt ->
                val qty = quantities[tt.id] ?: 0
                tt.priceCents * qty
            }
        }
    val hasSelection: Boolean get() = quantities.values.any { it > 0 }
}

class EventDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val eventRepository: EventRepository,
    private val purchaseRepository: PurchaseRepository,
) : ViewModel() {

    private val eventId: String = savedStateHandle["eventId"]!!

    private val _state = MutableStateFlow(EventDetailUiState())
    val state: StateFlow<EventDetailUiState> = _state.asStateFlow()

    init { loadEvent() }

    fun loadEvent() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            eventRepository.getEventById(eventId).fold(
                onSuccess = { event -> _state.update { it.copy(isLoading = false, event = event) } },
                onFailure = { e -> _state.update { it.copy(isLoading = false, error = e.toUserMessage()) } }
            )
        }
    }

    fun incrementQuantity(ticketTypeId: String) {
        val event = _state.value.event ?: return
        val tt = event.ticketTypes.find { it.id == ticketTypeId } ?: return
        val current = _state.value.quantities[ticketTypeId] ?: 0
        val max = min(20, tt.remaining.toInt())
        if (current < max) {
            _state.update { it.copy(quantities = it.quantities + (ticketTypeId to current + 1)) }
        }
    }

    fun decrementQuantity(ticketTypeId: String) {
        val current = _state.value.quantities[ticketTypeId] ?: 0
        if (current > 0) {
            _state.update { it.copy(quantities = it.quantities + (ticketTypeId to current - 1)) }
        }
    }

    // Step 1: createPurchase -> show payment dialog
    fun startPurchase() {
        val state = _state.value
        if (!state.hasSelection || state.isPurchasing) return

        val items = state.quantities.filter { it.value > 0 }
            .map { PurchaseItem(ticketTypeId = it.key, quantity = it.value) }

        _state.update { it.copy(isPurchasing = true, purchaseError = null) }

        viewModelScope.launch {
            purchaseRepository.createPurchase(items).fold(
                onSuccess = { purchase ->
                    _state.update {
                        it.copy(
                            isPurchasing = false,
                            pendingPurchaseId = purchase.id,
                            showPaymentDialog = true,
                        )
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(isPurchasing = false, purchaseError = e.toUserMessage()) }
                    // 409 capacity_exceeded -> refresh event
                    if (e.toUserMessage().contains("Stok yetersiz")) loadEvent()
                }
            )
        }
    }

    // Step 2: pay -> navigate
    fun confirmPayment() {
        val purchaseId = _state.value.pendingPurchaseId ?: return
        _state.update { it.copy(isPurchasing = true, showPaymentDialog = false) }

        viewModelScope.launch {
            purchaseRepository.pay(purchaseId).fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            isPurchasing = false,
                            purchaseSuccess = true,
                            quantities = emptyMap(),
                            pendingPurchaseId = null,
                        )
                    }
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(isPurchasing = false, purchaseError = e.toUserMessage(), pendingPurchaseId = null)
                    }
                }
            )
        }
    }

    fun dismissPaymentDialog() {
        _state.update { it.copy(showPaymentDialog = false, pendingPurchaseId = null) }
    }

    fun consumePurchaseSuccess() { _state.update { it.copy(purchaseSuccess = false) } }
    fun consumePurchaseError() { _state.update { it.copy(purchaseError = null) } }
}