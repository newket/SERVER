package com.newket.domain.ticket_cache.service

import com.newket.infra.mongodb.ticket_cache.entity.TicketCache
import com.newket.infra.mongodb.ticket_cache.repository.TicketCacheRepository
import org.springframework.stereotype.Service

@Service
class TicketCacheAppender(
    private val ticketCacheRepository: TicketCacheRepository
) {
    fun saveTicketCache(ticketCache: TicketCache) = ticketCacheRepository.save(ticketCache)

    fun saveAllTicketCache(ticketCaches: List<TicketCache>) = ticketCacheRepository.saveAll(ticketCaches)
}