package com.newket.infra.jpa.artist.repository

import com.newket.infra.jpa.artist.entity.Artist
import com.newket.infra.jpa.artist.entity.GroupMember
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface GroupMemberRepository : JpaRepository<GroupMember, Long> {

    @Query(
        """
        select a
        from GroupMember gm
        join Artist a on gm.groupId=a.id
        where gm.memberId=:memberId
    """
    )
    fun findAllByMemberId(memberId: Long): List<Artist>

    @Query(
        """
        select a
        from GroupMember gm
        join Artist a on gm.memberId=a.id
        where gm.groupId=:groupId
    """
    )
    fun findAllByGroupId(groupId: Long): List<Artist>
}