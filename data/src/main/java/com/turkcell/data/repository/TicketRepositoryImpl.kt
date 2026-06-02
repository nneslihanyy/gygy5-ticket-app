package com.turkcell.data.repository

import com.turkcell.core.domain.ticket.Ticket
import com.turkcell.core.domain.ticket.TicketRepository
import com.turkcell.data.mapper.toDomain
import com.turkcell.data.remote.TicketApi
import com.turkcell.data.util.runCatchingApi

class TicketRepositoryImpl(
    private val ticketApi: TicketApi,
) : TicketRepository {
    override suspend fun getMyTickets(): Result<List<Ticket>> =
        runCatchingApi { ticketApi.getMyTickets() }.map { list -> list.map { it.toDomain() } }

    override suspend fun getTicketById(id: String): Result<Ticket> =
        runCatchingApi { ticketApi.getTicketById(id) }.map { it.toDomain() }
}
