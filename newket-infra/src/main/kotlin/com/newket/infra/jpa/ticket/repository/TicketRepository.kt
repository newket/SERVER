package com.newket.infra.jpa.ticket.repository

import com.newket.infra.jpa.ticket.entity.Ticket
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate
import java.time.LocalTime

interface TicketRepository : JpaRepository<Ticket, Long> {
    @Query(
        """
        SELECT DISTINCT c
        FROM Ticket c
        JOIN TicketEventSchedule cs ON c.id = cs.ticket.id
        JOIN TicketSaleUrl ctp ON c.id = ctp.ticket.id
        JOIN TicketSaleSchedule cts on ctp.id = cts.ticketSaleUrl.id
        JOIN TicketArtist ca on ca.ticket.id = c.id
        WHERE cs.day > :date 
        AND ((cts.day = :date AND cts.time <= :time) or cts.day < :date)
        AND cts.type NOT LIKE '%선예매%'
        AND (c.title like concat('%', :keyword, '%') or ca.artist.name like concat('%', :keyword, '%') or ca.artist.subName like concat('%', :keyword, '%') or ca.artist.nickname like concat('%', :keyword, '%'))
        order by c.title
        limit 10
    """
    )
    fun findAllOnSaleContainsKeyword(date: LocalDate, time: LocalTime, keyword: String): List<Ticket>

    @Query(
        """
        SELECT DISTINCT c
        FROM Ticket c
        JOIN TicketEventSchedule cs ON c.id = cs.ticket.id
        JOIN TicketArtist ca ON ca.ticket.id = c.id
        JOIN Artist a ON ca.artist.id = a.id
        WHERE cs.day > CURRENT DATE
        AND (ca.artist.name LIKE CONCAT('%', :keyword, '%') or ca.artist.subName LIKE CONCAT('%', :keyword, '%') or ca.artist.nickname LIKE CONCAT('%', :keyword, '%') OR (c.title LIKE CONCAT('%', :keyword, '%')))
        order by c.title
        LIMIT 3
    """
    )
    fun autocompleteByKeyword(keyword: String): List<Ticket>
}