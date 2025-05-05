package com.newket.infra.jpa.ticket.entity

import com.newket.infra.jpa.config.BaseDateEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity

@Entity
class Place(
    @Column(unique = true)
    var placeName: String,
    var url: String
) : BaseDateEntity()