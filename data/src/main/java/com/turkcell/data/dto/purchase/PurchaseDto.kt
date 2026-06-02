package com.turkcell.data.dto.purchase

import kotlinx.serialization.Serializable

@Serializable
data class PurchaseItemRequestDto(
    val ticketTypeId: String,
    val quantity: Int,
)

@Serializable
data class CreatePurchaseRequestDto(
    val items: List<PurchaseItemRequestDto>,
)

@Serializable
data class PurchaseDto(
    val id: String,
    val userId: String,
    val status: String,
    val totalCents: Long,
    val createdAt: String,
    val paidAt: String? = null,
    val items: List<PurchaseItemDto> = emptyList(),
    val tickets: List<PurchaseTicketDto> = emptyList(),
)

@Serializable
data class PurchaseItemDto(
    val id: String,
    val ticketTypeId: String,
    val quantity: Int,
    val unitPriceCents: Long,
)

@Serializable
data class PurchaseTicketDto(
    val id: String,
    val qrCode: String,
    val status: String,
    val ticketTypeId: String,
)