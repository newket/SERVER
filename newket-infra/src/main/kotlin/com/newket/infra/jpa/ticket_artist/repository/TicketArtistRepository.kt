package com.newket.infra.jpa.ticket_artist.repository


import com.newket.infra.jpa.ticket.constant.Genre
import com.newket.infra.jpa.ticket.entity.TicketEventSchedule
import com.newket.infra.jpa.ticket.entity.TicketSaleSchedule
import com.newket.infra.jpa.ticket_artist.entity.TicketArtist
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate
import java.time.LocalTime

interface TicketArtistRepository : JpaRepository<TicketArtist, Long> {
    fun findAllByTicketId(ticketId: Long): List<TicketArtist>

    @Query(
        """
        select s
        from TicketSaleSchedule s
        join TicketArtist ta on ta.ticket.id = s.ticketSaleUrl.ticket.id
        where ta.artist.id = :artistId and (s.day > :date or (s.day=:date and s.time > :time))
        order by s.day, s.time
    """
    )
    fun findAllBeforeSaleByArtistId(artistId: Long, date: LocalDate, time: LocalTime): List<TicketSaleSchedule>

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
        JOIN TicketArtist ca ON ca.ticket.id = c.id
        WHERE ca.artist.id = :artistId 
        AND cs2.day > :date 
        AND ((cts.day = :date AND cts.time <= :time) OR cts.day < :date)
        AND cts.type NOT LIKE '%선예매%'
    )
    """
    )
    fun findAllOnSaleByArtistId(artistId: Long, date: LocalDate, time: LocalTime): List<TicketEventSchedule>

    @Query(
        """
    SELECT cs
    FROM TicketEventSchedule cs
    JOIN TicketArtist ca ON ca.ticket.id = cs.ticket.id
    WHERE ca.artist.id = :artistId
    AND NOT EXISTS (
        SELECT 1 FROM TicketEventSchedule subCs
        WHERE subCs.ticket.id = cs.ticket.id
        AND subCs.day > :date
    )
    order by cs.day desc 
    """
    )
    fun findAllAfterSaleByArtistId(artistId: Long, date: LocalDate): List<TicketEventSchedule>

    @Query(
        """
    SELECT cs
    FROM TicketEventSchedule cs
    JOIN TicketArtist ca ON ca.ticket.id = cs.ticket.id
    WHERE ca.artist.id = :artistId
    AND ca.ticket.genre = :genre
    AND NOT EXISTS (
        SELECT 1 FROM TicketEventSchedule subCs
        WHERE subCs.ticket.id = cs.ticket.id
        AND subCs.day > :date
    )
    order by cs.day desc 
    """
    )
    fun findAllAfterSaleByArtistIdAndGenre(artistId: Long, genre: Genre, date: LocalDate): List<TicketEventSchedule>

    fun deleteAllByTicketId(ticketId: Long)
}