package com.newket.infra.jpa.ticket_artist.repository

import com.newket.infra.jpa.ticket_artist.entity.LineupImage
import org.springframework.data.jpa.repository.JpaRepository

interface LineupImageRepository : JpaRepository<LineupImage, Long> {
    fun findByTicketId(ticketId: Long): LineupImage?
}