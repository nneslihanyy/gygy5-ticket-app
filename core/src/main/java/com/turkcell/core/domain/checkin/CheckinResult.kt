package com.turkcell.core.domain.checkin

data class CheckinEventInfo(
    val id: String,
    val name: String,
    val venue: String,
    val startsAt: String,
)

data class CheckinResult(
    val ticketId: String,
    val ticketType: String,
    val event: CheckinEventInfo,
    val checkedInAt: String,
)
