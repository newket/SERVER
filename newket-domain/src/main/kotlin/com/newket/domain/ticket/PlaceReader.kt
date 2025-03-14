package com.newket.domain.ticket

import com.newket.domain.ticket.exception.PlaceException
import com.newket.infra.jpa.ticket.entity.Place
import com.newket.infra.jpa.ticket.repository.PlaceRepository
import org.springframework.stereotype.Service

@Service
class PlaceReader(
    private val placeRepository: PlaceRepository
) {
    fun findByPlaceName(placeName: String): Place =
        placeRepository.findByPlaceName(placeName) ?: throw PlaceException.PlaceNotFoundException()

    fun findAll() = placeRepository.findAll()
}