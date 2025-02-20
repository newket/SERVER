package com.newket.api.controller.ticket

import com.newket.application.ticket.TicketService
import com.newket.application.ticket.dto.*
import org.springframework.web.bind.annotation.*

@RestController
class TicketController(
    private val ticketService: TicketService
) {

    @PostMapping(TicketApi.V1.BASE_URL)
    fun createTicket(@RequestBody createTicketRequest: CreateTicket.Request) {
        return ticketService.createTicket(createTicketRequest)
    }

    @DeleteMapping(TicketApi.V1.TICKET_DETAIL)
    fun deleteTicketBuffer(@PathVariable ticketId: Long) {
        return ticketService.deleteTicketBuffer(ticketId)
    }

    @GetMapping(TicketApi.V1.OPEN)
    fun openingNotice(
        @RequestParam(required = false, defaultValue = "day", value = "orderby") criteria: String
    ): OpeningNotice.Response {
        return ticketService.openingNotice(criteria)
    }

    @GetMapping(TicketApi.V1.ON_SALE)
    fun onSale(
        @RequestParam(required = false, defaultValue = "day", value = "orderby") criteria: String
    ): OnSale.Response {
        return ticketService.onSale(criteria)
    }

    //V2 티켓 상세
    @GetMapping(TicketApi.V2.TICKET_DETAIL)
    fun ticketDetailV2(@PathVariable concertId: Long): TicketDetail.V2.Response {
        return ticketService.ticketDetailV2(concertId)
    }

    //V1 티켓 상세
    @GetMapping(TicketApi.V1.TICKET_DETAIL)
    fun ticketDetail(@PathVariable ticketId: Long): TicketDetail.V1.Response {
        return ticketService.ticketDetail(ticketId)
    }

    //공연명+아티스트로 검색
    @GetMapping(TicketApi.V2.SEARCH)
    fun searchArtistsAndTickets(@RequestParam keyword: String): Search.Response {
        return ticketService.searchArtistsAndTickets(keyword)
    }

    //공연명+아티스트로 검색
    @GetMapping(TicketApi.V1.SEARCH)
    fun searchResult(@RequestParam keyword: String): SearchResult.Response {
        return ticketService.searchResult(keyword)
    }

    //공연명+아티스트로 검색 자동완성
    @GetMapping(TicketApi.V1.AUTOCOMPLETE)
    fun autocomplete(@RequestParam keyword: String): Autocomplete.Response {
        return ticketService.autocomplete(keyword)
    }

    @GetMapping(TicketApi.V1.FAVORITE)
    fun getFavoriteArtistOpeningNotices(): FavoriteArtistOpeningNotice.Response {
        return ticketService.getFavoriteArtistOpeningNotices()
    }
}