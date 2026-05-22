package com.turkcell.data.dto.ticket

import kotlinx.serialization.Serializable

@Serializable
data class MyTicketDto(
    val id: String,
    val qrCode: String,
    val status: String,
    val usedAt: String? = null,
    val checkedInBy: String? = null,
    val ticketType: TicketTypeInfoDto,
)

@Serializable
data class TicketTypeInfoDto(
    val id: String,
    val name: String,
    val priceCents: Long,
    val event: TicketEventDto,
)

@Serializable
data class TicketEventDto(
    val id: String,
    val name: String,
    val place: String? = null,
    val startsAt: String,
)
