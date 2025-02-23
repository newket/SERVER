package com.newket.infra.jpa.notification_request.entity

import com.newket.infra.jpa.config.BaseDateEntity
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "artist_notification",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "artist_id"]) // 복합 유니크 제약
    ]
)
class ArtistNotification(
    val userId: Long,
    val artistId: Long
) : BaseDateEntity()