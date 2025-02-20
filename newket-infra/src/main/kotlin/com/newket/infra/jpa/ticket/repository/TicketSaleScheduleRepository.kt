package com.newket.infra.jpa.ticket.repository

import com.newket.infra.jpa.ticket.entity.TicketSaleSchedule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate
import java.time.LocalTime

interface TicketSaleScheduleRepository : JpaRepository<TicketSaleSchedule, Long> {
    @Query(
        """
        select s
        from TicketSaleSchedule s
        where s.day > :date or (s.day=:date and s.time > :time)
        order by s.day, s.time
    """
    )
    fun findAllBeforeOpenOrderByDay(date: LocalDate, time: LocalTime): List<TicketSaleSchedule>

    @Query(
        """
        select s
        from TicketSaleSchedule s
        where s.day > :date or (s.day=:date and s.time > :time)
        order by s.ticketSaleUrl.ticket.id desc, s.day, s.time
    """
    )
    fun findAllBeforeOpenOrderById(date: LocalDate, time: LocalTime): List<TicketSaleSchedule>

    @Query(
        """
        select s
        from TicketSaleSchedule s
        join TicketSaleUrl u on u.id=s.ticketSaleUrl.id
        where u.ticket.id = :ticketId
    """
    )
    fun findALlByTicketId(ticketId: Long): List<TicketSaleSchedule>

    @Query(
        """
        select s
        from TicketSaleSchedule s
        join TicketArtist ca on ca.ticket.id = s.ticketSaleUrl.ticket.id
        where (s.day > :date or (s.day=:date and s.time > :time))
        and (ca.ticket.title like concat('%', :keyword, '%') or ca.artist.name like concat('%', :keyword, '%') or ca.artist.subName like concat('%', :keyword, '%') or ca.artist.nickname like concat('%', :keyword, '%'))
        order by ca.ticket.title desc, s.day, s.time
        limit 20
    """
    )
    fun findAllOpeningNoticeContainsKeyword(date: LocalDate, time: LocalTime, keyword: String): List<TicketSaleSchedule>

    @Query(
        """
        select s
        from TicketSaleSchedule s
        join TicketArtist ca on ca.ticket.id = s.ticketSaleUrl.ticket.id
        join ArtistNotification fa on ca.artist.id = fa.artistId
        where (s.day > :date or (s.day=:date and s.time > :time))
        and fa.userId=:userId
        order by s.ticketSaleUrl.ticket.id desc, s.day, s.time
    """
    )
    fun findAllFavoriteConcertByUserIdNowAfterOrderByIdDesc(
        userId: Long,
        date: LocalDate,
        time: LocalTime
    ): List<TicketSaleSchedule>

    @Query(
        """
        select s
        from TicketSaleSchedule s
        join Ticket c on c.id=s.ticketSaleUrl.ticket.id
        join TicketNotification n on n.ticketId=c.id
        where n.userId=:userId
        and (s.day > :date or (s.day=:date and s.time > :time))
        order by s.ticketSaleUrl.ticket.id desc, s.day, s.time
    """
    )
    fun findAllNotificationConcertTicketingSchedulesByUserId(
        userId: Long,
        date: LocalDate,
        time: LocalTime
    ): List<TicketSaleSchedule>

    @Query(
        """
        select s
        from TicketSaleSchedule s
        join Ticket t on t.id=s.ticketSaleUrl.ticket.id
        join TicketNotification n on n.ticketId=t.id
        where s.day = :date and hour(s.time) = hour(:time)
        order by s.ticketSaleUrl.ticket.id desc
    """
    )
    fun findAllTicketSaleScheduleByDateAndTime(date: LocalDate, time: LocalTime): List<TicketSaleSchedule>
}