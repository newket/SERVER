package com.newket.domain.ticket_cache

import com.newket.infra.jpa.ticket.constant.Genre
import com.newket.infra.mongodb.ticket_cache.entity.TicketCache
import com.newket.infra.mongodb.ticket_cache.repository.TicketCacheRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TicketCacheReader(
    private val ticketCacheRepository: TicketCacheRepository
) {
    fun findByTicketId(ticketId: Long) = ticketCacheRepository.findByTicketId(ticketId)

    //오픈 예정 티켓
    fun findAllBeforeSaleTicketOrderById() =
        ticketCacheRepository.findAllBeforeSaleTicket(LocalDateTime.now(), Sort.by(Sort.Order.desc("ticketId")))

    fun findAllBeforeSaleTicketByGenreOrderById(genre: Genre): List<TicketCache> {
        if (genre == Genre.CONCERT_FESTIVAL) {
            val concerts = ticketCacheRepository.findAllBeforeSaleTicketByGenre(
                Genre.CONCERT,
                LocalDateTime.now(),
                Sort.by(Sort.Order.desc("ticketId"))
            )
            val festivals = ticketCacheRepository.findAllBeforeSaleTicketByGenre(
                Genre.FESTIVAL,
                LocalDateTime.now(),
                Sort.by(Sort.Order.desc("ticketId"))
            )
            return concerts + festivals
        }
        return ticketCacheRepository.findAllBeforeSaleTicketByGenre(
            genre,
            LocalDateTime.now(),
            Sort.by(Sort.Order.desc("ticketId"))
        )
    }

    // 예매 중인 티켓 (최신 등록 순)
    fun findAllOnSaleTicketOrderById() =
        ticketCacheRepository.findAllOnSaleTicket(LocalDateTime.now(), Sort.by(Sort.Order.desc("ticketId")))

    fun findAllOnSaleTicketByGenreOrderById(genre: Genre): List<TicketCache> {
        if (genre == Genre.CONCERT_FESTIVAL) {
            val concerts = ticketCacheRepository.findAllOnSaleTicketByGenre(
                Genre.CONCERT, LocalDateTime.now(), Sort.by(Sort.Order.desc("genreId"))
            )
            val festivals = ticketCacheRepository.findAllOnSaleTicketByGenre(
                Genre.FESTIVAL, LocalDateTime.now(), Sort.by(Sort.Order.desc("genreId"))
            )
            return concerts + festivals
        }
        return ticketCacheRepository.findAllOnSaleTicketByGenre(
            genre, LocalDateTime.now(), Sort.by(Sort.Order.desc("genreId"))
        )
    }

    // 예매 중인 티켓 (공연 날짜 임박순)
    fun findAllOnSaleTicketOrderByDay() = ticketCacheRepository.findAllOnSaleTicket(
        LocalDateTime.now(), Sort.by(Sort.Order.asc("ticketEventSchedules.dateTime"))
    )

    fun findAllOnSaleTicketByGenreOrderByDay(genre: Genre): List<TicketCache> {
        if (genre == Genre.CONCERT_FESTIVAL) {
            val concerts = ticketCacheRepository.findAllOnSaleTicketByGenre(
                Genre.CONCERT, LocalDateTime.now(), Sort.by(Sort.Order.asc("ticketEventSchedules.dateTime"))
            )
            val festivals = ticketCacheRepository.findAllOnSaleTicketByGenre(
                Genre.FESTIVAL, LocalDateTime.now(), Sort.by(Sort.Order.asc("ticketEventSchedules.dateTime"))
            )
            return concerts + festivals
        }
        return ticketCacheRepository.findAllOnSaleTicketByGenre(
            genre, LocalDateTime.now(), Sort.by(Sort.Order.asc("ticketEventSchedules.dateTime"))
        )
    }

    // 오픈 예정 티켓 검색
    fun findAllBeforeSaleTicketByKeyword(keyword: String) = ticketCacheRepository.findAllBeforeSaleTicketByKeyword(
        keyword, LocalDateTime.now(), PageRequest.of(0, 10, Sort.by(Sort.Order.asc("title")))
    )

    fun findAllBeforeSaleTicketByKeywordAndGenre(keyword: String, genre: Genre): List<TicketCache> {
        if (genre == Genre.CONCERT_FESTIVAL) {
            val concerts = ticketCacheRepository.findAllBeforeSaleTicketByKeywordAndGenre(
                Genre.CONCERT, keyword, LocalDateTime.now(), PageRequest.of(0, 10, Sort.by(Sort.Order.asc("title")))
            )
            val festivals = ticketCacheRepository.findAllBeforeSaleTicketByKeywordAndGenre(
                Genre.FESTIVAL, keyword, LocalDateTime.now(), PageRequest.of(0, 10, Sort.by(Sort.Order.asc("title")))
            )
            return concerts + festivals
        }
        return ticketCacheRepository.findAllBeforeSaleTicketByKeywordAndGenre(
            genre, keyword, LocalDateTime.now(), PageRequest.of(0, 10, Sort.by(Sort.Order.asc("title")))
        )
    }

    // 예매 중인 티켓 검색
    fun findAllOnSaleTicketByKeyword(keyword: String) = ticketCacheRepository.findAllOnSaleTicketByKeyword(
        keyword, LocalDateTime.now(), PageRequest.of(0, 10, Sort.by(Sort.Order.asc("title")))
    )

    fun findAllOnSaleTicketByKeywordAndGenre(keyword: String, genre: Genre): List<TicketCache> {
        if (genre == Genre.CONCERT_FESTIVAL) {
            val concerts = ticketCacheRepository.findAllOnSaleTicketByKeywordAndGenre(
                Genre.CONCERT, keyword, LocalDateTime.now(), PageRequest.of(0, 10, Sort.by(Sort.Order.asc("title")))
            )
            val festivals = ticketCacheRepository.findAllOnSaleTicketByKeywordAndGenre(
                Genre.FESTIVAL, keyword, LocalDateTime.now(), PageRequest.of(0, 10, Sort.by(Sort.Order.asc("title")))
            )
            return concerts + festivals
        }
        return ticketCacheRepository.findAllOnSaleTicketByKeywordAndGenre(
            genre, keyword, LocalDateTime.now(), PageRequest.of(0, 10, Sort.by(Sort.Order.asc("title")))
        )
    }

    // 티켓 검색 자동완성
    fun findAllTicketByKeyword(keyword: String) = ticketCacheRepository.findAllTicketByKeyword(
        keyword, PageRequest.of(0, 3, Sort.by(Sort.Order.asc("title")))
    )

    fun findAllTicketByKeywordAndGenre(keyword: String, genre: Genre): List<TicketCache> {
        if (genre == Genre.CONCERT_FESTIVAL) {
            val concerts = ticketCacheRepository.findAllTicketByKeywordAndGenre(
                Genre.CONCERT, keyword, PageRequest.of(0, 3, Sort.by(Sort.Order.asc("title")))
            )
            val festivals = ticketCacheRepository.findAllTicketByKeywordAndGenre(
                Genre.FESTIVAL, keyword, PageRequest.of(0, 3, Sort.by(Sort.Order.asc("title")))
            )
            return concerts + festivals
        }
        return ticketCacheRepository.findAllTicketByKeywordAndGenre(
            genre, keyword, PageRequest.of(0, 3, Sort.by(Sort.Order.asc("title")))
        )
    }

    // 아티스트 오픈 예정 티켓
    fun findAllBeforeSaleTicketByArtistId(artistId: Long) = ticketCacheRepository.findAllBeforeSaleTicketByArtistId(
        artistId, LocalDateTime.now(), Sort.by(Sort.Order.asc("ticketEventSchedules.dateTime"))
    )

    fun findAllBeforeSaleTicketByArtistIdAndGenre(artistId: Long, genre: Genre): List<TicketCache> {
        if (genre == Genre.CONCERT_FESTIVAL) {
            val concerts = ticketCacheRepository.findAllBeforeSaleTicketByArtistIdAndGenre(
                artistId, Genre.CONCERT, LocalDateTime.now(), Sort.by(Sort.Order.asc("ticketEventSchedules.dateTime"))
            )
            val festivals = ticketCacheRepository.findAllBeforeSaleTicketByArtistIdAndGenre(
                artistId, Genre.FESTIVAL, LocalDateTime.now(), Sort.by(Sort.Order.asc("ticketEventSchedules.dateTime"))
            )
            return concerts + festivals
        }
        return ticketCacheRepository.findAllBeforeSaleTicketByArtistIdAndGenre(
            artistId, genre, LocalDateTime.now(), Sort.by(Sort.Order.asc("ticketEventSchedules.dateTime"))
        )
    }

    // 아티스트 예매 중인 티켓
    fun findAllOnSaleTicketByArtistId(artistId: Long) = ticketCacheRepository.findAllOnSaleTicketByArtistId(
        artistId, LocalDateTime.now(), Sort.by(Sort.Order.asc("ticketEventSchedules.dateTime"))
    )

    fun findAllOnSaleTicketByArtistIdAndGenre(artistId: Long, genre: Genre): List<TicketCache> {
        if (genre == Genre.CONCERT_FESTIVAL) {
            val concerts = ticketCacheRepository.findAllOnSaleTicketByArtistIdAndGenre(
                artistId, Genre.CONCERT, LocalDateTime.now(), Sort.by(Sort.Order.asc("ticketEventSchedules.dateTime"))
            )
            val festivals = ticketCacheRepository.findAllOnSaleTicketByArtistIdAndGenre(
                artistId, Genre.FESTIVAL, LocalDateTime.now(), Sort.by(Sort.Order.asc("ticketEventSchedules.dateTime"))
            )
            return concerts + festivals
        }
        return ticketCacheRepository.findAllOnSaleTicketByArtistIdAndGenre(
            artistId, genre, LocalDateTime.now(), Sort.by(Sort.Order.asc("ticketEventSchedules.dateTime"))
        )
    }

    // 오픈 예정 티켓 by artistIds
    fun findAllBeforeSaleTicketByArtistIds(artistIds: List<Long>) =
        ticketCacheRepository.findAllBeforeSaleTicketByArtistIds(
            artistIds, LocalDateTime.now()
        )

    fun findAllBeforeSaleTicketByArtistIdsAndGenre(artistIds: List<Long>, genre: Genre): List<TicketCache> {
        if (genre == Genre.CONCERT_FESTIVAL) {
            val concerts = ticketCacheRepository.findAllBeforeSaleTicketByArtistIdsAndGenre(
                artistIds, Genre.CONCERT, LocalDateTime.now()
            )
            val festivals = ticketCacheRepository.findAllBeforeSaleTicketByArtistIdsAndGenre(
                artistIds, Genre.FESTIVAL, LocalDateTime.now()
            )
            return concerts + festivals
        }
        return ticketCacheRepository.findAllBeforeSaleTicketByArtistIdsAndGenre(
            artistIds, genre, LocalDateTime.now()
        )
    }

    // 예매 중인 티켓 by artistIds
    fun findAllOnSaleTicketByArtistIds(artistIds: List<Long>) = ticketCacheRepository.findAllOnSaleTicketByArtistIds(
        artistIds, LocalDateTime.now(), Sort.by(Sort.Order.asc("ticketEventSchedules.dateTime"))
    )

    fun findAllOnSaleTicketByArtistIdsAndGenre(artistIds: List<Long>, genre: Genre): List<TicketCache> {
        if (genre == Genre.CONCERT_FESTIVAL) {
            val concerts = ticketCacheRepository.findAllOnSaleTicketByArtistIdsAndGenre(
                artistIds, Genre.CONCERT, LocalDateTime.now(), Sort.by(Sort.Order.asc("ticketEventSchedules.dateTime"))
            )
            val festivals = ticketCacheRepository.findAllOnSaleTicketByArtistIdsAndGenre(
                artistIds, Genre.FESTIVAL, LocalDateTime.now(), Sort.by(Sort.Order.asc("ticketEventSchedules.dateTime"))
            )
            return concerts + festivals
        }
        return ticketCacheRepository.findAllOnSaleTicketByArtistIdsAndGenre(
            artistIds, genre, LocalDateTime.now(), Sort.by(Sort.Order.asc("ticketEventSchedules.dateTime"))
        )
    }

    // 오픈 예정 티켓 by TicketIds
    fun findAllBeforeSaleTicketByTicketIds(ticketIds: List<Long>) =
        ticketCacheRepository.findAllBeforeSaleTicketByTicketIds(
            ticketIds, LocalDateTime.now()
        )

    fun findAllByGenre(genre: Genre) = ticketCacheRepository.findAllByGenreOrderByTicketIdDesc(genre)
}