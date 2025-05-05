package com.newket.api.controller.admin

import com.newket.application.admin.AdminService
import com.newket.application.admin.dto.*
import com.newket.client.crawling.CreateTicketRequest
import com.newket.infra.mongodb.ticket_buffer.entity.TicketSaleBuffer
import org.springframework.web.bind.annotation.*

@RestController
class AdminController(private val adminService: AdminService) {
    // 티켓 크롤링 (티켓팅 링크 request)
    @PostMapping(AdminApi.V1.TICKET_FETCH)
    fun fetchTicket(@RequestBody request: TextDto): CreateTicketRequest {
        return adminService.fetchTicket(request.text)
    }

    // 아티스트 크롤링 (아티스트 설명 글 request)
    @PostMapping(AdminApi.V1.ARTIST_FETCH)
    fun fetchArtists(@RequestBody request: TextDto): List<CreateTicketRequest.Artist> {
        return adminService.fetchTicketArtist(request.text)
    }

    // 아티스트 자동완성
    @PostMapping(AdminApi.V1.ARTIST_SEARCH)
    fun searchArtist(@RequestBody request: TextDto): List<CreateTicketRequest.Artist> {
        return adminService.searchArtist(request.text)
    }

    // 새 티켓 추가
    @PostMapping(AdminApi.V1.TICKET)
    fun createTicketBuffer(@RequestBody createTicketRequest: CreateTicketRequest) {
        return adminService.createTicketBuffer(createTicketRequest)
    }

    // 추가 예매
    @PostMapping(AdminApi.V1.TICKET_SALE)
    fun createTicketSaleBuffer(@RequestBody ticketSaleBuffer: TicketSaleBuffer) {
        return adminService.createTicketSaleBuffer(ticketSaleBuffer)
    }

    // 아티스트 추가
    @PostMapping(AdminApi.V1.TICKET_ARTIST)
    fun createTicketArtistBuffer(@RequestBody artists: AddTicketArtistsRequest) {
        return adminService.createTicketArtistBuffer(artists)
    }

    // 버퍼에 있는 티켓
    @GetMapping(AdminApi.V1.TICKET_BUFFER)
    fun getTicketBuffer(): List<TicketTableResponse> {
        return adminService.getTicketBuffer()
    }

    @DeleteMapping(AdminApi.V1.TICKET_DETAIL)
    fun deleteTicketBuffer(@PathVariable ticketId: Long) {
        return adminService.deleteTicketBuffer(ticketId)
    }

    // 아티스트 불러오기
    @GetMapping(AdminApi.V1.ARTIST)
    fun getAllArtists(): List<ArtistTableDto> {
        return adminService.getAllArtist()
    }

    // 아티스트 전체 수정
    @PutMapping(AdminApi.V1.ARTIST)
    fun putAllArtists(@RequestBody request: List<ArtistTableDto>) {
        return adminService.putAllArtists(request)
    }

    @GetMapping(AdminApi.V1.PLACE)
    fun getAllPlaces(): List<PlaceTableDto> {
        return adminService.getAllPlaces()
    }

    @PutMapping(AdminApi.V1.PLACE)
    fun putAllPlaces(@RequestBody request: List<PlaceTableDto>) {
        return adminService.putAllPlaces(request)
    }
}