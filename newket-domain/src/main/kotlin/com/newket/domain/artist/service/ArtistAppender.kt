package com.newket.domain.artist.service

import com.newket.infra.jpa.notifiacation.entity.ArtistNotification
import com.newket.infra.jpa.notifiacation.repository.ArtistNotificationRepository
import org.springframework.stereotype.Service

@Service
class ArtistAppender(
    private val artistNotificationRepository: ArtistNotificationRepository,
) {
    fun addUserFavoriteArtist(artistNotification: ArtistNotification) =
        artistNotificationRepository.save(artistNotification)
}