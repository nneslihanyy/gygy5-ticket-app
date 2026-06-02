package com.turkcell.data.purchase

import kotlinx.serialization.Serializable

@Serializable
data class PurchaseItemDto(
    val ticketTypeId: String,
    val quantity: Int,
    val unitPriceCents: Long
)