package com.newket.infra.jpa.ticket_artist.repository


import com.newket.infra.jpa.ticket.constant.Genre
import com.newket.infra.jpa.ticket.entity.TicketEventSchedule
import com.newket.infra.jpa.ticket_artist.entity.TicketArtist
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface TicketArtistRepository : JpaRepository<TicketArtist, Long> {
    fun findAllByTicketId(ticketId: Long): List<TicketArtist>

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

    @Query(
        """
    SELECT ta
    FROM TicketArtist ta
    JOIN FETCH ta.artist
    WHERE ta.ticket.id IN :ticketIds
"""
    )
    fun findAllTicketArtistsByTicketIdIn(ticketIds: List<Long>): List<TicketArtist>

    fun deleteAllByTicketId(ticketId: Long): List<TicketArtist>
}