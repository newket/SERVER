package com.newket.domain.artist.service

import com.newket.domain.artist.exception.ArtistException
import com.newket.infra.jpa.artist.entity.Artist
import com.newket.infra.jpa.artist.repository.ArtistRepository
import com.newket.infra.jpa.artist.repository.GroupMemberRepository
import com.newket.infra.jpa.notifiacation.repository.ArtistNotificationRepository
import com.newket.infra.jpa.ticket_artist.repository.TicketArtistRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class ArtistReader(
    private val ticketArtistRepository: TicketArtistRepository,
    private val artistRepository: ArtistRepository,
    private val artistNotificationRepository: ArtistNotificationRepository,
    private val groupMemberRepository: GroupMemberRepository,
) {
    fun findAllByTicketId(concertId: Long): List<Artist> =
        ticketArtistRepository.findAllByTicketId(concertId).map { it.artist }

    fun findByName(name: String): Artist =
        artistRepository.findByName(name) ?: throw ArtistException.ArtistNotFoundException()

    fun searchByKeyword(keyword: String): List<Artist> = artistRepository.searchByKeyword(keyword)

    fun autocompleteByKeyword(keyword: String): List<Artist> = artistRepository.autocompleteByKeyword(keyword)

    fun findById(artistId: Long): Optional<Artist> = artistRepository.findById(artistId)

    fun findAllFavoriteArtistsByUserId(userId: Long) = artistNotificationRepository.findAllByUserId(userId)

    fun findAllFavoriteArtistsByArtistId(artistId: Long) = artistNotificationRepository.findAllByArtistId(artistId)

    fun findFavoriteArtistByUserIdAndArtistId(userId: Long, artistId: Long) =
        artistNotificationRepository.findByUserIdAndArtistId(userId, artistId)

    fun findAllGroupsByMemberId(artistId: Long) = groupMemberRepository.findAllByMemberId(artistId)

    fun findAllMembersByGroupId(artistId: Long) = groupMemberRepository.findAllByGroupId(artistId)

    fun findRandomArtists(): List<Artist> {
        val randomIds = artistRepository.findRandomArtistIds()
        return artistRepository.findArtistsByIds(randomIds)
    }
}