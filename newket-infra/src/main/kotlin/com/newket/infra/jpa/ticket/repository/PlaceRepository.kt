package com.newket.infra.jpa.ticket.repository

import com.newket.infra.jpa.ticket.entity.Place
import org.springframework.data.jpa.repository.JpaRepository

interface PlaceRepository : JpaRepository<Place, Long> {
    fun findByPlaceName(placeName: String): Place?
}