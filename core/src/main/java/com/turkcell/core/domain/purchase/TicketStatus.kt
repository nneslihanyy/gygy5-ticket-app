package com.turkcell.core.domain.purchase


enum class TicketStatus {
    VALID,      // Bilet geçerli ve kullanılabilir
    USED,       // Bilet etkinlik girişinde okutulmuş / kullanılmış
    CANCELLED   // Bilet iptal edilmiş
}