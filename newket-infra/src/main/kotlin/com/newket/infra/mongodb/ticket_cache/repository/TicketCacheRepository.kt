package com.newket.infra.mongodb.ticket_cache.repository

import com.newket.infra.jpa.ticket.constant.Genre
import com.newket.infra.mongodb.ticket_cache.entity.TicketCache
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import java.time.LocalDateTime

interface TicketCacheRepository : MongoRepository<TicketCache, String> {
    // 오픈 예정 티켓 (현재 이후의 ticketSaleSchedules 이 있는 티켓)
    @Query("{ 'ticketSaleSchedules.dateTime' : { \$gte: ?0 } }")
    fun findAllBeforeSaleTicket(currentTime: LocalDateTime, sort: Sort): List<TicketCache>

    // 예매 중인 티켓
    @Query(
        "{'ticketSaleSchedules': {'\$elemMatch': {'type': '일반예매','dateTime': {'\$lt': ?0}}}," +
                "'ticketEventSchedules.dateTime': {'\$gt': ?0}}"
    )
    fun findAllOnSaleTicket(currentTime: LocalDateTime, sort: Sort): List<TicketCache>

    // 오픈 예정 티켓 검색
    @Query(
        "{'\$and': [" +
                "{ '\$or': [" +
                "{ 'title': { '\$regex': ?0, '\$options': 'i' } }, " +
                "{ 'artists.name': { '\$regex': ?0, '\$options': 'i' } }, " +
                "{ 'artists.subName': { '\$regex': ?0, '\$options': 'i' } }, " +
                "{ 'artists.nickname': { '\$regex': ?0, '\$options': 'i' } } " +
                "]}, " +
                "{ 'ticketSaleSchedules.dateTime': { '\$gte': ?1 } }" +
                "]}"
    )
    fun findAllBeforeSaleTicketByKeyword(keyword: String, currentTime: LocalDateTime, pageable: Pageable)
            : List<TicketCache>

    // 예매 중인 티켓 검색
    @Query(
        "{'\$and': [" +
                "{'\$or': [" +
                "{ 'title': { '\$regex': ?0, '\$options': 'i' } }, " +
                "{ 'artists.name': { '\$regex': ?0, '\$options': 'i' } }, " +
                "{ 'artists.subName': { '\$regex': ?0, '\$options': 'i' } }, " +
                "{ 'artists.nickname': { '\$regex': ?0, '\$options': 'i' } }" +
                "]}, " +
                "{'ticketSaleSchedules': {'\$elemMatch': {'type': '일반예매','dateTime': {'\$lt': ?1}}}," +
                "'ticketEventSchedules.dateTime': {'\$gt': ?1}}" +
                "]}"
    )
    fun findAllOnSaleTicketByKeyword(keyword: String, currentTime: LocalDateTime, pageable: Pageable): List<TicketCache>

    // 티켓 검색 자동완성
    @Query(
        "{ '\$or': [" +
                "{ 'title': { '\$regex': ?0, '\$options': 'i' } }, " +
                "{ 'artists.name': { '\$regex': ?0, '\$options': 'i' } }, " +
                "{ 'artists.subName': { '\$regex': ?0, '\$options': 'i' } }, " +
                "{ 'artists.nickname': { '\$regex': ?0, '\$options': 'i' } }" +
                "]}"
    )
    fun findAllTicketByKeyword(keyword: String, pageable: Pageable): List<TicketCache>

    // 아티스트 오픈 예정 티켓 조회
    @Query("{ \$and: [ { 'artists.artistId' : { \$eq: ?0 } }, { 'ticketSaleSchedules.dateTime' : { \$gte: ?1 } } ] }")
    fun findAllBeforeSaleTicketByArtistId(artistId: Long, currentTime: LocalDateTime, sort: Sort): List<TicketCache>

    @Query("{\$and: [ { 'artists.artistId': { \$eq: ?0 } }, { 'genre': { \$eq: ?1 } }, { 'ticketSaleSchedules.dateTime': { \$gte: ?2 } } ] }")
    fun findAllBeforeSaleTicketByArtistIdAndGenre(
        artistId: Long,
        genre: Genre,
        currentTime: LocalDateTime,
        sort: Sort
    ): List<TicketCache>

    // 아티스트 예매 중인 티켓 조회
    @Query(
        "{ \$and: [ { 'artists.artistId' : { \$eq: ?0 } }, " +
                "{'ticketSaleSchedules': {'\$elemMatch': {'type': '일반예매','dateTime': {'\$lt': ?1}}}," +
                "'ticketEventSchedules.dateTime': {'\$gt': ?1}} ] }"
    )
    fun findAllOnSaleTicketByArtistId(artistId: Long, currentTime: LocalDateTime, sort: Sort): List<TicketCache>

    @Query(
        "{ \$and: [ { 'artists.artistId' : { \$eq: ?0 } }, " +
                "{'ticketSaleSchedules': {'\$elemMatch': {'type': '일반예매','dateTime': {'\$lt': ?2}}}," +
                "'ticketEventSchedules.dateTime': {'\$gt': ?2}}," +
                "{ 'genre': { \$eq: ?1 } } ] }"
    )
    fun findAllOnSaleTicketByArtistIdAndGenre(
        artistId: Long,
        genre: Genre,
        currentTime: LocalDateTime,
        sort: Sort
    ): List<TicketCache>

    // 지난 티켓 삭제
    @Query(value = "{'ticketEventSchedules.dateTime': { '\$not': { '\$gt': ?0 } }}", delete = true)
    fun deleteAllAfterSaleTicketCache(currentTime: LocalDateTime): Long

    // 오픈 예정 티켓 by artistIds
    @Query("{ 'artists.artistId': { \$in: ?0 }, 'ticketSaleSchedules.dateTime': { \$gte: ?1 } }")
    fun findAllBeforeSaleTicketByArtistIds(artistIds: List<Long>, currentTime: LocalDateTime): List<TicketCache>

    // 예매 중인 티켓 by artistIds
    @Query(
        "{ 'artists.artistId': { \$in: ?0 }, 'ticketSaleSchedules': {'\$elemMatch': {'type': '일반예매','dateTime': {'\$lt': ?1}}}," +
                "'ticketEventSchedules.dateTime': {'\$gt': ?1}}"
    )
    fun findAllOnSaleTicketByArtistIds(artistIds: List<Long>, currentTime: LocalDateTime, sort: Sort): List<TicketCache>

    // 아티스트 알림받는 오픈 예정 티켓
    @Query("{ 'ticketId': { \$in: ?0 }, 'ticketSaleSchedules.dateTime': { \$gte: ?1 } }")
    fun findAllBeforeSaleTicketByTicketIds(ticketIds: List<Long>, currentTime: LocalDateTime): List<TicketCache>

    // findById
    fun findByTicketId(ticketId: Long): TicketCache?
}