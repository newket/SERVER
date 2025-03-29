package com.newket.api.controller.ticket

import com.newket.application.ticket.TicketService
import com.newket.application.ticket.dto.*
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class TicketController(
    private val ticketService: TicketService
) {
    // 오픈 예정 티켓
    @GetMapping(TicketApi.V1.BEFORE_SALE)
    fun getBeforeSaleTickets(
        @RequestParam(required = false, defaultValue = "day", value = "orderby") criteria: String
    ): BeforeSaleTicketsResponse {
        return ticketService.getBeforeSaleTickets(criteria)
    }

    // 예매 중인 티켓
    @GetMapping(TicketApi.V1.ON_SALE)
    fun getOnSaleTickets(
        @RequestParam(required = false, defaultValue = "day", value = "orderby") criteria: String
    ): OnSaleResponse {
        return ticketService.getOnSaleTickets(criteria)
    }

    // 티켓 상세
    @GetMapping(TicketApi.V1.TICKET_DETAIL)
    fun getTicketDetail(@PathVariable ticketId: Long): TicketDetailResponse {
        return ticketService.getTicketDetail(ticketId)
    }

    // 공연명+아티스트로 검색
    @GetMapping(TicketApi.V1.SEARCH)
    fun searchResult(@RequestParam keyword: String): SearchResultResponse{
        return ticketService.searchResult(keyword)
    }

    // 공연명+아티스트로 검색 자동완성
    @GetMapping(TicketApi.V1.AUTOCOMPLETE)
    fun autocomplete(@RequestParam keyword: String): AutocompleteResponse {
        return ticketService.autocomplete(keyword)
    }
}