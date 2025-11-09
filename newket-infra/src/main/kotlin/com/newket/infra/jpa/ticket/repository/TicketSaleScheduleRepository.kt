package com.newket.infra.jpa.ticket.repository

import com.newket.infra.jpa.ticket.entity.TicketSaleSchedule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate
import java.time.LocalTime

interface TicketSaleScheduleRepository : JpaRepository<TicketSaleSchedule, Long> {

    @Query(
        """
        select s
        from TicketSaleSchedule s
        join TicketSaleUrl u on u.id=s.ticketSaleUrl.id
        where u.ticket.id = :ticketId
    """
    )
    fun findAllByTicketId(ticketId: Long): List<TicketSaleSchedule>

    @Query(
        """
    SELECT tss
    FROM TicketSaleSchedule tss
    JOIN FETCH tss.ticketSaleUrl tsu
    WHERE tsu.ticket.id IN :ticketIds
"""
    )
    fun findAllByTicketSaleUrlTicketIdIn(ticketIds: List<Long>): List<TicketSaleSchedule>

    @Query(
        """
        select s
        from TicketSaleSchedule s
        join Ticket t on t.id=s.ticketSaleUrl.ticket.id
        join TicketNotification n on n.ticketId=t.id
        where s.day = :date and hour(s.time) = hour(:time)
        order by s.ticketSaleUrl.ticket.id desc
    """
    )
    fun findAllTicketSaleScheduleByDateAndTime(date: LocalDate, time: LocalTime): List<TicketSaleSchedule>
}