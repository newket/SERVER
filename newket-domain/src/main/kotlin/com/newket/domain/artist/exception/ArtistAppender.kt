package com.newket.domain.artist.exception

import com.newket.infra.jpa.artist.entity.Artist
import com.newket.infra.jpa.artist.entity.GroupMember
import com.newket.infra.jpa.artist.repository.ArtistRepository
import com.newket.infra.jpa.artist.repository.GroupMemberRepository
import org.springframework.stereotype.Service

@Service
class ArtistAppender(
    private val artistRepository: ArtistRepository,
    private val groupMemberRepository: GroupMemberRepository
) {
    fun saveAll(artistList: List<Artist>) = artistRepository.saveAll(artistList)

    fun saveAllGroups(groupList: List<GroupMember>) = groupMemberRepository.saveAll(groupList)
}