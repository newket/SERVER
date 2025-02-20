package com.newket.infra.jpa.user.entity

import com.newket.infra.jpa.config.BaseDateEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "user_device",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "token"]) // 복합 유니크 제약
    ]
)
class UserDevice(
    val userId: Long,
    @Column(
        length = 500,
        nullable = false,
    )
    val token: String,
    var artistNotification: Boolean,
    var ticketNotification: Boolean
) : BaseDateEntity() {
    fun updateArtistNotification(artistNotification: Boolean) {
        this.artistNotification = artistNotification
    }

    fun updateTicketNotification(ticketNotification: Boolean) {
        this.ticketNotification = ticketNotification
    }
}