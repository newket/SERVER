package com.newket.domain.artist.service

import com.newket.infra.jpa.notifiacation.repository.ArtistNotificationRepository
import org.springframework.stereotype.Service

@Service
class ArtistRemover(
    private val artistNotificationRepository: ArtistNotificationRepository
) {
    fun deleteAllUserFavoriteArtists(userId: Long) {
        artistNotificationRepository.deleteAllByUserId(userId)
    }

    fun deleteUserFavoriteArtistByUserId(userId: Long, artistId: Long) {
        artistNotificationRepository.deleteByUserIdAndArtistId(userId, artistId)
    }
}