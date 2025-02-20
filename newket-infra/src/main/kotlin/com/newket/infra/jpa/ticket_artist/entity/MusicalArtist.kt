package com.newket.infra.jpa.ticket_artist.entity

import com.newket.infra.jpa.config.BaseDateEntity
import jakarta.persistence.Entity

@Entity
class MusicalArtist(
    val ticketArtistId: Long,
    val role: String,
) : BaseDateEntity()