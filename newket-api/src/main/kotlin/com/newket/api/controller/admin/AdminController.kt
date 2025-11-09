package com.newket.api.controller.admin

import com.newket.application.admin.AdminService
import com.newket.application.admin.dto.*
import com.newket.client.crawling.CreateMusicalRequest
import com.newket.client.crawling.CreateTicketRequest
import com.newket.infra.jpa.ticket.constant.Genre
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
class AdminController(private val adminService: AdminService) {
    // 티켓 크롤링 (티켓팅 링크 request)
    @PostMapping(AdminApi.V1.TICKET_FETCH)
    suspend fun fetchTicket(@RequestBody request: TextDto): CreateTicketRequest =
        withContext(Dispatchers.Default) {
            adminService.fetchTicket(request.text)
        }

    @PostMapping(AdminApi.V1.TICKET_FETCH_MUSICAL)
    suspend fun fetchMusical(@RequestBody request: TextDto): CreateMusicalRequest =
        withContext(Dispatchers.Default) {
            adminService.fetchMusical(request.text)
        }

    // 새 티켓 추가
    @PostMapping(AdminApi.V1.TICKET)
    fun createTicketBuffer(@RequestBody createTicketRequest: CreateTicketRequest) {
        return adminService.createTicketBuffer(createTicketRequest)
    }

    @PutMapping(AdminApi.V1.TICKET_DETAIL)
    fun updateTicket(@PathVariable ticketId: Long, @RequestBody createTicketRequest: CreateTicketRequest) {
        return adminService.updateTicket(ticketId, createTicketRequest)
    }

    @GetMapping(AdminApi.V1.TICKET_DETAIL)
    fun getTicket(@PathVariable ticketId: Long): CreateTicketRequest {
        return adminService.getTicket(ticketId)
    }

    @PostMapping(AdminApi.V1.TICKET_MUSICAL)
    fun createMusical(@RequestBody createMusicalRequest: CreateMusicalRequest) {
        return adminService.createMusical(createMusicalRequest)
    }

    @PutMapping(AdminApi.V1.TICKET_MUSICAL_DETAIL)
    fun updateMusical(@PathVariable ticketId: Long, @RequestBody createMusicalRequest: CreateMusicalRequest) {
        return adminService.updateMusical(ticketId, createMusicalRequest)
    }

    @GetMapping(AdminApi.V1.TICKET_MUSICAL_DETAIL)
    fun getMusical(@PathVariable ticketId: Long): CreateMusicalRequest {
        return adminService.getMusical(ticketId)
    }

    // 추가예매 추가
    @PostMapping(AdminApi.V1.TICKET_ADDITIONAL_SALE)
    fun createTicketSaleScheduleBuffer(
        @RequestBody request: AddTicketSaleScheduleRequest,
        @PathVariable ticketSaleUrlId: Long
    ) {
        return adminService.createTicketSaleScheduleBuffer(request, ticketSaleUrlId)
    }

    // 아티스트 추가
    @PostMapping(AdminApi.V1.TICKET_ADDITIONAL_ARTIST)
    fun createTicketArtistBuffer(@RequestBody artists: AddTicketArtistsRequest, @PathVariable ticketId: Long) {
        return adminService.createTicketArtistBuffer(artists, ticketId)
    }

    // 등록 예정 티켓
    @GetMapping(AdminApi.V1.TICKET_BEFORE_SALE)
    fun getBeforeSaleTicket(@PathVariable genre: Genre): List<TicketTableResponse> {
        return adminService.getBeforeSaleTicket(genre)
    }

    // 판매중인 티켓
    @GetMapping(AdminApi.V1.TICKET_ON_SALE)
    fun getOnSaleTicket(@PathVariable genre: Genre): List<TicketTableResponse> {
        return adminService.getOnSaleTicket(genre)
    }

    // 판매 완료 티켓
    @GetMapping(AdminApi.V1.TICKET_AFTER_SALE)
    fun getAfterSaleTicket(@PathVariable genre: Genre): List<TicketTableResponse> {
        return adminService.getAfterSaleTicket(genre)
    }

    @DeleteMapping(AdminApi.V1.TICKET_DETAIL)
    fun deleteTicket(@PathVariable ticketId: Long) {
        return adminService.deleteTicket(ticketId)
    }

    // 아티스트DB
    @GetMapping(AdminApi.V1.ARTIST)
    fun getAllArtists(): List<ArtistTableDto> {
        return adminService.getAllArtist()
    }

    @PutMapping(AdminApi.V1.ARTIST)
    fun putAllArtists(@RequestBody request: List<ArtistTableDto>) {
        return adminService.putAllArtists(request)
    }

    @PostMapping(AdminApi.V1.ARTIST_SEARCH)
    fun searchArtist(@RequestBody request: TextDto): List<CreateTicketRequest.Artist> {
        return adminService.searchArtist(request.text)
    }

    // 아티스트 크롤링
    @PostMapping(AdminApi.V1.ARTIST_FETCH)
    fun fetchArtists(@RequestBody request: TextDto): List<CreateTicketRequest.Artist> {
        return adminService.fetchTicketArtist(request.text)
    }

    // 그룹DB
    @GetMapping(AdminApi.V1.GROUP)
    fun getAllGroups(): List<GroupTableDto> {
        return adminService.getAllGroups()
    }

    @PutMapping(AdminApi.V1.GROUP)
    fun putAllGroups(@RequestBody request: List<GroupTableDto>) {
        return adminService.putAllGroups(request)
    }

    // 장소DB
    @GetMapping(AdminApi.V1.PLACE)
    fun getAllPlaces(): List<PlaceTableDto> {
        return adminService.getAllPlaces()
    }

    @PutMapping(AdminApi.V1.PLACE)
    fun putAllPlaces(@RequestBody request: List<PlaceTableDto>) {
        return adminService.putAllPlaces(request)
    }

    @PostMapping(AdminApi.V1.PLACE_SEARCH)
    fun searchPlace(@RequestBody request: TextDto): List<PlaceTableDto> {
        return adminService.searchPlace(request.text)
    }

    //S3
    @PostMapping(AdminApi.V1.S3)
    fun uploadFile(@RequestParam file: MultipartFile): String {
        return adminService.uploadFile(file)
    }
}