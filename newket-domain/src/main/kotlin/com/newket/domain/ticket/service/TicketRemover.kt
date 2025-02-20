package com.newket.domain.ticket.service

import com.newket.infra.jpa.ticket.repository.TicketRepository
import org.springframework.stereotype.Service

@Service
class TicketRemover (
    val ticketRepository: TicketRepository
){
    fun deleteByTicketId(ticketId: Long) = ticketRepository.deleteById(ticketId)
}