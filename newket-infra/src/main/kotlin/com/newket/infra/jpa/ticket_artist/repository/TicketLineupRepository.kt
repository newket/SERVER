package com.newket.infra.jpa.ticket_artist.repository

import com.newket.infra.jpa.ticket_artist.entity.TicketLineup
import org.springframework.data.jpa.repository.JpaRepository

interface TicketLineupRepository : JpaRepository<TicketLineup, Long> {
    fun findByTicketId(ticketId: Long): TicketLineup?

    fun deleteByTicketId(ticketId: Long)
}