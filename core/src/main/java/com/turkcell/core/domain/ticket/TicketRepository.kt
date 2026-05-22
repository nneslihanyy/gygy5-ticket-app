package com.turkcell.core.domain.ticket

interface TicketRepository {
    suspend fun getMyTickets(): Result<List<Ticket>>
}
