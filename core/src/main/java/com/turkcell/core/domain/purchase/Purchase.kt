package com.turkcell.core.domain.purchase

data class Purchase(
    val id: String,
    val status: PurchaseStatus,
    val totalCents: Long,
    val items: List<PurchaseItem>,
    val ticketIds: List<String> = emptyList(),
)
