package com.newket.infra.jpa.ticket.repository

import com.newket.infra.jpa.ticket.constant.Genre
import com.newket.infra.jpa.ticket.entity.Ticket
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface TicketRepository : JpaRepository<Ticket, Long> {

    @Query(
        """
    SELECT t
    FROM Ticket t
    JOIN FETCH t.place
    JOIN TicketEventSchedule cs ON t.id = cs.ticket.id
    WHERE cs.ticket.genre = :genre
    AND NOT EXISTS (
        SELECT 1 FROM TicketEventSchedule subCs
        WHERE subCs.ticket.id = cs.ticket.id
        AND subCs.day > :date
    )
    order by cs.ticket.id desc 
        """
    )
    fun findAllAfterSaleTicketByGenre(genre: Genre, date: LocalDate): List<Ticket>

    @Query("""
    select t
    from Ticket t
    join fetch t.place
    where t.id in :ticketIds
""")
    fun findAllByIdsWithPlace(ticketIds: List<Long>): List<Ticket>
}