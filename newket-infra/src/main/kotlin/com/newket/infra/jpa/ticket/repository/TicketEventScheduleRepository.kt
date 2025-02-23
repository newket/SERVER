package com.newket.infra.jpa.ticket.repository

import com.newket.infra.jpa.ticket.entity.TicketEventSchedule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate
import java.time.LocalTime

interface TicketEventScheduleRepository : JpaRepository<TicketEventSchedule, Long> {
    fun findAllByTicketId(ticketId: Long): List<TicketEventSchedule>

    @Query(
        """
    SELECT DISTINCT cs
    FROM TicketEventSchedule cs
    WHERE cs.ticket.id IN (
        SELECT DISTINCT c.id
        FROM Ticket c
        JOIN TicketEventSchedule cs2 ON c.id = cs2.ticket.id
        JOIN TicketSaleUrl ctp ON c.id = ctp.ticket.id
        JOIN TicketSaleSchedule cts ON ctp.id = cts.ticketSaleUrl.id
        WHERE cs2.day > :date 
        AND ((cts.day = :date AND cts.time <= :time) OR cts.day < :date)
        AND cts.type NOT LIKE '%선예매%'
    )
    ORDER BY cs.day
    """
    )
    fun findAllOnSaleOrderByDay(date: LocalDate, time: LocalTime): List<TicketEventSchedule>

    @Query(
        """
    SELECT DISTINCT cs
    FROM TicketEventSchedule cs
    WHERE cs.ticket.id IN (
        SELECT DISTINCT c.id
        FROM Ticket c
        JOIN TicketEventSchedule cs2 ON c.id = cs2.ticket.id
        JOIN TicketSaleUrl ctp ON c.id = ctp.ticket.id
        JOIN TicketSaleSchedule cts ON ctp.id = cts.ticketSaleUrl.id
        WHERE cs2.day > :date 
        AND ((cts.day = :date AND cts.time <= :time) OR cts.day < :date)
        AND cts.type NOT LIKE '%선예매%'
    )
    ORDER BY cs.ticket.id desc
    """
    )
    fun findAllOnSaleOrderById(date: LocalDate, time: LocalTime): List<TicketEventSchedule>

    @Query(
        """
    SELECT DISTINCT cs
    FROM TicketEventSchedule cs
    WHERE cs.ticket.id IN (
        SELECT DISTINCT c.id
        FROM Ticket c
        JOIN TicketEventSchedule tes ON c.id = tes.ticket.id
        JOIN TicketSaleUrl ctp ON c.id = ctp.ticket.id
        JOIN TicketSaleSchedule cts ON ctp.id = cts.ticketSaleUrl.id
        WHERE tes.day > :date 
    )
    ORDER BY cs.ticket.id desc
    """
    )
    fun findAllSellingTicket(date: LocalDate): List<TicketEventSchedule>
}