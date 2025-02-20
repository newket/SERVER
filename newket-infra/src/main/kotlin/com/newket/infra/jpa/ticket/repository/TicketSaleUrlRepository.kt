package com.newket.infra.jpa.ticket.repository

import com.newket.infra.jpa.ticket.entity.TicketSaleUrl
import org.springframework.data.jpa.repository.JpaRepository

interface TicketSaleUrlRepository : JpaRepository<TicketSaleUrl, Long> {
}