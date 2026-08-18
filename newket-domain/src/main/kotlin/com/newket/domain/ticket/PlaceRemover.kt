package com.newket.domain.ticket

import com.newket.infra.jpa.ticket.entity.Place
import com.newket.infra.jpa.ticket.repository.PlaceRepository
import org.springframework.stereotype.Service

@Service
class PlaceRemover(
    private val placeRepository: PlaceRepository
) {
    fun deletePlaces(places: List<Place>) = placeRepository.deleteAll(places)
}