package com.newket.domain.artist.exception

import com.newket.infra.jpa.artist.entity.Artist
import com.newket.infra.jpa.artist.entity.GroupMember
import com.newket.infra.jpa.artist.repository.ArtistRepository
import com.newket.infra.jpa.artist.repository.GroupMemberRepository
import org.springframework.stereotype.Service

@Service
class ArtistRemover(
    private val artistRepository: ArtistRepository, private val groupMemberRepository: GroupMemberRepository
) {
    fun deleteAll(artists: List<Artist>) = artistRepository.deleteAll(artists)

    fun deleteAllGroups(groups: List<GroupMember>) = groupMemberRepository.deleteAll(groups)
}