package com.newket.infra.jpa.notifiacation.repository

import com.newket.infra.jpa.notifiacation.entity.ArtistNotification
import org.springframework.data.jpa.repository.JpaRepository

interface ArtistNotificationRepository : JpaRepository<ArtistNotification, Long> {
    fun deleteAllByUserId(userId: Long)

    fun findAllByUserId(userId: Long) : List<ArtistNotification>

    fun findAllByArtistId(artistId: Long) : List<ArtistNotification>

    fun findByUserIdAndArtistId(userId: Long, artistId: Long): ArtistNotification?

    fun deleteByUserIdAndArtistId(userId: Long, artistId: Long)
}