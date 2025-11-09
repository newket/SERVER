package com.newket.infra.jpa.ticket.repository

import com.newket.infra.jpa.ticket.entity.TicketEventSchedule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface TicketEventScheduleRepository : JpaRepository<TicketEventSchedule, Long> {
    fun findAllByTicketId(ticketId: Long): List<TicketEventSchedule>

    @Query(
        """
    SELECT tes
    FROM TicketEventSchedule tes
    JOIN FETCH tes.ticket t
    WHERE t.id IN :ticketIds
"""
    )
    fun findAllByTicketIdIn(ticketIds: List<Long>): List<TicketEventSchedule>

    fun deleteAllByTicketId(ticketId: Long)
}