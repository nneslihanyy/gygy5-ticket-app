package com.turkcell.ticketapp.navigation

import kotlinx.serialization.Serializable

@Serializable object Login
@Serializable object Register
@Serializable object Home
@Serializable object AdminHome
@Serializable object StaffHome
@Serializable object Checkin
@Serializable data class EventDetail(val eventId: String)
@Serializable object MyTickets
@Serializable data class TicketDetail(val ticketId: String)