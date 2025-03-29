package com.newket.infra.mongodb.ticket_buffer.entity

import com.newket.infra.mongodb.config.BaseTimeMongoEntity
import com.newket.infra.mongodb.ticket_cache.entity.Artist
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "ticket_artist_buffer")
data class TicketArtistBuffer(
    val ticketId: Long,
    val artists: List<Artist>,
) : BaseTimeMongoEntity()