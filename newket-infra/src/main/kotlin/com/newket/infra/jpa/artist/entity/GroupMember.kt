package com.newket.infra.jpa.artist.entity

import com.newket.infra.jpa.config.BaseDateEntity
import jakarta.persistence.Entity

@Entity
class GroupMember(
    val memberId: Long,
    val groupId: Long,
) : BaseDateEntity()