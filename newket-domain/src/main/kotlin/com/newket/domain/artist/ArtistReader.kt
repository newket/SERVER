package com.newket.domain.artist

import com.newket.domain.artist.exception.ArtistException
import com.newket.infra.jpa.artist.entity.Artist
import com.newket.infra.jpa.artist.repository.ArtistRepository
import com.newket.infra.jpa.artist.repository.GroupMemberRepository
import com.newket.infra.jpa.notification_request.repository.ArtistNotificationRepository
import com.newket.infra.jpa.ticket_artist.entity.TicketArtist
import com.newket.infra.jpa.ticket_artist.repository.TicketArtistRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class ArtistReader(
    private val ticketArtistRepository: TicketArtistRepository,
    private val artistRepository: ArtistRepository,
    private val artistNotificationRepository: ArtistNotificationRepository,
    private val groupMemberRepository: GroupMemberRepository,
) {
    fun findAllTicketArtistsByTicketId(ticketId: Long): List<TicketArtist> =
        ticketArtistRepository.findAllByTicketId(ticketId)

    fun searchByKeyword(keyword: String): List<Artist> =
        artistRepository.searchByKeyword(keyword, PageRequest.of(0, 10))

    fun autocompleteByKeyword(keyword: String): List<Artist> =
        artistRepository.autocompleteByKeyword(keyword, PageRequest.of(0, 3))

    fun findById(artistId: Long): Artist =
        artistRepository.findById(artistId).orElseThrow { ArtistException.ArtistNotFoundException() }

    fun findAllFavoriteArtistsByArtistId(artistId: Long) = artistNotificationRepository.findAllByArtistId(artistId)

    fun findAllGroups() = groupMemberRepository.findAll()

    fun findAllGroupsByMemberId(artistId: Long) = groupMemberRepository.findAllByMemberId(artistId)

    fun findAllMembersByGroupId(artistId: Long) = groupMemberRepository.findAllByGroupId(artistId)

    fun findRandomArtists(): List<Artist> {
        val randomIds = artistRepository.findRandomArtistIds(PageRequest.of(0, 10))
        return artistRepository.findArtistsByIds(randomIds)
    }

    fun findAll(): List<Artist> = artistRepository.findAll()
}