package com.turkcell.data.mapper

import com.turkcell.core.domain.checkin.CheckinEventInfo
import com.turkcell.core.domain.checkin.CheckinResult
import com.turkcell.data.dto.checkin.ScanResponseDto

internal fun ScanResponseDto.toDomain(): CheckinResult = CheckinResult(
    ticketId = ticketId,
    ticketType = ticketType,
    event = CheckinEventInfo(
        id = event.id,
        name = event.name,
        venue = event.venue,
        startsAt = event.startsAt,
    ),
    checkedInAt = checkedInAt,
)
