package com.turkcell.core.domain.ticket

data class Ticket(
    val id: String,
    val qrCode: String,
    val status: String,
    val usedAt: String?,
    val eventName: String,
    val eventVenue: String,
    val eventStartsAt: String,
    val ticketTypeName: String,
    val priceCents: Long,
)
