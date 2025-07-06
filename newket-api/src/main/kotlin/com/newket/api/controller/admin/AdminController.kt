package com.newket.api.controller.admin

import com.newket.application.admin.AdminService
import com.newket.application.admin.dto.*
import com.newket.client.crawling.CreateMusicalRequest
import com.newket.client.crawling.CreateTicketRequest
import com.newket.infra.mongodb.ticket_buffer.entity.TicketSaleBuffer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.web.bind.annotation.*

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

    @PostMapping(AdminApi.V1.TICKET_MUSICAL)
    fun createMusical(@RequestBody createMusicalRequest: CreateMusicalRequest) {
        return adminService.createMusical(createMusicalRequest)
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

    // 뮤지컬 티켓 불러오기
    @GetMapping(AdminApi.V1.TICKET_MUSICAL)
    fun getMusical(): List<TicketTableResponse> {
        return adminService.getMusical()
    }

    @DeleteMapping(AdminApi.V1.TICKET_MUSICAL_DETAIL)
    fun deleteMusical(@PathVariable ticketId: Long) {
        return adminService.deleteMusical(ticketId)
    }

    // 아티스트
    @GetMapping(AdminApi.V1.ARTIST)
    fun getAllArtists(): List<ArtistTableDto> {
        return adminService.getAllArtist()
    }

    @PutMapping(AdminApi.V1.ARTIST)
    fun putAllArtists(@RequestBody request: List<ArtistTableDto>) {
        return adminService.putAllArtists(request)
    }

    // 그룹
    @GetMapping(AdminApi.V1.GROUP)
    fun getAllGroups(): List<GroupTableDto> {
        return adminService.getAllGroups()
    }

    @PutMapping(AdminApi.V1.GROUP)
    fun putAllGroups(@RequestBody request: List<GroupTableDto>) {
        return adminService.putAllGroups(request)
    }

    // 장소
    @GetMapping(AdminApi.V1.PLACE)
    fun getAllPlaces(): List<PlaceTableDto> {
        return adminService.getAllPlaces()
    }

    @PutMapping(AdminApi.V1.PLACE)
    fun putAllPlaces(@RequestBody request: List<PlaceTableDto>) {
        return adminService.putAllPlaces(request)
    }
}