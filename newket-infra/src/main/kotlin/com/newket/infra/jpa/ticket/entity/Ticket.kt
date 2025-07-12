package com.newket.infra.jpa.ticket.entity

import com.newket.infra.jpa.config.BaseDateEntity
import com.newket.infra.jpa.ticket.constant.Genre
import jakarta.persistence.*

@Entity
class Ticket(
    @ManyToOne(fetch = FetchType.LAZY)
    var place: Place,
    var title: String,
    var imageUrl: String,
    @Enumerated(EnumType.STRING)
    val genre: Genre,
) : BaseDateEntity()