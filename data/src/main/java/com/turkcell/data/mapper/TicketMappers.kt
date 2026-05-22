package com.turkcell.data.mapper

import com.turkcell.core.domain.ticket.Ticket
import com.turkcell.data.dto.ticket.MyTicketDto

internal fun MyTicketDto.toDomain(): Ticket = Ticket(
    id = id,
    qrCode = qrCode,
    status = status,
    usedAt = usedAt,
    eventName = ticketType.event.name,
    eventVenue = ticketType.event.place.orEmpty(),
    eventStartsAt = ticketType.event.startsAt,
    ticketTypeName = ticketType.name,
    priceCents = ticketType.priceCents,
)
