package com.turkcell.data.purchase

import kotlinx.serialization.Serializable

@Serializable
data class TicketDto(
    val id: String,
    val purchaseId: String,
    val ticketTypeId: String,
    val status: String
)