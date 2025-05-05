package com.newket.domain.ticket

import com.newket.infra.jpa.ticket.entity.Place
import com.newket.infra.jpa.ticket.repository.PlaceRepository
import org.springframework.stereotype.Service

@Service
class PlaceAppender(private val placeRepository: PlaceRepository) {
    fun saveAll(placeList: List<Place>) {
        placeRepository.saveAll(placeList)
    }
}