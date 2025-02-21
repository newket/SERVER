package com.newket.infra.jpa.artist.repository

import com.newket.infra.jpa.artist.entity.Artist
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ArtistRepository : JpaRepository<Artist, Long> {
    fun findByName(name: String): Artist?

    @Query(
        """
        select a
        from Artist a 
        where a.name like concat('%', :keyword, '%') or a.subName like concat('%', :keyword, '%') or a.nickname like concat('%', :keyword, '%')
        order by 
            case 
                when a.name like concat('%', :keyword, '%') then 1
                when a.subName like concat('%', :keyword, '%') then 2
                when a.nickname like concat('%', :keyword, '%') then 3
                else 4
            end,
            a.name
        limit 10
    """
    )
    fun searchByKeyword(keyword: String): List<Artist>

    @Query(
        """
        select DISTINCT a
        from Artist a
        where a.name like concat('%', :keyword, '%') or a.subName like concat('%', :keyword, '%') or a.nickname like concat('%', :keyword, '%')
        order by 
            case 
                when a.name like concat('%', :keyword, '%') then 1
                when a.subName like concat('%', :keyword, '%') then 2
                when a.nickname like concat('%', :keyword, '%') then 3
                else 4
            end,
            a.name
        limit 3
    """
    )
    fun autocompleteByKeyword(keyword: String): List<Artist>

    @Query("SELECT a.id FROM Artist a ORDER BY FUNCTION('RAND') LIMIT 10")
    fun findRandomArtistIds(): List<Long>

    @Query("SELECT a FROM Artist a WHERE a.id IN :ids")
    fun findArtistsByIds(@Param("ids") ids: List<Long>): List<Artist>

}