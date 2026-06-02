package com.turkcell.core.domain.purchase


data class Ticket(
    val id: String,
    val purchaseId: String,
    val ticketTypeId: String,
    val status: TicketStatus,
    val qrCodeUrl: String? = null // Eğer bilet için QR kod veya barkod linki gerekecekse opsiyonel olarak dursun
)