package com.newket.infra.jpa.ticket.repository

import com.newket.infra.jpa.ticket.entity.TicketPrice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface TicketPriceRepository : JpaRepository<TicketPrice, Long> {
    fun findAllByTicketId(ticketId: Long): List<TicketPrice>

    @Query("""
    SELECT tp
    FROM TicketPrice tp
    JOIN FETCH tp.ticket t
    WHERE t.id IN :ticketIds
""")
    fun findAllByTicketIdIn(ticketIds: List<Long>): List<TicketPrice>

    fun deleteAllByTicketId(ticketId: Long)
}