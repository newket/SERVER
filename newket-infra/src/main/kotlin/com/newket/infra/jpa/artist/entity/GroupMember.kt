package com.newket.infra.jpa.artist.entity

import com.newket.infra.jpa.config.BaseDateEntity
import jakarta.persistence.Entity

@Entity
class GroupMember(
    var memberId: Long,
    var groupId: Long,
) : BaseDateEntity()