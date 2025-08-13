package com.newket.application.ticket.dto

data class TicketResponse(
    val beforeSaleTickets: BeforeSaleTicketsResponse,
    val onSaleTickets: OnSaleResponse,
)