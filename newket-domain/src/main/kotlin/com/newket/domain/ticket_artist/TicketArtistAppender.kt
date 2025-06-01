package com.newket.domain.ticket_artist

import com.newket.infra.jpa.ticket_artist.entity.MusicalArtist
import com.newket.infra.jpa.ticket_artist.repository.MusicalArtistRepository
import org.springframework.stereotype.Service

@Service
class TicketArtistAppender(private val musicalArtistRepository: MusicalArtistRepository) {
    fun saveAllMusicalArtists(musicalArtists: List<MusicalArtist>) = musicalArtistRepository.saveAll(musicalArtists)
}