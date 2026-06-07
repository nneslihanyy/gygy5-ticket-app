package com.turkcell.data.dto.event

import kotlinx.serialization.Serializable

// Burası dış dünya ile iletişim nesnesi..
@Serializable
data class EventDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val place: String? = null,
    val startsAt: String,
    val endsAt: String? = null,
    val ticketTypes: List<TicketTypeDto> = emptyList(),
)