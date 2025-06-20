package com.newket.domain.ticket_cache

import com.newket.infra.mongodb.ticket_cache.repository.TicketCacheRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TicketCacheRemover(private val ticketCacheRepository: TicketCacheRepository) {
    fun deleteAllTicketCache() = ticketCacheRepository.deleteAll()

    fun deleteAllAfterSaleTicketCache() = ticketCacheRepository.deleteAllAfterSaleTicketCache(LocalDateTime.now())

    fun deleteByTicketId(ticketId: Long) = ticketCacheRepository.deleteByTicketId(ticketId)
}