package com.newket.domain.artist.exception

import com.newket.infra.jpa.artist.entity.Artist
import com.newket.infra.jpa.artist.repository.ArtistRepository
import org.springframework.stereotype.Service

@Service
class ArtistRemover(
    private val artistRepository: ArtistRepository
) {
    fun deleteAll(artists: List<Artist>){
        artistRepository.deleteAll(artists)
    }
}