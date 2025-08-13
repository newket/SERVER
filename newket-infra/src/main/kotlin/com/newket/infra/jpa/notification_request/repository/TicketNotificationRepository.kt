package com.newket.infra.jpa.notification_request.repository

import com.newket.infra.jpa.notification_request.entity.TicketNotification
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate
import java.time.LocalTime

interface TicketNotificationRepository : JpaRepository<TicketNotification, Long> {
    fun findByTicketIdAndUserId(ticketId: Long, userId: Long): TicketNotification?

    fun deleteByTicketIdAndUserId(ticketId: Long, userId: Long)

    fun findAllByTicketId(ticketId: Long): List<TicketNotification>

    fun findAllByUserId(userId: Long): List<TicketNotification>

    fun deleteAllByTicketIdInAndUserId(ticketId: List<Long>, userId: Long)

    @Query(
        """
    SELECT tn.ticket_id
    FROM ticket_notification tn
    WHERE tn.ticket_id IN (
        SELECT tsu.ticket_id
        FROM ticket_sale_url tsu
        JOIN ticket_sale_schedule tss ON tsu.id = tss.ticket_sale_url_id
        WHERE tss.day > :date OR (tss.day = :date AND tss.time > :time)
    )
    GROUP BY tn.ticket_id
    ORDER BY COUNT(tn.ticket_id) DESC
    LIMIT 5
    """,
        nativeQuery = true
    )
    fun findTop5BeforeSaleTicketIds(date: LocalDate, time: LocalTime): List<Long>

    @Query(
        """
    SELECT tn.ticket_id
    FROM ticket_notification tn
    WHERE tn.ticket_id IN (SELECT tsu.ticket_id
       FROM ticket_sale_url tsu
        JOIN ticket_sale_schedule tss ON tsu.id = tss.ticket_sale_url_id
        JOIN ticket_event_schedule tes ON tes.ticket_id = tn.ticket_id
       WHERE tes.day > :date
         AND ((tss.day = :date AND tss.time <= :time) or tss.day < :date)
         AND tss.type NOT LIKE '%선예매%')
    GROUP BY tn.ticket_id
    ORDER BY COUNT(tn.ticket_id) DESC
    """,
        nativeQuery = true
    )
    fun findTop5OnSaleTicketIds(date: LocalDate, time: LocalTime, pageable: Pageable): List<Long>
}