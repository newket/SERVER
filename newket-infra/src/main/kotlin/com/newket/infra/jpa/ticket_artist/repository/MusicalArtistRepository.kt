package com.newket.infra.jpa.ticket_artist.repository

import com.newket.infra.jpa.ticket_artist.entity.MusicalArtist
import org.springframework.data.jpa.repository.JpaRepository

interface MusicalArtistRepository : JpaRepository<MusicalArtist, Long> {
    fun findByTicketArtistId(ticketArtistId: Long): MusicalArtist?
}